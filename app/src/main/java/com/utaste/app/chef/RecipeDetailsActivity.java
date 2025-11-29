package com.utaste.app.chef;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.utaste.R;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;

import java.util.List;
import java.util.Locale;

public class RecipeDetailsActivity extends AppCompatActivity {

    private TextView tvRecipeName, tvDescription, tvIngredientsList;
    private ImageView ivRecipeImage;
    private ImageButton btnBack;

    private RecipeDao recipeDao;
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
        loadRecipeDetails();
    }

    private void initViews() {
        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvDescription = findViewById(R.id.tvDescription);
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        btnBack = findViewById(R.id.btnBack);
        tvIngredientsList = findViewById(R.id.tvIngredientsList);
    }

    private void loadRecipeDetails() {
        new Thread(() -> {
            Recipe recipe = recipeDao.findById(recipeId);
            List<RecipeDao.RecipeIngredientRow> ingredients = recipeDao.getIngredientsForRecipe(recipeId);

            runOnUiThread(() -> {
                if (recipe == null) {
                    Toast.makeText(this, "Error loading recipe details.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                tvRecipeName.setText(recipe.getName());
                tvDescription.setText(recipe.getDescription());

                Glide.with(this)
                        .load(recipe.getImagePath())
                        .placeholder(R.drawable.ic_launcher_background)
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
            // CORRECTION : Utiliser Ingredient.Unit.format pour un affichage propre
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
    protected void onDestroy() {
        super.onDestroy();
        if (recipeDao != null) {
            recipeDao.close();
        }
    }
}