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
    private Ingredient.Unit unit;

    // --- Constructeur ---
    public RecipeIngredient(Recipe recipe, Ingredient ingredient, double quantity, Ingredient.Unit unit) {
        this.recipe = recipe;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = (unit == null ? Ingredient.Unit.PIECE : unit);
        associations.add(this); // On enregistre l'association globale ici
    }

    // --- Getters ---
    public Recipe getRecipe() { return recipe; }
    public Ingredient getIngredient() { return ingredient; }
    public double getQuantity() { return quantity; }
    public Ingredient.Unit getUnit() { return unit; }

    // --- Setters ---
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(Ingredient.Unit unit) { this.unit = unit; }

}
