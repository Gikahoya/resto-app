package com.utaste.domain.recipe;

import androidx.annotation.NonNull;

/**
 * Modèle de domaine pour un ingrédient.
 *
 * Remarque :
 * - La quantité (amount + unit) est maintenant gérée dans RecipeIngredient.
 * - Pour le calcul des calories d'une RECETTE, on utilisera plutôt
 *   RecipeNutritionEntry qui stocke la quantité utilisée dans une recette.
 */
public class Ingredient {

    // ==== Unités regroupées par type ====
    public enum Unit {
        // Masses
        GRAMME("g"),

        // Volumes
        LITRE("L"),
        MILLILITRE("mL"),

        // Comptage / divers
        PIECE("pc");      // 1 pc, 2 pcs...

        private final String symbol;

        Unit(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        // ---- Conversions utiles ----

        /** Valeur lue depuis la DB -> enum Unit (avec valeur par défaut PIECE). */
        public static Unit fromDb(String value) {
            if (value == null) return PIECE;
            try {
                return Unit.valueOf(value);
            } catch (Exception e) {
                return PIECE;
            }
        }

        /** Enum Unit -> valeur stockée en DB (name()). */
        public static String toDb(Unit unit) {
            return unit == null ? PIECE.name() : unit.name();
        }

        /** Conversion texte utilisateur -> enum (tolère casse/pluriels usuels). */
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
                    return MILLILITRE;
                default:
                    return PIECE;
            }
        }

        /** Formatage simple : "250 g", "1.5 L", "3 pcs"... */
        public static String format(double amount, Unit unit) {
            String u = unit == null ? PIECE.getSymbol() : unit.getSymbol();
            return (Math.floor(amount) == amount)
                    ? String.format("%.0f %s", amount, u)
                    : String.format("%.2f %s", amount, u);
        }
    }

    // ==== Champs "de base" (déjà existants) ====
    private int id;              // PK SQLite AUTOINCREMENT
    private String name;         // nom d'ingrédient
    private String qrCode;       // peut être null
    private long createdAt;      // epoch millis
    private long updatedAt;      // epoch millis
    private Unit unit;


    // ==== Nouveau : informations nutritionnelles par 100 g ====
    private NutritionFact nutritionFact;

    // ==== Constructeurs ====
    public Ingredient() {
    }

    public Ingredient(String name, String qrCode) {
        this.name = name;
        this.qrCode = qrCode;
    }

    // ==== Getters / Setters de base ====
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name == null ? null : name.trim());
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ==== Helpers "généraux" ====
    @NonNull

    public static String unitToDb(Unit unit) {
        return Unit.toDb(unit);
    }

    public static Unit unitFromDb(String dbValue) {
        return Unit.fromDb(dbValue);
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }


    // ==== Partie Nutrition (nouveau) ======================================

    public NutritionFact getNutritionFact() {
        return nutritionFact;
    }

    public void setNutritionFact(NutritionFact nutritionFact) {
        this.nutritionFact = nutritionFact;
    }

    public double getCarbsFor(double grams) {
        return nutritionFact == null ? 0.0 : nutritionFact.getCarbsFor(grams);
    }

    public double getProteinFor(double grams) {
        return nutritionFact == null ? 0.0 : nutritionFact.getProteinFor(grams);
    }

    public double getFatFor(double grams) {
        return nutritionFact == null ? 0.0 : nutritionFact.getFatFor(grams);
    }

    public double getCaloriesFor(double grams) {
        return nutritionFact == null ? 0.0 : nutritionFact.getCaloriesFor(grams);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", nutritionFact=" + nutritionFact +
                '}';
    }
}
