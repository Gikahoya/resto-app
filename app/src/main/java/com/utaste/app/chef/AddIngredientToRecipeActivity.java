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

/**
 * Écran "Add ingredient to recipe"
 *
 * - L'utilisateur choisit une recette dans un Spinner.
 * - Il saisit le nom de l'ingrédient, la quantité (g) et éventuellement un QR code.
 * - On appelle IngredientService pour lier cet ingrédient à la recette
 *   dans la table recipe_ingredients.
 */
public class AddIngredientToRecipeActivity extends AppCompatActivity {

    private Spinner spRecipe;
    private EditText etName, etQuantity, etQr;
    private Button btnSave;
    private ImageButton btnBack;

    private RecipeDao recipeDao;
    private IngredientService ingredientService;

    /** Liste en mémoire des recettes pour le Spinner */
    private final List<Recipe> recipes = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient_to_recipe);

        // --- Bind UI ---
        spRecipe   = findViewById(R.id.spRecipe);
        etName     = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etQr       = findViewById(R.id.etQr);
        btnSave    = findViewById(R.id.btnSave);
        btnBack    = findViewById(R.id.btnBack);

        if (spRecipe == null || etName == null || etQuantity == null ||
                etQr == null || btnSave == null || btnBack == null) {
            // Si tu vois ce toast, c'est que le layout ne correspond pas aux IDs
            Toast.makeText(this,
                    "Layout error: some views are null. Vérifie activity_add_ingredient_to_recipe.xml",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // --- Services ---
        recipeDao = new RecipeDao(this);
        ingredientService = new IngredientService(this);

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Charger les recettes dans le spinner
        loadRecipesIntoSpinner();

        // Sauvegarder l'ingrédient pour la recette
        btnSave.setOnClickListener(v -> onSaveClicked());
    }

    /**
     * Charge toutes les recettes depuis la DB
     * et remplit le Spinner avec leurs noms.
     */
    private void loadRecipesIntoSpinner() {
        recipes.clear();
        recipes.addAll(recipeDao.getAll());   // Méthode à avoir dans RecipeDao

        if (recipes.isEmpty()) {
            Toast.makeText(this,
                    "No recipes found. Create a recipe first.",
                    Toast.LENGTH_SHORT).show();
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

    /**
     * Quand on clique sur "Add ingredient to recipe"
     */
    private void onSaveClicked() {
        if (recipes.isEmpty()) {
            Toast.makeText(this,
                    "No recipe available. Create a recipe first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int idx = spRecipe.getSelectedItemPosition();
        if (idx < 0 || idx >= recipes.size()) {
            Toast.makeText(this,
                    "Please select a recipe.",
                    Toast.LENGTH_SHORT).show();
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
            qr = null;   // facultatif
        }

        boolean ok;
        try {
            // On stocke toujours la quantité en grammes
            ok = ingredientService.addIngredientToRecipeFromQrByRecipeName(
                    recipeName,
                    ingredientName,
                    qr,
                    qty,
                    "g"
            );
        } catch (Exception e) {
            e.printStackTrace();
            ok = false;
        }

        if (ok) {
            Toast.makeText(this,
                    "Ingredient added to recipe.",
                    Toast.LENGTH_SHORT).show();

            // Reset du formulaire
            etName.setText("");
            etQuantity.setText("");
            etQr.setText("");
        } else {
            Toast.makeText(this,
                    "Error while saving ingredient.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ingredientService != null) {
            ingredientService.close();
        }
    }
}
