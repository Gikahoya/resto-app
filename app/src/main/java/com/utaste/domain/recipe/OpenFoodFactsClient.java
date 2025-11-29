package com.utaste.domain.recipe;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenFoodFactsClient {

    private static final String TAG = "OpenFoodFactsClient";
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v2/product/";

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

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            JSONObject root = new JSONObject(sb.toString());

            String statusVerbose = root.optString("status_verbose", "");
            if (!statusVerbose.toLowerCase().contains("product found")) {
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

            double carbs100 = nutriments.optDouble("carbohydrates_100g", 0.0);
            double protein100 = nutriments.optDouble("proteins_100g", 0.0);
            double fat100 = nutriments.optDouble("fat_100g", 0.0);
            double fiber100 = nutriments.optDouble("fiber_100g", 0.0);
            double salt100 = nutriments.optDouble("salt_100g", 0.0);
            double saturatedFat100 = nutriments.optDouble("saturated-fat_100g", 0.0);
            double sugars100 = nutriments.optDouble("sugars_100g", 0.0);

            double energyKcal100 = nutriments.optDouble("energy-kcal_100g", Double.NaN);
            if (Double.isNaN(energyKcal100)) {
                double energy100_kJ = nutriments.optDouble("energy_100g", 0.0);
                energyKcal100 = energy100_kJ / 4.184;
            }

            NutritionFact nf = new NutritionFact();
            nf.setCarbsPer100g(carbs100);
            nf.setProteinPer100g(protein100);
            nf.setFatPer100g(fat100);
            nf.setFiberPer100g(fiber100);
            nf.setSaltPer100g(salt100);
            nf.setCaloriesPer100g(energyKcal100);
            nf.setSaturatedFatPer100g(saturatedFat100);
            nf.setSugarsPer100g(sugars100);

            Log.d(TAG, "Fetched data for " + barcode + ": " + nf.toString());

            return nf;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}