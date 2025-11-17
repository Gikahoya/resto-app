package com.utaste.app.chef;

import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.NutritionFact;
import com.utaste.domain.recipe.RecipeNutritionEntry;
import com.utaste.domain.recipe.RecipeNutritionSummary;

import java.util.List;

/**
 * Service responsable du calcul du bilan calorique d'une recette.
 *
 * Ce service ne connaît pas :
 *   - Android
 *   - SQLite
 *   - OpenFoodFacts
 *
 * Il reçoit simplement une liste de RecipeNutritionEntry, c'est-à-dire :
 *   - un Ingredient (avec NutritionFact défini)
 *   - une quantité en grammes dans la recette.
 *
 * Formules (à mettre dans ton rapport) :
 *
 * Pour chaque ingrédient i :
 *   factor_i = quantityInGrams_i / 100
 *   carbs_i    = nf_i.carbsPer100g    * factor_i
 *   proteins_i = nf_i.proteinsPer100g * factor_i
 *   fats_i     = nf_i.fatsPer100g     * factor_i
 *
 * Totaux :
 *   totalCarbs    = Σ carbs_i
 *   totalProteins = Σ proteins_i
 *   totalFats     = Σ fats_i
 *
 * Calories totales :
 *   totalCalories = 4 * totalCarbs + 4 * totalProteins + 9 * totalFats
 */
public class NutritionCalculator {

    /**
     * Calcule le résumé nutritionnel d'une recette complète.
     *
     * @param entries liste d'entrées (ingrédient + quantité en g)
     * @return un RecipeNutritionSummary avec les totaux
     */
    public RecipeNutritionSummary compute(List<RecipeNutritionEntry> entries) {

        double totalCarbs = 0.0;
        double totalProteins = 0.0;
        double totalFats = 0.0;

        // Sécurité : si la liste est null ou vide, tout vaut 0
        if (entries == null || entries.isEmpty()) {
            return new RecipeNutritionSummary(0, 0, 0, 0);
        }

        // Parcourir tous les ingrédients de la recette
        for (RecipeNutritionEntry entry : entries) {

            Ingredient ingredient = entry.getIngredient();
            if (ingredient == null) continue;

            NutritionFact nf = ingredient.getNutritionFact();
            if (nf == null) {
                // Si pas d'infos nutritionnelles pour cet ingrédient,
                // on l'ignore dans le calcul.
                continue;
            }

            double quantityGrams = entry.getQuantityInGrams();

            // Proportion par rapport aux valeurs "pour 100 g"
            double factor = quantityGrams / 100.0;

            // Contribution de cet ingrédient aux macros totales
            totalCarbs    += nf.getCarbsPer100g()    * factor;
            totalProteins += nf.getProteinsPer100g() * factor;
            totalFats     += nf.getFatsPer100g()     * factor;
        }

        // Conversion en kilocalories (formule standard)
        double totalCalories =
                4.0 * totalCarbs +
                        4.0 * totalProteins +
                        9.0 * totalFats;

        return new RecipeNutritionSummary(
                totalCarbs,
                totalProteins,
                totalFats,
                totalCalories
        );
    }
}
