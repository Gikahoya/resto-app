package com.utaste.domain.recipe;public class NutritionFact {

    // Coefficients classiques de conversion en kcal
    public static final double KCAL_PER_GRAM_CARB    = 4.0;
    public static final double KCAL_PER_GRAM_PROTEIN = 4.0;
    public static final double KCAL_PER_GRAM_FAT     = 9.0;

    private double carbsPer100g;
    private double proteinPer100g;
    private double fatPer100g;
    private double fiberPer100g;
    private double saltPer100g;
    private double energyKcalPer100g;
    private double saturatedFatPer100g;
    private double sugarsPer100g;

    public NutritionFact() {
    }

    public NutritionFact(double carbsPer100g,
                         double proteinPer100g,
                         double fatPer100g,
                         double fiberPer100g,
                         double saltPer100g,
                         double saturatedFatPer100g,
                         double sugarsPer100g) {
        this.carbsPer100g = carbsPer100g;
        this.proteinPer100g = proteinPer100g;
        this.fatPer100g = fatPer100g;
        this.fiberPer100g = fiberPer100g;
        this.saltPer100g = saltPer100g;
        this.saturatedFatPer100g = saturatedFatPer100g;
        this.sugarsPer100g = sugarsPer100g;
    }

    public NutritionFact(double carbs100, double protein100, double fat100, double kcal100) {
        this.carbsPer100g = carbs100;
        this.proteinPer100g = protein100;
        this.fatPer100g = fat100;
        this.energyKcalPer100g = kcal100;
    }

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

    public void setCaloriesPer100g(double energyKcal100) {
        this.energyKcalPer100g = energyKcal100;
    }

    public double getEnergyKcalPer100g() {
        return energyKcalPer100g;
    }

    public double getSaturatedFatPer100g() {
        return saturatedFatPer100g;
    }

    public void setSaturatedFatPer100g(double saturatedFatPer100g) {
        this.saturatedFatPer100g = saturatedFatPer100g;
    }

    public double getSugarsPer100g() {
        return sugarsPer100g;
    }

    public void setSugarsPer100g(double sugarsPer100g) {
        this.sugarsPer100g = sugarsPer100g;
    }

    public double getCarbs()   { return carbsPer100g; }
    public double getProtein() { return proteinPer100g; }
    public double getFat()     { return fatPer100g; }
    public double getFiber()   { return fiberPer100g; }
    public double getSalt()    { return saltPer100g; }
    public double getEnergyKcal() { return energyKcalPer100g; }
    public double getSaturatedFat() { return saturatedFatPer100g; }
    public double getSugars() { return sugarsPer100g; }

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

    public double getSaturatedFatFor(double grams) {
        return grams * saturatedFatPer100g / 100.0;
    }

    public double getSugarsFor(double grams) {
        return grams * sugarsPer100g / 100.0;
    }

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
                ", energyKcalPer100g=" + energyKcalPer100g +
                ", saturatedFatPer100g=" + saturatedFatPer100g +
                ", sugarsPer100g=" + sugarsPer100g +
                '}';
    }
}