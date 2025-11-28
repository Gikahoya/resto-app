package com.utaste.app.chef;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.utaste.R;
// CORRECTION: Import de l'adapter que nous allons créer/utiliser.
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Recipe;
import com.utaste.ui.recipe.CreateRecipeActivity;

import java.util.ArrayList;
import java.util.List;

public class ManageRecipeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRecipes;
    // CORRECTION: La variable doit être de type Adapter pour fonctionner avec un RecyclerView.
    private RecipeAdminAdapter recipeAdapter;
    private List<Recipe> allRecipes;
    private RecipeDao recipeDao;
    private SearchView searchView;
    private ImageButton btnBack;
    private FloatingActionButton fabCreateRecipe;
    private TextView tvEmptyList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_recipe);

        // Initialisation des vues
        initViews();

        // Initialisation de la base de données
        recipeDao = new RecipeDao(this);

        // Configuration des listeners
        setupListeners();

        // Configuration du RecyclerView
        setupRecyclerView();

        // Le chargement initial se fera dans onResume()
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les recettes lorsque l'activité redevient visible
        // (par exemple, après avoir créé ou modifié une recette)
        loadRecipes();
        if (searchView != null) {
            searchView.setQuery("", false); // Efface la recherche précédente
            searchView.clearFocus();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        searchView = findViewById(R.id.searchView);
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        fabCreateRecipe = findViewById(R.id.fabCreateRecipe);
        tvEmptyList = findViewById(R.id.tvEmptyList);
    }

    private void setupListeners() {
        // Bouton Retour
        btnBack.setOnClickListener(v -> finish());

        // Bouton flottant pour créer une recette
        fabCreateRecipe.setOnClickListener(v -> {
            // Lance l'activité pour créer une nouvelle recette.
            // Le layout activity_create_recipe.xml correspond bien à une activité de création/modification.
            Intent intent = new Intent(ManageRecipeActivity.this, CreateRecipeActivity.class);
            startActivity(intent);
        });

        // Logique de la barre de recherche
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRecipes(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecipes(newText);
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));
        // L'adaptateur sera initialisé et attaché dans loadRecipes()
    }

// Dans ManageRecipeActivity.java

    private void loadRecipes() {
        // CORRECTION: Utilisation de la bonne méthode `getAll()` du RecipeDao
        allRecipes = recipeDao.getAll();

        if (allRecipes == null) {
            allRecipes = new ArrayList<>();
        }

        // --- BLOC CORRIGÉ ---

        // 1. On vérifie si l'adaptateur existe déjà pour simplement le mettre à jour.
        if (recipeAdapter == null) {
            // Création et configuration de l'adaptateur en passant le contexte (this)
            recipeAdapter = new RecipeAdminAdapter(this, allRecipes);
            recyclerViewRecipes.setAdapter(recipeAdapter);
        } else {
            // 2. Si l'adaptateur existe, on met juste à jour sa liste. C'est plus efficace.
            recipeAdapter.filterList(allRecipes);
        }

        // La logique de clic est maintenant gérée DANS RecipeAdminAdapter,
        // donc setOnItemClickListener n'est plus nécessaire ici.

        checkIfListIsEmpty();
    }

    private void filterRecipes(String text) {
        List<Recipe> filteredList = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (recipe.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(recipe);
            }
        }
        // CORRECTION: La méthode `filterList` doit exister dans l'Adapter.
        recipeAdapter.filterList(filteredList);
        checkIfListIsEmpty();
    }

    private void checkIfListIsEmpty() {
        // CORRECTION: On vérifie si l'adapter est non-nul avant de l'utiliser.
        if (recipeAdapter == null || recipeAdapter.getItemCount() == 0) {
            recyclerViewRecipes.setVisibility(View.GONE);
            tvEmptyList.setVisibility(View.VISIBLE);
        } else {
            recyclerViewRecipes.setVisibility(View.VISIBLE);
            tvEmptyList.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // C'est une bonne pratique de fermer la connexion au DAO.
        if (recipeDao != null) {
            recipeDao.close();
        }
    }
}
