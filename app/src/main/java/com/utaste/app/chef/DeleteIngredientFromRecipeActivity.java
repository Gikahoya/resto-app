package com.utaste.app.chef;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.data.sqlite.RecipeIngredientDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;

import java.util.List;
import java.util.stream.Collectors;

public class DeleteIngredientFromRecipeActivity extends AppCompatActivity {

    // Vues
    private TextView tvIngredientName;
    private Spinner spRecipe;
    private Button btnDelete;

    // DAOs
    private IngredientDao ingredientDao;
    private RecipeDao recipeDao;
    private RecipeIngredientDao recipeIngredientDao;

    // Données
    private Ingredient currentIngredient;
    private int ingredientId = -1;
    private List<RecipeDao.RecipeIngredientRow> recipesContainingIngredient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_ingredient_from_recipe);

        ingredientId = getIntent().getIntExtra("INGREDIENT_ID", -1);
        if (ingredientId == -1) {
            Toast.makeText(this, "Error: Ingredient ID not provided.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initDaos();
        initViews();
        setupListeners();
        loadData();
    }

    private void initDaos() {
        ingredientDao = new IngredientDao(this);
        recipeDao = new RecipeDao(this);
        recipeIngredientDao = new RecipeIngredientDao(this);
    }

    private void initViews() {
        tvIngredientName = findViewById(R.id.tvIngredientName);
        spRecipe = findViewById(R.id.spRecipe);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> confirmUnlink());
    }

    private void loadData() {
        new Thread(() -> {
            // Récupérer l'ingrédient et les recettes qui l'utilisent
            currentIngredient = ingredientDao.getById(ingredientId);
            recipesContainingIngredient = recipeDao.getRecipesForIngredient(ingredientId);

            // Noms des recettes pour le Spinner
            List<String> recipeNames = recipesContainingIngredient.stream()
                    .map(row -> row.recipe.getName())
                    .collect(Collectors.toList());

            runOnUiThread(() -> {
                if (currentIngredient == null) {
                    Toast.makeText(this, "Could not load ingredient.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (recipesContainingIngredient.isEmpty()) {
                    Toast.makeText(this, "This ingredient is not used in any recipe.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                tvIngredientName.setText(currentIngredient.getName());

                // Configurer le spinner des recettes
                ArrayAdapter<String> recipeAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item, recipeNames);
                spRecipe.setAdapter(recipeAdapter);
            });
        }).start();
    }

    private void confirmUnlink() {
        if (spRecipe.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a recipe.", Toast.LENGTH_SHORT).show();
            return;
        }
        String recipeName = spRecipe.getSelectedItem().toString();

        new AlertDialog.Builder(this)
                .setTitle("Unlink Ingredient")
                .setMessage("Are you sure you want to remove '" + currentIngredient.getName() + "' from the recipe '" + recipeName + "'?")
                .setPositiveButton("Unlink", (dialog, which) -> unlinkIngredient(recipeName))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void unlinkIngredient(String recipeName) {
        // Trouver l'ID de la recette
        long recipeId = -1;
        for (RecipeDao.RecipeIngredientRow row : recipesContainingIngredient) {
            if (row.recipe.getName().equals(recipeName)) {
                recipeId = row.recipe.getId();
                break;
            }
        }
        if (recipeId == -1) {
            Toast.makeText(this, "Error finding selected recipe.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Supprimer le lien en base de données
        long finalRecipeId = recipeId;
        new Thread(() -> {
            boolean success = recipeIngredientDao.deleteLink(finalRecipeId, ingredientId);
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Ingredient unlinked successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to unlink ingredient.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ingredientDao != null) ingredientDao.close();
        if (recipeDao != null) recipeDao.close();
        if (recipeIngredientDao != null) recipeIngredientDao.close();
    }
}