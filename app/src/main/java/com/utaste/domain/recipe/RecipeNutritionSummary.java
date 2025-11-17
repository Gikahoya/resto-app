package com.utaste.domain.recipe;

/**
 * Résumé nutritionnel global d'une recette :
 *   - total de glucides, protéines, lipides (en g)
 *   - total de calories (en kcal)
 *
 * C'est ce que tu vas afficher dans "Recipe Caloric Balance".
 */
public class RecipeNutritionSummary {

    private final double totalCarbs;     // g
    private final double totalProtein;   // g
    private final double totalFat;       // g
    private final double totalCalories;  // kcal

    public RecipeNutritionSummary(double totalCarbs,
                                  double totalProtein,
                                  double totalFat,
                                  double totalCalories) {
        this.totalCarbs = totalCarbs;
        this.totalProtein = totalProtein;
        this.totalFat = totalFat;
        this.totalCalories = totalCalories;
    }

    public double getTotalCarbs() {
        return totalCarbs;
    }

    public double getTotalProtein() {
        return totalProtein;
    }

    public double getTotalFat() {
        return totalFat;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    // Pourcentage de calories par macro (peut servir si tu veux un graphe)
    public double getCarbCalories() {
        return totalCarbs * NutritionFact.KCAL_PER_GRAM_CARB;
    }

    public double getProteinCalories() {
        return totalProtein * NutritionFact.KCAL_PER_GRAM_PROTEIN;
    }

    public double getFatCalories() {
        return totalFat * NutritionFact.KCAL_PER_GRAM_FAT;
    }

    public double getCarbPercent() {
        return totalCalories == 0 ? 0 : (getCarbCalories() / totalCalories) * 100.0;
    }

    public double getProteinPercent() {
        return totalCalories == 0 ? 0 : (getProteinCalories() / totalCalories) * 100.0;
    }

    public double getFatPercent() {
        return totalCalories == 0 ? 0 : (getFatCalories() / totalCalories) * 100.0;
    }

    @Override
    public String toString() {
        return "RecipeNutritionSummary{" +
                "totalCarbs=" + totalCarbs +
                ", totalProtein=" + totalProtein +
                ", totalFat=" + totalFat +
                ", totalCalories=" + totalCalories +
                '}';
    }
}
