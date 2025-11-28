package com.utaste.domain.recipe;

import androidx.annotation.NonNull;

public class Ingredient {

    public enum Unit {
        // Masses
        GRAMME("g"),

        // Volumes
        LITRE("L"),
        MILLILITRE("mL"),

        // Comptage / divers
        PIECE("pc"); // MODIFIÉ pour "pc"

        private final String symbol;

        Unit(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public static Unit fromDb(String value) {
            if (value == null) return PIECE;
            try {
                return Unit.valueOf(value);
            } catch (Exception e) {
                return PIECE;
            }
        }

        public static String toDb(Unit unit) {
            return unit == null ? PIECE.name() : unit.name();
        }

        public static Unit fromString(String s) {
            if (s == null) return PIECE;
            String k = s.trim().toLowerCase();
            switch (k) {
                case "g":
                case "gramme":
                case "grammes":
                    return GRAMME;
                case "l":
                case "litre":
                case "litres":
                    return LITRE;
                case "ml":
                case "millilitre":
                case "millilitres":
                    return MILLILITRE;
                default:
                    return PIECE;
            }
        }

        public static String format(double amount, Unit unit) {
            String u = unit == null ? PIECE.getSymbol() : unit.getSymbol();
            // Utilise %.0f pour les entiers et %.1f pour les décimaux pour un affichage propre
            if (amount == (long) amount) {
                return String.format("%d %s", (long) amount, u);
            } else {
                return String.format("%.1f %s", amount, u);
            }
        }
    }

    private int id;
    private String name;
    private String qrCode;
    private long createdAt;
    private long updatedAt;
    private Unit unit;
    private NutritionFact nutritionFact;

    public Ingredient() {}

    public Ingredient(String name, String qrCode) {
        this.name = name;
        this.qrCode = qrCode;
    }

    // --- Getters et Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = (name == null ? null : name.trim()); }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
    public NutritionFact getNutritionFact() { return nutritionFact; }
    public void setNutritionFact(NutritionFact nutritionFact) { this.nutritionFact = nutritionFact; }


    // ====================================================================
    //  MÉTHODES DE CALCUL NUTRITIONNEL AJOUTÉES POUR CORRIGER L'ERREUR
    // ====================================================================

    /**
     * Calcule la quantité de glucides pour une masse donnée.
     * @param grams La masse en grammes.
     * @return La quantité de glucides.
     */
    public double getCarbsFor(double grams) {
        return nutritionFact == null ? 0.0 : nutritionFact.getCarbsFor(grams);
    }

    /**
     * Calcule la quantité de protéines pour une masse donnée.
     * @param grams La masse en grammes.
     * @return La quantité de protéines.
     */
    public double getProteinFor(double grams) {
        return nutritionFact == null ? 0.0 : nutritionFact.getProteinFor(grams);
    }

    /**
     * Calcule la quantité de lipides pour une masse donnée.
     * @param grams La masse en grammes.
     * @return La quantité de lipides.
     */
    public double getFatFor(double grams) {
        return nutritionFact == null ? 0.0 : nutritionFact.getFatFor(grams);
    }

    /**
     * Calcule le nombre de calories pour une masse donnée.
     * @param grams La masse en grammes.
     * @return Le nombre de calories.
     */
    public double getCaloriesFor(double grams) {
        return nutritionFact == null ? 0.0 : nutritionFact.getCaloriesFor(grams);
    }

    @NonNull
    @Override
    public String toString() {
        return name != null ? name : super.toString();
    }
}