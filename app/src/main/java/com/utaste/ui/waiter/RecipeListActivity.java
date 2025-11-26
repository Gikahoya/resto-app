package com.utaste.ui.waiter;

import android.os.Bundle;import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.utaste.R;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRecipes;
    private RecipeNutritionAdapter recipeAdapter;
    private SearchView searchView;

    private RecipeDao recipeDao;
    private IngredientDao ingredientDao;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        recipeDao = new RecipeDao(this);
        ingredientDao = new IngredientDao(this);

        searchView = findViewById(R.id.searchView);
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));

        recipeAdapter = new RecipeNutritionAdapter();
        recyclerViewRecipes.setAdapter(recipeAdapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupSearchView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipesAndCalculateCalories();
    }

    private void loadRecipesAndCalculateCalories() {
        executor.execute(() -> {
            List<Recipe> allRecipes = recipeDao.getAll();
            List<RecipeNutritionAdapter.RecipeInfo> recipeInfos = new ArrayList<>();

            for (Recipe recipe : allRecipes) {
                List<RecipeDao.RecipeIngredientRow> ingredientsForRecipe = recipeDao.getIngredientsForRecipe(recipe.getId());

                double totalCalories = 0;
                for (RecipeDao.RecipeIngredientRow row : ingredientsForRecipe) {
                    Ingredient ingredient = ingredientDao.getById(row.ingredient.getId());

                    if (ingredient != null && ingredient.getNutritionFact() != null) {
                        totalCalories += ingredient.getCaloriesFor(row.quantityInGrams);
                    }
                }

                recipeInfos.add(new RecipeNutritionAdapter.RecipeInfo(recipe, totalCalories));
            }

            handler.post(() -> {
                recipeAdapter.submitList(recipeInfos);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recipeDao != null) recipeDao.close();
        if (ingredientDao != null) ingredientDao.close();
        executor.shutdown();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (recipeAdapter != null) {
                    recipeAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });
    }
}