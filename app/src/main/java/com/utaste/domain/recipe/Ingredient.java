package com.utaste.domain.recipe;

/**
 * Représente un ingrédient dans le domaine.
 * Correspond à la table "ingredients" en SQLite.
 */
public class Ingredient {

    // Identifiant en base (colonne "id")
    private long id;

    // Nom lisible de l'ingrédient (colonne "name")
    private String name;

    // Code QR associé à l'ingrédient (colonne "qr_code")
    private String qrCode;

    // Quantité "de base" pour cet ingrédient (optionnelle, colonne "amount")
    private Double amount;

    // Unité de la quantité de base (ex: "g", "ml", "piece", colonne "unit")
    private String unit;

    public Ingredient() {
        // constructeur vide requis par certains frameworks / libs
    }

    public Ingredient(long id, String name, String qrCode, Double amount, String unit) {
        this.id = id;
        this.name = name;
        this.qrCode = qrCode;
        this.amount = amount;
        this.unit = unit;
    }

    public Ingredient(String name, String qrCode) {
        this(0, name, qrCode, null, null);
    }

    // ========= Getters / Setters =========

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
