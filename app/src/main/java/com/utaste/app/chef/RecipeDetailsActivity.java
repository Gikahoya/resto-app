package com.utaste.app.chef;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.utaste.R;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;
import com.utaste.ui.recipe.CreateRecipeActivity;

import java.util.List;
import java.util.Locale;

public class RecipeDetailsActivity extends AppCompatActivity {

    private TextView tvRecipeName, tvDescription, tvIngredientsList;
    private ImageView ivRecipeImage;
    private ImageButton btnBack;
    private FloatingActionButton fabEditRecipe;

    private RecipeDao recipeDao;
    private Recipe currentRecipe; // ✅ AJOUT : Garder une référence à la recette chargée
    private long recipeId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        recipeId = getIntent().getLongExtra("RECIPE_ID", -1);
        if (recipeId == -1) {
            Toast.makeText(this, "Error: Recipe not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recipeDao = new RecipeDao(this);
        initViews();

        btnBack.setOnClickListener(v -> finish());

        // Le chargement se fait dans onResume()

        // ✅ MODIFICATION : Le listener est maintenant configuré ici
        fabEditRecipe.setOnClickListener(view -> {
            if (currentRecipe != null) {
                Intent intent = new Intent(RecipeDetailsActivity.this, CreateRecipeActivity.class);
                // On passe TOUTES les infos de la recette
                intent.putExtra("RECIPE_ID", currentRecipe.getId());
                intent.putExtra("RECIPE_NAME", currentRecipe.getName());
                intent.putExtra("RECIPE_DESCRIPTION", currentRecipe.getDescription());
                intent.putExtra("RECIPE_IMAGE_PATH", currentRecipe.getImagePath());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Cannot modify recipe: data not loaded yet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvDescription = findViewById(R.id.tvDescription);
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        btnBack = findViewById(R.id.btnBack);
        tvIngredientsList = findViewById(R.id.tvIngredientsList);
        fabEditRecipe = findViewById(R.id.fabEditRecipe);
    }

    private void loadRecipeDetails() {
        new Thread(() -> {
            // On charge la recette et ses ingrédients
            currentRecipe = recipeDao.findById(recipeId); // ✅ MODIFICATION: Stocke la recette
            List<RecipeDao.RecipeIngredientRow> ingredients = recipeDao.getIngredientsForRecipe(recipeId);

            runOnUiThread(() -> {
                if (currentRecipe == null) {
                    Toast.makeText(this, "Error loading recipe details.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Affichage des détails
                tvRecipeName.setText(currentRecipe.getName());
                tvDescription.setText(currentRecipe.getDescription());

                Glide.with(this)
                        .load(currentRecipe.getImagePath())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivRecipeImage);

                displayIngredients(ingredients);
            });
        }).start();
    }

    private void displayIngredients(List<RecipeDao.RecipeIngredientRow> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            tvIngredientsList.setText("No ingredients linked to this recipe yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (RecipeDao.RecipeIngredientRow row : ingredients) {
            Ingredient.Unit unitEnum = Ingredient.Unit.fromDb(row.unit);
            String formattedQuantity = Ingredient.Unit.format(row.quantityInGrams, unitEnum);

            sb.append("- ")
                    .append(row.ingredient.getName())
                    .append(" (")
                    .append(formattedQuantity)
                    .append(")\n");
        }
        tvIngredientsList.setText(sb.toString().trim());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les détails au cas où ils auraient été modifiés
        loadRecipeDetails();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recipeDao != null) {
            recipeDao.close();
        }
    }
}
