package com.utaste.service;

// Exemple dans une nouvelle classe RecipeService.java

import android.content.Context;

import com.utaste.data.sqlite.IngredientDao;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.data.sqlite.RecipeIngredientDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;
import com.utaste.domain.recipe.RecipeIngredient;


import java.util.List;

public class RecipeService {

    private final RecipeDao recipeDao;

    public RecipeService(Context context) {
        this.recipeDao = new RecipeDao(context);
    }

    public Recipe getRecipeByName(String name) {
        // Étape 1 : Obtenir la recette de base depuis le DAO.
        Recipe recipe = recipeDao.getByName(name);
        if (recipe == null) {
            return null; // La recette n'existe pas.
        }
        return recipe;
    }

    /**
     * Récupère une recette complète avec tous ses objets Ingrédient.
     * C'est ici que la "magie" opère.
     */

}
