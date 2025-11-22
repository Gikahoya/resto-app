package com.utaste.domain.recipe;

import android.content.Context;

import com.utaste.data.sqlite.RecipeDao;

import java.util.List;
import java.util.Optional;

/**
 * Cette classe sert à gérer les recettes; création et suppression
 */
public class RecipeRepository {

    private final RecipeDao recipeDao;

    public RecipeRepository(Context context) {
        this.recipeDao = new RecipeDao(context);
    }

    public List<Recipe> getAllRecipes() {
        return recipeDao.getAll();
    }

    public Optional<Recipe> findRecipeByName(String name) {
        return Optional.ofNullable(recipeDao.findByName(name));
    }

    public void addRecipe(Recipe recipe) {
        if (findRecipeByName(recipe.getName()).isPresent()) {
            throw new IllegalArgumentException("Recipe with this name already exists");
        }
        recipeDao.insertIfAbsent(recipe.getName(), recipe.getDescription(), recipe.getImagePath());
    }

    /**
     * Supprime une recette et toutes ses associations avec des ingrédients.
     * @param recipe La recette à supprimer.
     */
    public void deleteRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }
        recipeDao.deleteByName(recipe.getName());
    }

    public void close() {
        recipeDao.close();
    }
}
