package com.utaste.domain.recipe;

import androidx.annotation.NonNull;

public class Ingredient {
    // ==== Unités regroupées par type ====
    public enum Unit {
        // Longueurs
        METRE("m"),
        CENTIMETRE("cm"),
        MILLIMETRE("mm"),

        // Masses
        KILOGRAMME("kg"),
        GRAMME("g"),
        MILLIGRAMME("mg"),

        // Volumes
        LITRE("L"),
        MILLILITRE("mL"),

        // Comptage / divers
        PIECE("pc"),        // 1 pc, 2 pcs...
        PAQUET("paquet");   // 1 paquet, 2 paquets

        private final String symbol;

        Unit(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        // ---- Conversions utiles ----
        public static Unit fromDb(String value) {
            if (value == null) return PIECE;
            try { return Unit.valueOf(value); } catch (Exception e) { return PIECE; }
        }

        public static String toDb(Unit unit) {
            return unit == null ? PIECE.name() : unit.name();
        }

        /** Conversion texte utilisateur -> enum (tolère casse/pluriels usuels) */
        public static Unit fromString(String s) {
            if (s == null) return PIECE;
            String k = s.trim().toLowerCase();
            switch (k) {
                case "m": case "metre": case "mètre": return METRE;
                case "cm": case "centimetre": case "centimètre": return CENTIMETRE;
                case "mm": case "millimetre": case "millimètre": return MILLIMETRE;
                case "kg": case "kilogramme": return KILOGRAMME;
                case "g": case "gramme": case "grammes": return GRAMME;
                case "mg": case "milligramme": return MILLIGRAMME;
                case "l": case "litre": case "litres": return LITRE;
                case "ml": case "millilitre": return MILLILITRE;
                case "paquet": case "paquets": case "pack": case "packs": return PAQUET;
                default: return PIECE;
            }
        }

        /** Formatage simple : "250 g", "1.5 L", "3 pcs" */
        public static String format(double amount, Unit unit) {
            String u = unit == null ? PIECE.getSymbol() : unit.getSymbol();
            return (Math.floor(amount) == amount)
                    ? String.format("%.0f %s", amount, u)
                    : String.format("%.2f %s", amount, u);
        }
    }

    // ==== Champs ====
    private int id;              // PK SQLite AUTOINCREMENT
    private String name;         // nom d'ingrédient
    private String qrCode;       // peut être null
    private double amount;       // quantité numérique
    private Unit unit;           // unité associée à amount
    private long createdAt;      // epoch millis
    private long updatedAt;      // epoch millis

    // ==== Constructeurs ====
    public Ingredient() {}

    public Ingredient(String name, String qrCode, double amount, Unit unit) {
        this.name = name;
        this.qrCode = qrCode;
        this.amount = amount;
        this.unit = (unit == null ? Unit.PIECE : unit);
    }

    // ==== Getters / Setters ====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = (name == null ? null : name.trim());
    }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // ==== Helpers ====
    @NonNull
    public String getDisplayQuantity() {
        return Unit.format(amount, unit);
    }

    public static String unitToDb(Unit unit) { return Unit.toDb(unit); }
    public static Unit unitFromDb(String dbValue) { return Unit.fromDb(dbValue); }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", amount=" + amount +
                ", unit=" + unit +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
