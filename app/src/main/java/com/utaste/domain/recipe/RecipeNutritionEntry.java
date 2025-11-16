package com.utaste.domain.recipe;

/**
 * Représente une "entrée" utilisée pour calculer
 * le bilan nutritionnel d'une recette.
 *
 * Cette classe ne reflète pas forcément directement la base de données.
 * C'est un petit conteneur pour les calculs :
 *
 *   - ingredient : l'ingrédient utilisé (avec ses infos nutritionnelles)
 *   - quantityInGrams : la quantité de cet ingrédient dans la recette, en grammes
 *
 * Exemple :
 *   Ingredient pasta = ...;    // pâtes avec NutritionFact rempli
 *   RecipeNutritionEntry e = new RecipeNutritionEntry(pasta, 100);
 *   // => 100 g de pâtes dans la recette
 */
public class RecipeNutritionEntry {

    // L'ingrédient utilisé dans la recette
    private final Ingredient ingredient;

    // Quantité utilisée dans la recette, en grammes
    private final double quantityInGrams;

    /**
     * Constructeur principal.
     *
     * @param ingredient      ingrédient utilisé (non null)
     * @param quantityInGrams quantité en grammes dans la recette (>= 0)
     */
    public RecipeNutritionEntry(Ingredient ingredient, double quantityInGrams) {
        this.ingredient = ingredient;
        this.quantityInGrams = quantityInGrams;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public double getQuantityInGrams() {
        return quantityInGrams;
    }
}
