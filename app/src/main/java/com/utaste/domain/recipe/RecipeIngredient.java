package com.utaste.domain.recipe;

import com.utaste.domain.recipe.Ingredient;
import java.util.*;

/**
 * Cette classe sert à lier les ingrèdients aux recettes
 */
public class RecipeIngredient {

    // Liste statique qui contient toutes les associations (globale)
    private static List<RecipeIngredient> associations = new ArrayList<>();

    // --- Attributs ---
    private Recipe recipe;
    private Ingredient ingredient;
    private double quantity;

    // --- Constructeur ---
    public RecipeIngredient(Recipe recipe, Ingredient ingredient, double quantity) {
        this.recipe = recipe;
        this.ingredient = ingredient;
        this.quantity = quantity;
        associations.add(this); // On enregistre l'association globale ici
    }

    // --- Getters ---
    public Recipe getRecipe() { return recipe; }
    public Ingredient getIngredient() { return ingredient; }
    public double getQuantity() { return quantity; }

    // --- Méthodes de gestion des listes ---

    // Retourne tous les ingrédients d’une recette donnée
    public static List<RecipeIngredient> getByRecipe(Recipe recipe) {
        List<RecipeIngredient> list = new ArrayList<>();
        for (RecipeIngredient ri : associations) {
            if (ri.getRecipe().equals(recipe)) {
                list.add(ri);
            }
        }
        return list;
    }

    // Retourne toutes les recettes contenant un ingrédient donné
    public static List<RecipeIngredient> getByIngredient(Ingredient ingredient) {
        List<RecipeIngredient> list = new ArrayList<>();
        for (RecipeIngredient ri : associations) {
            if (ri.getIngredient().equals(ingredient)) {
                list.add(ri);
            }
        }
        return list;
    }
    // Supprime une association ingrédient-recette
    public static void removeIngredientFromRecipe(Recipe recipe, Ingredient ingredient) {
        associations.removeIf(ri ->
                ri.getRecipe().equals(recipe) && ri.getIngredient().equals(ingredient)
        );
    }

    // Supprime toutes les associations liées à une recette (ex. suppression d'une recette)
    public static void removeRecipe(Recipe recipe) {
        associations.removeIf(ri -> ri.getRecipe().equals(recipe));
    }
}
