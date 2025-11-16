package com.utaste.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Classe centrale pour la gestion de la base SQLite de uTaste.
 *
 * ⚠ IMPORTANT :
 *  - Elle définit tous les noms de tables/colonnes utilisés ailleurs dans le code
 *    (ex: IngredientService, RecipeDao, etc.).
 *  - Si tu modifies un nom ici, il faut aussi le modifier partout où il est utilisé.
 *
 * Dans cette version :
 *  - Table INGREDIENTS
 *  - Table RECIPES
 *  - Table RECIPE_INGREDIENTS (lien recette <-> ingrédient avec quantité)
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    // =====================
    //  Configuration DB
    // =====================

    /** Nom du fichier de base de données sur le téléphone. */
    public static final String DB_NAME = "utaste.db";

    /**
     * Version de la base.
     * ↑ Incrémente ce nombre à chaque fois que tu modifies la structure des tables
     *    (ajout/suppression de colonnes, de tables, etc.).
     */
    public static final int DB_VERSION = 2;

    // =====================
    //  TABLE : INGREDIENTS
    // =====================

    /** Nom de la table des ingrédients. */
    public static final String TABLE_INGREDIENTS = "ingredients";

    /** PK autoincrémentée. */
    public static final String COL_ID = "id";

    /** Nom lisible de l’ingrédient (ex: "Pâtes", "Poulet"). */
    public static final String COL_NAME = "name";

    /** QR code associé (peut être null). */
    public static final String COL_QR_CODE = "qr_code";

    /** Quantité de référence (ex: 100 g, 1 pièce, etc.) – optionnel pour l’instant. */
    public static final String COL_AMOUNT = "amount";

    /** Unité de cette quantité (ex: "g", "ml", "pc"). */
    public static final String COL_UNIT = "unit";

    /** Date de création en millis epoch (System.currentTimeMillis()). */
    public static final String COL_CREATED_AT = "created_at";

    /** Date de dernière mise à jour. */
    public static final String COL_UPDATED_AT = "updated_at";

    // =====================
    //  TABLE : RECIPES
    // =====================

    /** Nom de la table des recettes. */
    public static final String TABLE_RECIPES = "recipes";

    /** PK autoincrémentée pour les recettes. */
    public static final String COL_RECIPE_ID = "id";

    /** Nom unique de la recette (ex: "Spaghetti bolognaise"). */
    public static final String COL_RECIPE_NAME = "name";

    /** Description libre. */
    public static final String COL_DESCRIPTION = "description";

    /** Chemin ou URL de l’image (optionnel). */
    public static final String COL_IMAGE_PATH = "image_path";

    // =======================================
    //  TABLE : RECIPE_INGREDIENTS (link table)
    // =======================================

    /**
     * Table de liaison recette <-> ingrédient.
     * Une ligne = un ingrédient utilisé dans une recette avec une certaine quantité.
     */
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    /** PK autoincrémentée de la table de lien. */
    public static final String COL_RI_ID = "id";

    /** FK vers recipes.id. */
    public static final String COL_RI_RECIPE_ID = "recipe_id";

    /** FK vers ingredients.id. */
    public static final String COL_RI_INGREDIENT_ID = "ingredient_id";

    /** Quantité de cet ingrédient dans la recette (ex: 250.0). */
    public static final String COL_RI_QUANTITY = "quantity";

    // =====================
    //  SQL de création
    // =====================

    private static final String SQL_CREATE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_INGREDIENTS + " (" +
                    COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME        + " TEXT NOT NULL, " +
                    COL_QR_CODE     + " TEXT, " +
                    COL_AMOUNT      + " REAL, " +
                    COL_UNIT        + " TEXT, " +
                    COL_CREATED_AT  + " INTEGER NOT NULL, " +
                    COL_UPDATED_AT  + " INTEGER NOT NULL" +
                    ");";

    private static final String SQL_CREATE_RECIPES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                    COL_RECIPE_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RECIPE_NAME  + " TEXT NOT NULL UNIQUE, " +
                    COL_DESCRIPTION  + " TEXT, " +
                    COL_IMAGE_PATH   + " TEXT" +
                    ");";

    private static final String SQL_CREATE_RECIPE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPE_INGREDIENTS + " (" +
                    COL_RI_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RI_RECIPE_ID     + " INTEGER NOT NULL, " +
                    COL_RI_INGREDIENT_ID + " INTEGER NOT NULL, " +
                    COL_RI_QUANTITY      + " REAL NOT NULL, " +
                    // contraintes de clé étrangère
                    "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " +
                    TABLE_RECIPES + "(" + COL_RECIPE_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + COL_RI_INGREDIENT_ID + ") REFERENCES " +
                    TABLE_INGREDIENTS + "(" + COL_ID + ") ON DELETE CASCADE" +
                    ");";

    public DataBaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Active les contraintes de clé étrangère (important pour ON DELETE CASCADE)
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INGREDIENTS);
        db.execSQL(SQL_CREATE_RECIPES);
        db.execSQL(SQL_CREATE_RECIPE_INGREDIENTS);

        // Index utiles pour les recherches par nom / QR code
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ing_name ON "
                + TABLE_INGREDIENTS + " (" + COL_NAME + " COLLATE NOCASE);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ing_qr ON "
                + TABLE_INGREDIENTS + " (" + COL_QR_CODE + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Stratégie simple : on recrée tout.
        // Pour une vraie app en prod, on ferait des migrations plus fines.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        onCreate(db);
    }
}
