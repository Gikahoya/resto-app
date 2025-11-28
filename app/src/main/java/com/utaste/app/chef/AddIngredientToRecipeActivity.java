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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddIngredientToRecipeActivity extends AppCompatActivity {

    // Vues
    private Spinner spRecipe;
    private TextView tvIngredientName; // MODIFIÉ: C'est maintenant un TextView
    private EditText etQuantity;
    private Spinner spUnit;
    private Button btnSave;

    // DAOs
    private RecipeDao recipeDao;
    private IngredientDao ingredientDao;
    private RecipeIngredientDao recipeIngredientDao;

    // Données
    private List<Recipe> allRecipes;
    private Ingredient currentIngredient;
    private int ingredientId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient_to_recipe);

        // Récupérer l'ID de l'ingrédient depuis l'intent
        ingredientId = getIntent().getIntExtra("INGREDIENT_ID", -1);
        if (ingredientId == -1) {
            Toast.makeText(this, "Error: Ingredient not specified.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initDaos();
        initViews();
        setupListeners();
        loadData();
    }

    private void initDaos() {
        recipeDao = new RecipeDao(this);
        ingredientDao = new IngredientDao(this);
        recipeIngredientDao = new RecipeIngredientDao(this);
    }

    private void initViews() {
        spRecipe = findViewById(R.id.spRecipe);
        tvIngredientName = findViewById(R.id.tvIngredientName); // MODIFIÉ
        etQuantity = findViewById(R.id.etQuantity);
        spUnit = findViewById(R.id.spUnit);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveLink());
    }

    private void loadData() {
        new Thread(() -> {
            allRecipes = recipeDao.getAll();
            currentIngredient = ingredientDao.getById(ingredientId);

            List<String> recipeNames = allRecipes.stream()
                    .map(Recipe::getName)
                    .collect(Collectors.toCollection(ArrayList::new));

            runOnUiThread(() -> {
                if (currentIngredient == null) {
                    Toast.makeText(this, "Could not load ingredient.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Afficher le nom de l'ingrédient
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

    private void saveLink() {
        // Valider les entrées
        if (spRecipe.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a recipe.", Toast.LENGTH_SHORT).show();
            return;
        }
        String recipeName = spRecipe.getSelectedItem().toString();
        String quantityStr = etQuantity.getText().toString().trim();
        Ingredient.Unit selectedUnit = (Ingredient.Unit) spUnit.getSelectedItem();

        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError("Quantity is required.");
            return;
        }

        // Trouver l'ID de la recette
        long recipeId = getRecipeIdByName(recipeName);
        if (recipeId == -1) {
            Toast.makeText(this, "Error finding selected recipe.", Toast.LENGTH_SHORT).show();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
        } catch (NumberFormatException e) {
            etQuantity.setError("Invalid number.");
            return;
        }

        // Sauvegarder en base de données
        new Thread(() -> {
            recipeIngredientDao.insertOrUpdate(recipeId, ingredientId, quantity, selectedUnit.name());
            runOnUiThread(() -> {
                Toast.makeText(this, "Ingredient linked successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private long getRecipeIdByName(String name) {
        for (Recipe r : allRecipes) {
            if (r.getName().equals(name)) {
                return r.getId();
            }
        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recipeDao != null) recipeDao.close();
        if (ingredientDao != null) ingredientDao.close();
        if (recipeIngredientDao != null) recipeIngredientDao.close();
    }
}