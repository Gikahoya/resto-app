package com.utaste.domain.recipe;

/**
 * Représente l'utilisation d'un ingrédient DANS UNE RECETTE,
 * uniquement pour les calculs nutritionnels.
 *
 * Exemple :
 *   - Ingrédient : Pâtes (avec NutritionFact défini)
 *   - quantityInGrams = 120
 *   => la recette utilise 120 g de pâtes.
 *
 * Cette classe ne connaît pas la base de données.
 * Elle travaille uniquement avec :
 *   - Ingredient (domaine)
 *   - quantité en grammes
 */
public class RecipeNutritionEntry {

    // Ingrédient utilisé dans la recette
    private final Ingredient ingredient;

    // Quantité utilisée dans la recette, en grammes (g)
    private final double quantityInGrams;

    /**
     * @param ingredient      ingrédient utilisé (avec éventuellement NutritionFact)
     * @param quantityInGrams quantité utilisée dans la recette (en g)
     */
    public RecipeNutritionEntry(Ingredient ingredient, double quantityInGrams) {
        this.ingredient = ingredient;
        this.quantityInGrams = Math.max(0, quantityInGrams);
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public double getQuantityInGrams() {
        return quantityInGrams;
    }

    // ---- Raccourcis pour les calculs (en utilisant Ingredient + NutritionFact) ----

    public double getCarbs() {
        return ingredient == null ? 0.0 : ingredient.getCarbsFor(quantityInGrams);
    }

    public double getProtein() {
        return ingredient == null ? 0.0 : ingredient.getProteinFor(quantityInGrams);
    }

    public double getFat() {
        return ingredient == null ? 0.0 : ingredient.getFatFor(quantityInGrams);
    }

    public double getCalories() {
        return ingredient == null ? 0.0 : ingredient.getCaloriesFor(quantityInGrams);
    }
}

