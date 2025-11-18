package com.utaste.app.chef;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Recipe;

import java.util.ArrayList;
import java.util.List;

public class AddIngredientToRecipeActivity extends AppCompatActivity {

    private Spinner spRecipe;
    private EditText etName, etQuantity, etQr;
    private Button btnSave;
    private ImageButton btnBack;

    private RecipeDao recipeDao;
    private IngredientService ingredientService;

    /** Liste de recettes pour le Spinner */
    private final List<Recipe> recipes = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient_to_recipe);

        // ---------- Bind UI ----------
        spRecipe   = findViewById(R.id.spRecipe);
        etName     = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etQr       = findViewById(R.id.etQr);
        btnSave    = findViewById(R.id.btnSave);
        btnBack    = findViewById(R.id.btnBack);

        if (spRecipe == null || etName == null || etQuantity == null
                || etQr == null || btnSave == null || btnBack == null) {

            Toast.makeText(
                    this,
                    "Layout error: vérifie activity_add_ingredient_to_recipe.xml (IDs manquants).",
                    Toast.LENGTH_LONG
            ).show();
            // On évite de continuer si le layout n’est pas bon
            return;
        }

        // ---------- Services DB ----------
        try {
            recipeDao = new RecipeDao(this);
            ingredientService = new IngredientService(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    this,
                    "Error opening database: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
            return; // on n’a pas de DB, on arrête là pour éviter un crash
        }

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Charger les recettes dans le spinner
        loadRecipesIntoSpinner();

        // Clic sur "Add ingredient to recipe"
        btnSave.setOnClickListener(v -> onSaveClicked());
    }

    // -------------------------------------------------------------------------
    // Charger les recettes
    // -------------------------------------------------------------------------
    private void loadRecipesIntoSpinner() {
        recipes.clear();

        try {
            List<Recipe> fromDb = recipeDao.getAll();
            if (fromDb != null) {
                recipes.addAll(fromDb);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    this,
                    "Error loading recipes: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (recipes.isEmpty()) {
            Toast.makeText(
                    this,
                    "No recipes found. Create a recipe first.",
                    Toast.LENGTH_SHORT
            ).show();
        }

        List<String> names = new ArrayList<>();
        for (Recipe r : recipes) {
            names.add(r.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                names
        );
        spRecipe.setAdapter(adapter);
    }

    // -------------------------------------------------------------------------
    // Quand on clique sur "Add ingredient to recipe"
    // -------------------------------------------------------------------------
    private void onSaveClicked() {
        if (recipes.isEmpty()) {
            Toast.makeText(
                    this,
                    "No recipe available. Create a recipe first.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int idx = spRecipe.getSelectedItemPosition();
        if (idx < 0 || idx >= recipes.size()) {
            Toast.makeText(
                    this,
                    "Please select a recipe.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String recipeName     = recipes.get(idx).getName();
        String ingredientName = etName.getText().toString().trim();
        String qtyStr         = etQuantity.getText().toString().trim();
        String qr             = etQr.getText().toString().trim();

        if (ingredientName.isEmpty()) {
            etName.setError("Required");
            etName.requestFocus();
            return;
        }

        if (qtyStr.isEmpty()) {
            etQuantity.setError("Required");
            etQuantity.requestFocus();
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            etQuantity.setError("Invalid number");
            etQuantity.requestFocus();
            return;
        }

        if (qty <= 0) {
            etQuantity.setError("Must be > 0");
            etQuantity.requestFocus();
            return;
        }

        if (qr.isEmpty()) {
            qr = null; // facultatif
        }

        boolean ok;
        try {
            ok = ingredientService.addIngredientToRecipeFromQrByRecipeName(
                    recipeName,
                    ingredientName,
                    qr,
                    qty,
                    "g"
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    this,
                    "Error while saving ingredient: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
            ok = false;
        }

        if (ok) {
            Toast.makeText(
                    this,
                    "Ingredient added to recipe.",
                    Toast.LENGTH_SHORT
            ).show();

            etName.setText("");
            etQuantity.setText("");
            etQr.setText("");
        } else {
            Toast.makeText(
                    this,
                    "Error while saving ingredient.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ingredientService != null) {
            ingredientService.close();
        }
        // RecipeDao se repose sur DataBaseHelper, pas besoin de close explicitement
    }
}
