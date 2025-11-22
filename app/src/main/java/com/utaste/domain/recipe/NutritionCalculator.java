package com.utaste.domain.recipe;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.List;

/**
 * Classe utilitaire statique pour calculer le bilan nutritionnel
 * d'une recette à partir d'une liste de RecipeNutritionEntry.
 *
 * Elle ne dépend pas d'Android ni de SQLite, uniquement de ton
 * modèle de domaine.
 */
public final class NutritionCalculator {

    // Classe utilitaire => constructeur privé
    private NutritionCalculator() {
    }

    /**
     * Calcule le résumé nutritionnel global d'une recette.
     *
     * @param entries liste d'entrées (ingrédient + quantité en g)
     * @return résumé contenant total glucides / protéines / lipides / calories
     */
    @NonNull
    @Contract("null -> new")
    public static RecipeNutritionSummary computeSummary(List<RecipeNutritionEntry> entries) {
        double totalCarbs = 0.0;
        double totalProtein = 0.0;
        double totalFat = 0.0;
        double totalCalories = 0.0;

        if (entries != null) {
            for (RecipeNutritionEntry entry : entries) {
                if (entry == null) continue;

                totalCarbs += entry.getCarbs();
                totalProtein += entry.getProtein();
                totalFat += entry.getFat();
                totalCalories += entry.getCalories();
            }
        }

        return new RecipeNutritionSummary(
                totalCarbs,
                totalProtein,
                totalFat,
                totalCalories
        );
    }
}
