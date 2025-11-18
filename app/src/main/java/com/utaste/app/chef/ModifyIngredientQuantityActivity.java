package com.utaste.app.chef;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.Locale;

public class ModifyIngredientQuantityActivity extends AppCompatActivity {

    private Spinner spRecipe;
    private Spinner spIngredient;
    private EditText etQuantity;
    private Button btnUpdate;
    private ImageButton btnBack;

    private RecipeDao recipeDao;

    // Données en mémoire
    private final List<Recipe> recipes = new ArrayList<>();
    private final List<RecipeDao.RecipeIngredientRow> currentIngredients = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_ingredient_quantity);

        spRecipe     = findViewById(R.id.spRecipe);
        spIngredient = findViewById(R.id.spIngredient);
        etQuantity   = findViewById(R.id.etQuantity);
        btnUpdate    = findViewById(R.id.btnUpdate);
        btnBack      = findViewById(R.id.btnBack);

        recipeDao = new RecipeDao(this);

        btnBack.setOnClickListener(v -> finish());

        loadRecipesIntoSpinner();

        spRecipe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < recipes.size()) {
                    Recipe r = recipes.get(position);
                    loadIngredientsForRecipe(r);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // rien
            }
        });

        btnUpdate.setOnClickListener(v -> onUpdateClicked());
    }

    private void loadRecipesIntoSpinner() {
        recipes.clear();
        recipes.addAll(recipeDao.getAll());

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

    private void loadIngredientsForRecipe(Recipe recipe) {
        currentIngredients.clear();
        currentIngredients.addAll(
                recipeDao.getIngredientsForRecipe(recipe.getId())
        );

        List<String> ingNames = new ArrayList<>();
        for (RecipeDao.RecipeIngredientRow row : currentIngredients) {
            ingNames.add(row.ingredient.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                ingNames
        );
        spIngredient.setAdapter(adapter);

        if (currentIngredients.isEmpty()) {
            Toast.makeText(this,
                    "This recipe has no ingredients.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void onUpdateClicked() {
        if (recipes.isEmpty()) {
            Toast.makeText(this,
                    "No recipe available.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int recipeIndex = spRecipe.getSelectedItemPosition();
        if (recipeIndex < 0 || recipeIndex >= recipes.size()) {
            Toast.makeText(this,
                    "Please select a recipe.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentIngredients.isEmpty()) {
            Toast.makeText(this,
                    "This recipe has no ingredients.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int ingIndex = spIngredient.getSelectedItemPosition();
        if (ingIndex < 0 || ingIndex >= currentIngredients.size()) {
            Toast.makeText(this,
                    "Please select an ingredient.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String qtyStr = etQuantity.getText().toString().trim();
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

        Recipe selectedRecipe = recipes.get(recipeIndex);
        RecipeDao.RecipeIngredientRow row = currentIngredients.get(ingIndex);

        boolean ok = recipeDao.updateIngredientQuantityForRecipe(
                selectedRecipe.getId(),
                row.ingredient.getId(),
                qty
        );

        if (ok) {
            Toast.makeText(this,
                    String.format(Locale.getDefault(),
                            "Quantity updated to %.1f g", qty),
                    Toast.LENGTH_SHORT).show();
            etQuantity.setText("");
        } else {
            Toast.makeText(this,
                    "Error while updating quantity.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recipeDao != null) {
            recipeDao.close();
        }
    }
}
