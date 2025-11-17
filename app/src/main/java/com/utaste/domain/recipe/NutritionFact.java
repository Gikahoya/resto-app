package com.utaste.domain.recipe;

/**
 * Représente les informations nutritionnelles d'un ingrédient
 * pour 100 grammes de cet ingrédient.
 *
 * On stocke uniquement ce qui est nécessaire pour le livrable :
 *   - glucides (carbohydrates)
 *   - protéines (proteins)
 *   - lipides (fats)
 *
 * Toutes les valeurs sont en GRAMMES pour 100 g d'aliment.
 *
 * Exemple d'utilisation :
 *   NutritionFact nf = new NutritionFact(20, 5, 10);
 *   // => 20 g de glucides, 5 g de protéines, 10 g de lipides pour 100 g.
 */
public class NutritionFact {

    // Glucides pour 100 g d'ingrédient
    private double carbsPer100g;

    // Protéines pour 100 g d'ingrédient
    private double proteinsPer100g;

    // Lipides pour 100 g d'ingrédient
    private double fatsPer100g;

    // Fibres pour 100g d'ingrédient
    private double fibersPer100g;

    // Sel pour 100 g d'ingrédient
    private double saltPer100g;

    /**
     * Constructeur vide (utile pour certains frameworks / sérialisation).
     */
    public NutritionFact() {
    }

    /**
     * Constructeur pratique avec tous les champs.
     *
     * @param carbsPer100g    glucides pour 100 g
     * @param proteinsPer100g protéines pour 100 g
     * @param fatsPer100g     lipides pour 100 g
     */
    public NutritionFact(double carbsPer100g,
                         double proteinsPer100g,
                         double fatsPer100g) {
        this.carbsPer100g = carbsPer100g;
        this.proteinsPer100g = proteinsPer100g;
        this.fatsPer100g = fatsPer100g;
    }

    // ----- Getters / Setters -----

    public double getCarbsPer100g() {
        return carbsPer100g;
    }

    public void setCarbsPer100g(double carbsPer100g) {
        this.carbsPer100g = carbsPer100g;
    }

    public double getProteinsPer100g() {
        return proteinsPer100g;
    }

    public void setProteinsPer100g(double proteinsPer100g) {
        this.proteinsPer100g = proteinsPer100g;
    }

    public double getFatsPer100g() {
        return fatsPer100g;
    }

    public void setFatsPer100g(double fatsPer100g) {
        this.fatsPer100g = fatsPer100g;
    }

    public double getFibersPer100g() { return fibersPer100g; }

    public void setFibersPer100g(double fibersPer100g) { this.fibersPer100g = fibersPer100g; }

    public double getSaltPer100g() { return saltPer100g; }

    public void setSaltPer100g(double saltPer100g) { this.saltPer100g = saltPer100g; }

    public void setProteinPer100g(double protein100) {
    }

    public void setFatPer100g(double fat100) {
    }

    public void setCaloriesPer100g(double energyKcal100) {
    }
}
