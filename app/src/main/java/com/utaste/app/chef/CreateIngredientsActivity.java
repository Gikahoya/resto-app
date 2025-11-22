package com.utaste.app.chef;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.NutritionFact;
import com.utaste.domain.recipe.OpenFoodFactsClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateIngredientsActivity extends AppCompatActivity {

    // Éléments d'interface
    private EditText etName;
    private EditText etQrCode;
    private Button btnSave;

    // Outils techniques
    private IngredientDao ingredientDao;
    private final OpenFoodFactsClient apiClient = new OpenFoodFactsClient();

    // Executor pour gérer l'appel réseau + BDD sans bloquer l'UI
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ingredient);

        // Initialisation DAO
        ingredientDao = new IngredientDao(this);

        // Initialisation des Vues
        initViews();

        // Clic sur le bouton Sauvegarder
        btnSave.setOnClickListener(v -> attemptSaveIngredient());

    }

    private void initViews() {
        etName = findViewById(R.id.et_ingredient_name);
        etQrCode = findViewById(R.id.et_qr_code);
        btnSave = findViewById(R.id.btn_save);
    }

    private void attemptSaveIngredient() {
        // 1. Récupération des champs
        String name = etName.getText().toString().trim();
        String qrCode = etQrCode.getText().toString().trim();

        // 2. Validations
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required.");
            return;
        }

        // Le QR Code est désormais obligatoire pour l'API
        if (TextUtils.isEmpty(qrCode)) {
            etQrCode.setError("The QR code is mandatory for retrieving nutritional information.");
            return;
        }

        // Désactiver le bouton pour éviter les doubles clics
        btnSave.setEnabled(false);
        Toast.makeText(this, "Nutritiousness data retrieval...", Toast.LENGTH_SHORT).show();

        // 3. Traitement en arrière-plan (Réseau + BDD)
        executor.execute(() -> {
            NutritionFact nutritionFact = null;

            // A. Appel API OpenFoodFacts
            try {
                nutritionFact = apiClient.fetchNutritionForBarcode(qrCode);
            } catch (Exception e) {
                e.printStackTrace();
                // On continue même si l'API échoue (on aura null)
            }

            // B. Création de l'objet Ingrédient
            Ingredient ingredient = new Ingredient(name, qrCode);

            if (nutritionFact != null) {
                ingredient.setNutritionFact(nutritionFact);
            }

            // C. Insertion en base de données
            long id = ingredientDao.insert(ingredient);

            // D. Retour sur le Thread Principal (UI)
            NutritionFact finalNutritionFact = nutritionFact; // Pour usage dans le lambda
            mainHandler.post(() -> {
                btnSave.setEnabled(true);

                if (id != -1) {
                    String msg = "Ingredient created successfully !";
                    if (finalNutritionFact != null) {
                        msg += " (Nutritiousness data retrieved)";
                    } else {
                        msg += " (Nutritiousness data not available)";
                    }
                    Toast.makeText(CreateIngredientsActivity.this, msg, Toast.LENGTH_LONG).show();
                    finish(); // Ferme l'activité
                } else {
                    Toast.makeText(CreateIngredientsActivity.this, "Error creating ingredient", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ingredientDao != null) {
            ingredientDao.close();
        }
    }
}
