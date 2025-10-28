package com.utaste.domain.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cette classe sert à gérer les recettes; création et suppression
 */

public class RecipeRepository {

    private final List<Recipe> recipes = new ArrayList<>();

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipes); // Retourne une copie pour éviter les modifications externes
    }

    public Optional<Recipe> findRecipeByName(String name) {
        return recipes.stream()
                .filter(recipe -> recipe.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public void addRecipe(Recipe recipe) {
        if (findRecipeByName(recipe.getName()).isPresent()) {
            throw new IllegalArgumentException("Recipe with this name already exists");
        }
        recipes.add(recipe);
    }

    /**
     * Supprime une recette et toutes ses associations avec des ingrédients.
     * @param recipe La recette à supprimer.
     */
    public void deleteRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }
        // 1. Supprimer toutes les associations d'ingrédients pour cette recette
        RecipeIngredient.removeRecipe(recipe);

        // 2. Supprimer la recette de la liste principale
        recipes.removeIf(r -> r.equals(recipe));
    }


}