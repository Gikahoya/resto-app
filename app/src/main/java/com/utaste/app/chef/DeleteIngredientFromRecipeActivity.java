package com.utaste.app.chef;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class DeleteIngredientFromRecipeActivity extends AppCompatActivity {

    private Spinner spRecipe;
    private Spinner spIngredient;
    private Button btnDelete;
    private ImageButton btnBack;

    private RecipeDao recipeDao;

    private final List<Recipe> recipes = new ArrayList<>();
    private final List<RecipeDao.RecipeIngredientRow> currentIngredients = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_ingredient_from_recipe);

        spRecipe     = findViewById(R.id.spRecipe);
        spIngredient = findViewById(R.id.spIngredient);
        btnDelete    = findViewById(R.id.btnDelete);
        btnBack      = findViewById(R.id.btnBack);

        recipeDao = new RecipeDao(this);

        btnBack.setOnClickListener(v -> finish());

        loadRecipesIntoSpinner();

        spRecipe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < recipes.size()) {
                    loadIngredientsForRecipe(recipes.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnDelete.setOnClickListener(v -> onDeleteClicked());
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

    private void onDeleteClicked() {
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

        Recipe selectedRecipe = recipes.get(recipeIndex);
        RecipeDao.RecipeIngredientRow row = currentIngredients.get(ingIndex);

        boolean ok = recipeDao.deleteIngredientFromRecipe(
                selectedRecipe.getId(),
                row.ingredient.getId()
        );

        if (ok) {
            Toast.makeText(this,
                    "Ingredient removed from recipe.",
                    Toast.LENGTH_SHORT).show();
            // rafraîchir la liste
            loadIngredientsForRecipe(selectedRecipe);
        } else {
            Toast.makeText(this,
                    "Error while deleting ingredient.",
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
