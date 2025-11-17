package com.utaste.domain.recipe;

/**
 * Représente les informations nutritionnelles d'un ingrédient
 * pour 100 g de produit.
 *
 * On stocke ici :
 *  - glucides  / 100 g  (carbsPer100g)
 *  - protéines / 100 g  (proteinPer100g)
 *  - lipides   / 100 g  (fatPer100g)
 *  - fibres    / 100 g  (fiberPer100g)  [optionnel]
 *  - sel       / 100 g  (saltPer100g)   [optionnel]
 *
 * On fournit aussi des helpers pour calculer :
 *  - les nutriments pour X g d'ingrédient
 *  - les calories (kcal) pour X g d'ingrédient
 */
public class NutritionFact {

    // Coefficients classiques de conversion en kcal
    public static final double KCAL_PER_GRAM_CARB    = 4.0;
    public static final double KCAL_PER_GRAM_PROTEIN = 4.0;
    public static final double KCAL_PER_GRAM_FAT     = 9.0;

    // Valeurs pour 100 g de produit
    private double carbsPer100g;    // glucides
    private double proteinPer100g;  // protides
    private double fatPer100g;      // lipides
    private double fiberPer100g;    // fibres
    private double saltPer100g;     // sel

    public NutritionFact() {
    }

    public NutritionFact(double carbsPer100g,
                         double proteinPer100g,
                         double fatPer100g,
                         double fiberPer100g,
                         double saltPer100g) {
        this.carbsPer100g = carbsPer100g;
        this.proteinPer100g = proteinPer100g;
        this.fatPer100g = fatPer100g;
        this.fiberPer100g = fiberPer100g;
        this.saltPer100g = saltPer100g;
    }

    public NutritionFact(double carbs100, double protein100, double fat100, double kcal100) {
    }

    // ===== Getters / Setters =====

    public double getCarbsPer100g() {
        return carbsPer100g;
    }

    public void setCarbsPer100g(double carbsPer100g) {
        this.carbsPer100g = carbsPer100g;
    }

    public double getProteinPer100g() {
        return proteinPer100g;
    }

    public void setProteinPer100g(double proteinPer100g) {
        this.proteinPer100g = proteinPer100g;
    }

    public double getFatPer100g() {
        return fatPer100g;
    }

    public void setFatPer100g(double fatPer100g) {
        this.fatPer100g = fatPer100g;
    }

    public double getFiberPer100g() {
        return fiberPer100g;
    }

    public void setFiberPer100g(double fiberPer100g) {
        this.fiberPer100g = fiberPer100g;
    }

    public double getSaltPer100g() {
        return saltPer100g;
    }

    public void setSaltPer100g(double saltPer100g) {
        this.saltPer100g = saltPer100g;
    }

    // ===== Calculs pour une quantité donnée (en grammes) =====

    public double getCarbsFor(double grams) {
        return grams * carbsPer100g / 100.0;
    }

    public double getProteinFor(double grams) {
        return grams * proteinPer100g / 100.0;
    }

    public double getFatFor(double grams) {
        return grams * fatPer100g / 100.0;
    }

    public double getFiberFor(double grams) {
        return grams * fiberPer100g / 100.0;
    }

    public double getSaltFor(double grams) {
        return grams * saltPer100g / 100.0;
    }

    /**
     * Calories totales pour "grams" grammes de cet ingrédient.
     *
     * On ne compte que glucides + protéines + lipides,
     * en utilisant les coefficients standard.
     */
    public double getCaloriesFor(double grams) {
        double carbs   = getCarbsFor(grams);
        double protein = getProteinFor(grams);
        double fat     = getFatFor(grams);

        return carbs   * KCAL_PER_GRAM_CARB
                + protein * KCAL_PER_GRAM_PROTEIN
                + fat     * KCAL_PER_GRAM_FAT;
    }

    @Override
    public String toString() {
        return "NutritionFact{" +
                "carbsPer100g=" + carbsPer100g +
                ", proteinPer100g=" + proteinPer100g +
                ", fatPer100g=" + fatPer100g +
                ", fiberPer100g=" + fiberPer100g +
                ", saltPer100g=" + saltPer100g +
                '}';
    }

    public void setCaloriesPer100g(double energyKcal100) {
    }
}
