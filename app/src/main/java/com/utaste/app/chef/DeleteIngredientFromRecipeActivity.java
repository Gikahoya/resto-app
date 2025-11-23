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
import com.utaste.domain.recipe.RecipeIngredient;
import com.utaste.service.RecipeIngredientService;

import java.util.ArrayList;
import java.util.List;

/**
 * Activité permettant de supprimer un ingrédient d'une recette.
 * Elle utilise le RecipeIngredientService.
 */
public class DeleteIngredientFromRecipeActivity extends AppCompatActivity {

    // --- Interface ---
    private Spinner spRecipe;
    private Spinner spIngredient;
    private Button btnDelete;
    private ImageButton btnBack;

    // --- Services & Données ---
    private RecipeDao recipeDao; // Juste pour lire la liste des recettes
    private RecipeIngredientService recipeIngredientService; // Pour gérer les liaisons

    // Listes pour garder en mémoire les objets affichés dans les Spinners
    private final List<Recipe> recipes = new ArrayList<>();
    private final List<RecipeIngredient> currentIngredients = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_ingredient_from_recipe);

        // 1. Liaison Vues
        spRecipe     = findViewById(R.id.spRecipe);
        spIngredient = findViewById(R.id.spIngredient);
        btnDelete    = findViewById(R.id.btnDelete);
        btnBack      = findViewById(R.id.btnBack);

        // 2. Initialisation Services
        recipeDao = new RecipeDao(this);
        recipeIngredientService = new RecipeIngredientService(this);

        // 3. Configuration initiale
        btnBack.setOnClickListener(v -> finish());
        loadRecipesIntoSpinner();

        // 4. Listener sur la sélection d'une recette
        // Quand on change de recette, on recharge la liste de ses ingrédients
        spRecipe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < recipes.size()) {
                    loadIngredientsForRecipe(recipes.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Rien à faire
            }
        });

        // 5. Action supprimer
        btnDelete.setOnClickListener(v -> onDeleteClicked());
    }

    /**
     * Charge toutes les recettes disponibles dans le premier Spinner.
     */
    private void loadRecipesIntoSpinner() {
        recipes.clear();
        recipes.addAll(recipeDao.getAll());

        if (recipes.isEmpty()) {
            Toast.makeText(this, "No recipes found. Create a recipe first.", Toast.LENGTH_SHORT).show();
        }

        // Création de la liste de noms pour l'affichage
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
     * Charge les ingrédients liés à la recette sélectionnée dans le second Spinner.
     */
    private void loadIngredientsForRecipe(Recipe recipe) {
        currentIngredients.clear();

        // Utilisation du Service pour récupérer les vrais objets RecipeIngredient
        currentIngredients.addAll(
                recipeIngredientService.getIngredientsByRecipeId(recipe.getId())
        );

        List<String> ingNames = new ArrayList<>();
        for (RecipeIngredient ri : currentIngredients) {
            // On affiche : "Farine (500 g)" par exemple, ou juste le nom
            String displayName = ri.getIngredient().getName() + " (" + ri.getQuantity() + " " + ri.getUnit() + ")";
            ingNames.add(displayName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                ingNames
        );
        spIngredient.setAdapter(adapter);

        if (currentIngredients.isEmpty()) {
            Toast.makeText(this, "This recipe has no ingredients.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Logique de suppression.
     */
    private void onDeleteClicked() {
        // Vérifications de sécurité
        if (recipes.isEmpty()) return;

        int recipeIndex = spRecipe.getSelectedItemPosition();
        if (recipeIndex < 0 || recipeIndex >= recipes.size()) {
            return;
        }

        if (currentIngredients.isEmpty()) {
            Toast.makeText(this, "No ingredients to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        int ingIndex = spIngredient.getSelectedItemPosition();
        if (ingIndex < 0 || ingIndex >= currentIngredients.size()) {
            Toast.makeText(this, "Please select an ingredient.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupération des objets sélectionnés
        Recipe selectedRecipe = recipes.get(recipeIndex);
        RecipeIngredient selectedLink = currentIngredients.get(ingIndex);

        // Appel au Service pour supprimer le lien
        // On a besoin de l'ID de la recette et de l'ID de l'ingrédient
        boolean ok = recipeIngredientService.removeIngredientFromRecipe(
                selectedRecipe.getId(),
                selectedLink.getIngredient().getId()
        );

        if (ok) {
            Toast.makeText(this, "Ingredient removed from recipe.", Toast.LENGTH_SHORT).show();
            // On rafraîchit la liste pour voir que l'ingrédient a disparu
            loadIngredientsForRecipe(selectedRecipe);
        } else {
            Toast.makeText(this, "Error while deleting ingredient.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fermeture propre
        if (recipeDao != null) recipeDao.close();
        if (recipeIngredientService != null) recipeIngredientService.close();
    }
}