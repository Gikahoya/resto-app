package com.utaste.app.chef;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.data.sqlite.RecipeIngredientDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ModifyIngredientQuantityActivity extends AppCompatActivity {

    // Vues
    private TextView tvIngredientName;
    private Spinner spRecipe;
    private EditText etQuantity;
    private Spinner spUnit;
    private Button btnSave;

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
        setContentView(R.layout.activity_modify_ingredient_quantity);

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
        etQuantity = findViewById(R.id.etQuantity);
        spUnit = findViewById(R.id.spUnit);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveModification());
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

                // Configurer le spinner des unités
                ArrayAdapter<Ingredient.Unit> unitAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item, Ingredient.Unit.values());
                spUnit.setAdapter(unitAdapter);
            });
        }).start();
    }

    private void saveModification() {
        // Valider les entrées
        if (spRecipe.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a recipe.", Toast.LENGTH_SHORT).show();
            return;
        }
        String recipeName = spRecipe.getSelectedItem().toString();
        String quantityStr = etQuantity.getText().toString().trim();
        Ingredient.Unit selectedUnit = (Ingredient.Unit) spUnit.getSelectedItem();

        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError("New quantity is required.");
            return;
        }

        // Trouver l'ID de la recette
        long recipeId = -1;
        for(RecipeDao.RecipeIngredientRow row : recipesContainingIngredient) {
            if (row.recipe.getName().equals(recipeName)) {
                recipeId = row.recipe.getId();
                break;
            }
        }
        if (recipeId == -1) {
            Toast.makeText(this, "Error finding selected recipe.", Toast.LENGTH_SHORT).show();
            return;
        }

        double newQuantity;
        try {
            newQuantity = Double.parseDouble(quantityStr);
        } catch (NumberFormatException e) {
            etQuantity.setError("Invalid number.");
            return;
        }

        // Sauvegarder la modification en base de données
        long finalRecipeId = recipeId;
        new Thread(() -> {
            boolean success = recipeIngredientDao.updateLink(finalRecipeId, ingredientId, newQuantity, selectedUnit.name());
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this,
                            String.format(Locale.ENGLISH, "Quantity for %s in %s modified to %.1f %s.",
                                    currentIngredient.getName(), recipeName, newQuantity, selectedUnit.getSymbol()),
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update quantity.", Toast.LENGTH_SHORT).show();
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