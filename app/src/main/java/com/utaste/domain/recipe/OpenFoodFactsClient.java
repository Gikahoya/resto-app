package com.utaste.domain.recipe;

import android.util.Log;

import com.utaste.domain.recipe.NutritionFact;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Client très simple pour l'API OpenFoodFacts.
 *
 * Appel :
 *   https://world.openfoodfacts.org/api/v2/product/{barcode}.json
 *
 * On récupère :
 *   - carbohydrates_100g
 *   - proteins_100g
 *   - fat_100g
 *   - energy-kcal_100g (ou energy_100g en kJ qu'on convertit en kcal)
 */
public class OpenFoodFactsClient {

    private static final String TAG = "OpenFoodFactsClient";
    private static final String BASE_URL =
            "https://world.openfoodfacts.org/api/v2/product/";

    /**
     * Va chercher les infos nutritionnelles pour un code-barres donné.
     *
     * @param barcode code-barres (ex: "5449000000996")
     * @return NutritionFact ou null si produit introuvable / erreur.
     */
    public NutritionFact fetchNutritionForBarcode(String barcode) throws Exception {
        if (barcode == null || barcode.trim().isEmpty()) {
            return null;
        }

        String urlString = BASE_URL + barcode.trim() + ".json";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "HTTP status " + status + " for " + urlString);
                return null;
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            JSONObject root = new JSONObject(sb.toString());

            // status_verbose = "product found" ou "success"
            String statusVerbose = root.optString("status_verbose", "");
            if (!statusVerbose.toLowerCase().contains("product")) {
                Log.w(TAG, "Product not found for barcode " + barcode);
                return null;
            }

            JSONObject product = root.optJSONObject("product");
            if (product == null) {
                return null;
            }

            JSONObject nutriments = product.optJSONObject("nutriments");
            if (nutriments == null) {
                return null;
            }

            double carbs100   = nutriments.optDouble("carbohydrates_100g", 0.0);
            double protein100 = nutriments.optDouble("proteins_100g",      0.0);
            double fat100     = nutriments.optDouble("fat_100g",           0.0);

            // energy-kcal_100g parfois absent, on essaie energy_100g (en kJ)
            double energyKcal100 = nutriments.optDouble("energy-kcal_100g", Double.NaN);
            if (Double.isNaN(energyKcal100)) {
                double energy100_kJ = nutriments.optDouble("energy_100g", 0.0);
                energyKcal100 = energy100_kJ / 4.184; // kJ -> kcal approx
            }

            NutritionFact nf = new NutritionFact();
            nf.setCarbsPer100g(carbs100);
            nf.setProteinPer100g(protein100);
            nf.setFatPer100g(fat100);
            nf.setCaloriesPer100g(energyKcal100);

            Log.d(TAG, "Barcode " + barcode + " -> " +
                    carbs100 + "g carbs, " +
                    protein100 + "g protein, " +
                    fat100 + "g fat, " +
                    energyKcal100 + " kcal /100g");

            return nf;
        } finally {
            conn.disconnect();
        }
    }
}
