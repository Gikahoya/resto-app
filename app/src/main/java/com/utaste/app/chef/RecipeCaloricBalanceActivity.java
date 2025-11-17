package com.utaste.app.chef;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.NutritionCalculator;
import com.utaste.domain.recipe.NutritionFact;
import com.utaste.domain.recipe.Recipe;
import com.utaste.domain.recipe.RecipeNutritionEntry;
import com.utaste.domain.recipe.RecipeNutritionSummary;
import com.utaste.remote.OpenFoodFactsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeCaloricBalanceActivity extends AppCompatActivity {

    private Spinner spRecipe;
    private Button btnCompute;
    private TextView tvCarbs, tvProtein, tvFat, tvEnergy;

    private RecipeDao recipeDao;
    private OpenFoodFactsClient offClient;

    private ImageButton btnBack;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_caloric_balance);

        // Bind UI
        spRecipe   = findViewById(R.id.spRecipe);
        btnCompute = findViewById(R.id.btnComputeBalance);
        tvCarbs    = findViewById(R.id.tvCarbs);
        tvProtein  = findViewById(R.id.tvProtein);
        tvFat      = findViewById(R.id.tvFat);
        tvEnergy   = findViewById(R.id.tvEnergy);
        btnBack    = findViewById(R.id.btnBack);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // DAOs
        recipeDao = new RecipeDao(this);
        offClient = new OpenFoodFactsClient();

        // Load recipes
        loadRecipesIntoSpinner();

        // Compute caloric balance
        btnCompute.setOnClickListener(v -> computeBalance());
    }

    /**
     * Load recipes inside spinner with nice clean display
     */
    private void loadRecipesIntoSpinner() {
        List<Recipe> recipes = (List<Recipe>) recipeDao.getAll();

        if (recipes.isEmpty()) {
            Toast.makeText(this,
                    "No recipes found. Create a recipe first.",
                    Toast.LENGTH_SHORT).show();
        }

        // Ensure clean display (avoid toString crazy output)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getRecipeNames(recipes)
        );

        spRecipe.setAdapter(adapter);
    }

    /**
     * Utility: convert Recipe objects → recipe names
     */
    private List<String> getRecipeNames(List<Recipe> list) {
        List<String> names = new ArrayList<>();
        for (Recipe r : list) names.add(r.getName());
        return names;
    }

    /**
     * Compute caloric balance for the selected recipe
     */
    private void computeBalance() {
        String selectedName = (String) spRecipe.getSelectedItem();

        if (selectedName == null) {
            Toast.makeText(this, "Please select a recipe.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch recipe by name
        Recipe recipe = recipeDao.getByName(selectedName);

        if (recipe == null) {
            Toast.makeText(this, "Error loading recipe.", Toast.LENGTH_SHORT).show();
            return;
        }

        long recipeId = recipe.getId();

        btnCompute.setEnabled(false);
        btnCompute.setText("Computing...");

        new Thread(() -> {

            List<RecipeDao.RecipeIngredientRow> rows =
                    recipeDao.getIngredientsForRecipe(recipeId);

            List<RecipeNutritionEntry> entries = new ArrayList<>();

            for (RecipeDao.RecipeIngredientRow row : rows) {

                Ingredient ing = row.ingredient;
                double qtyGrams = row.quantityInGrams;

                NutritionFact nf = ing.getNutritionFact();

                // Fetch nutrition if missing (OpenFoodFacts)
                if (nf == null && ing.getQrCode() != null && !ing.getQrCode().isEmpty()) {
                    try {
                        nf = offClient.fetchNutritionForBarcode(ing.getQrCode());
                        ing.setNutritionFact(nf);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                entries.add(new RecipeNutritionEntry(ing, qtyGrams));
            }

            RecipeNutritionSummary summary =
                    NutritionCalculator.computeSummary(entries);

            runOnUiThread(() -> {
                btnCompute.setEnabled(true);
                btnCompute.setText("Compute caloric balance");

                tvCarbs.setText(String.format(Locale.getDefault(),
                        "Carbohydrates: %.1f g", summary.getTotalCarbs()));

                tvProtein.setText(String.format(Locale.getDefault(),
                        "Proteins: %.1f g", summary.getTotalProtein()));

                tvFat.setText(String.format(Locale.getDefault(),
                        "Fat: %.1f g", summary.getTotalFat()));

                tvEnergy.setText(String.format(Locale.getDefault(),
                        "Total energy: %.0f kcal", summary.getTotalCalories()));

                if (rows.isEmpty()) {
                    Toast.makeText(this,
                            "This recipe has no ingredients.",
                            Toast.LENGTH_SHORT).show();
                }
            });

        }).start();
    }
}
