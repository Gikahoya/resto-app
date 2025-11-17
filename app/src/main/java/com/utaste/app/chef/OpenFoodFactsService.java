package com.utaste.app.chef;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.utaste.domain.recipe.NutritionFact;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OpenFoodFactsService {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    public interface NutritionFactCallback {
        void onSuccess(NutritionFact nutritionFact);
        void onError(String message);
    }

    public void fetchNutritionFacts(String barcode, NutritionFactCallback callback) {
        executor.execute(() -> {
            try {
                // ================== CORRECTION DE L'URL ==================
                // On utilise la version v0 de l'API, plus stable.
                URL url = new URL("https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json");
                // =======================================================

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                if (connection.getResponseCode() == 200) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    ProductResponse response = gson.fromJson(reader, ProductResponse.class);
                    reader.close();

                    // La structure de la réponse de la v0 est légèrement différente
                    if (response != null && "product found".equals(response.status_verbose) && response.product != null && response.product.nutriments != null) {
                        Nutriments n = response.product.nutriments;
                        NutritionFact nf = new NutritionFact(
                                n.carbohydrates_100g,
                                n.proteins_100g,
                                n.fat_100g
                        );
                        nf.setFibersPer100g(n.fiber_100g);
                        nf.setSaltPer100g(n.salt_100g);
                        handler.post(() -> callback.onSuccess(nf));
                    } else {
                        handler.post(() -> callback.onError("Product data not found."));
                    }
                } else {
                    handler.post(() -> {
                        try {
                            callback.onError("Product not found (API Error " + connection.getResponseCode() + ")");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                handler.post(() -> callback.onError("Error: " + e.getMessage()));
            }
        });
    }

    // Les classes internes pour parser le JSON
    private static class ProductResponse {
        Product product;
        String status_verbose; // La v0 utilise ce champ pour indiquer si le produit est trouvé
    }

    private static class Product {
        Nutriments nutriments;
    }

    private static class Nutriments {
        @SerializedName("carbohydrates_100g")
        double carbohydrates_100g;

        @SerializedName("proteins_100g")
        double proteins_100g;

        @SerializedName("fat_100g")
        double fat_100g;

        @SerializedName("fiber_100g")
        double fiber_100g;

        @SerializedName("salt_100g")
        double salt_100g;
    }

}