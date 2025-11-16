package com.utaste.domain.recipe;

/**
 * Résume le bilan nutritionnel d'une recette entière.
 *
 * On y stocke :
 *   - la quantité totale de glucides (en grammes)
 *   - la quantité totale de protéines (en grammes)
 *   - la quantité totale de lipides (en grammes)
 *   - les calories totales (en kilocalories)
 *
 * Cette classe est IMMUTABLE : les champs sont final et on
 * fournit les valeurs uniquement via le constructeur.
 */
public class RecipeNutritionSummary {

    // Quantité totale de glucides de la recette (g)
    private final double totalCarbs;

    // Quantité totale de protéines de la recette (g)
    private final double totalProteins;

    // Quantité totale de lipides de la recette (g)
    private final double totalFats;

    // Calories totales de la recette (kcal)
    private final double totalCalories;

    /**
     * Constructeur complet.
     */
    public RecipeNutritionSummary(double totalCarbs,
                                  double totalProteins,
                                  double totalFats,
                                  double totalCalories) {
        this.totalCarbs = totalCarbs;
        this.totalProteins = totalProteins;
        this.totalFats = totalFats;
        this.totalCalories = totalCalories;
    }

    public double getTotalCarbs() {
        return totalCarbs;
    }

    public double getTotalProteins() {
        return totalProteins;
    }

    public double getTotalFats() {
        return totalFats;
    }

    public double getTotalCalories() {
        return totalCalories;
    }
}
