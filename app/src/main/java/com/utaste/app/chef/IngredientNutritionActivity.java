package com.utaste.app.chef;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.NutritionFact;
import com.utaste.domain.recipe.OpenFoodFactsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class IngredientNutritionActivity extends AppCompatActivity {

    private AutoCompleteTextView actvIngredientName;
    private Button btnFetch;
    private TextView tvCarbs, tvProtein, tvFat, tvSalt, tvEnergy, tvSaturatedFat, tvSugars;
    private ImageButton btnBack;

    private IngredientDao ingredientDao;
    private OpenFoodFactsClient offClient;
    private List<Ingredient> allIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients_nutrition_facts);

        ingredientDao = new IngredientDao(this);
        offClient = new OpenFoodFactsClient();

        initViews();
        btnBack.setOnClickListener(v -> finish());
        btnFetch.setOnClickListener(v -> onFetchClicked());
        loadIngredientsForAutoComplete();
    }

    private void initViews() {
        actvIngredientName = findViewById(R.id.actvIngredientName);
        btnFetch = findViewById(R.id.btnFetch);
        btnBack = findViewById(R.id.btnBack);

        tvEnergy = findViewById(R.id.tvEnergy);
        tvFat = findViewById(R.id.tvFat);
        tvSaturatedFat = findViewById(R.id.tvSaturatedFat);
        tvCarbs = findViewById(R.id.tvCarbs);
        tvSugars = findViewById(R.id.tvSugars);
        tvProtein = findViewById(R.id.tvProtein);
        tvSalt = findViewById(R.id.tvSalt);
    }

    private void loadIngredientsForAutoComplete() {
        new Thread(() -> {
            allIngredients = ingredientDao.getAll();
            List<String> ingredientNames = allIngredients.stream()
                    .map(Ingredient::getName)
                    .collect(Collectors.toCollection(ArrayList::new));

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, ingredientNames);
                actvIngredientName.setAdapter(adapter);
            });
        }).start();
    }

    private void onFetchClicked() {
        String ingredientName = actvIngredientName.getText().toString().trim();
        if (ingredientName.isEmpty()) {
            actvIngredientName.setError("Ingredient name is required");
            actvIngredientName.requestFocus();
            return;
        }

        Ingredient selectedIngredient = null;
        for (Ingredient ingredient : allIngredients) {
            if (ingredient.getName().equalsIgnoreCase(ingredientName)) {
                selectedIngredient = ingredient;
                break;
            }
        }

        if (selectedIngredient == null) {
            Toast.makeText(this, "Ingredient not found in your database.", Toast.LENGTH_SHORT).show();
            return;
        }

        String barcode = selectedIngredient.getQrCode();
        if (barcode == null || barcode.trim().isEmpty()) {
            Toast.makeText(this, "This ingredient does not have a barcode.", Toast.LENGTH_SHORT).show();
            return;
        }
        fetchDataForBarcode(barcode);
    }

    private void fetchDataForBarcode(final String barcode) {
        btnFetch.setEnabled(false);
        btnFetch.setText("Loading...");
        resetDisplay();

        new Thread(() -> {
            NutritionFact nf = null;
            String errorMessage = null;
            try {
                nf = offClient.fetchNutritionForBarcode(barcode);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = "Network error: " + e.getMessage();
            }

            final NutritionFact result = nf;
            final String finalErrorMessage = errorMessage;

            runOnUiThread(() -> {
                btnFetch.setEnabled(true);
                btnFetch.setText("Fetch nutrition facts");

                if (result != null) {
                    showNutrition(result);
                } else if (finalErrorMessage != null) {
                    Toast.makeText(this, finalErrorMessage, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Product not found on OpenFoodFacts.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void resetDisplay() {
        tvEnergy.setText("Total energy: 0 kcal");
        tvFat.setText("Fat: 0.0 g");
        tvSaturatedFat.setText("  of which saturates: 0.0 g");
        tvCarbs.setText("Carbohydrates: 0.0 g");
        tvSugars.setText("  of which sugars: 0.0 g");
        tvProtein.setText("Proteins: 0.0 g");
        tvSalt.setText("Salt: 0.0 g");
    }

    private void showNutrition(NutritionFact nf) {
        tvEnergy.setText(String.format(Locale.getDefault(), "Total energy: %.0f kcal", nf.getEnergyKcal()));
        tvFat.setText(String.format(Locale.getDefault(), "Fat: %.1f g", nf.getFat()));
        tvSaturatedFat.setText(String.format(Locale.getDefault(), "  of which saturates: %.1f g", nf.getSaturatedFat()));
        tvCarbs.setText(String.format(Locale.getDefault(), "Carbohydrates: %.1f g", nf.getCarbs()));
        tvSugars.setText(String.format(Locale.getDefault(), "  of which sugars: %.1f g", nf.getSugars()));
        tvProtein.setText(String.format(Locale.getDefault(), "Proteins: %.1f g", nf.getProtein()));
        tvSalt.setText(String.format(Locale.getDefault(), "Salt: %.1f g", nf.getSalt()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ingredientDao != null) {
            ingredientDao.close();
        }
    }
}