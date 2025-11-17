package com.utaste.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Helper central pour gérer la base SQLite de uTaste.
 *
 * Rôles :
 *  - Créer toutes les tables au premier lancement.
 *  - Gérer les migrations quand DB_VERSION change.
 *
 * IMPORTANT :
 *  - On définit des constantes "ING_COL_*" pour la table ingredients.
 *  - On définit des constantes "REC_COL_*" pour la table recipes.
 *  - On garde aussi des alias "COL_*" et "COL_RECIPE_*" pour compatibilité
 *    avec les anciennes classes (IngredientDao, IngredientService, etc.).
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    // Nom du fichier SQLite et version de schéma.
    public static final String DB_NAME = "utaste.db";
    // ↑ augmente ce nombre si tu modifies la structure de la DB.
    // On passe à 3 pour ajouter les colonnes nutritionnelles.
    public static final int DB_VERSION = 3;

    // ============================================================
    //  TABLE INGREDIENTS
    // ============================================================

    public static final String TABLE_INGREDIENTS = "ingredients";

    // Colonnes de base
    public static final String ING_COL_ID          = "id";
    public static final String ING_COL_NAME        = "name";
    public static final String ING_COL_QR_CODE     = "qr_code";
    public static final String ING_COL_AMOUNT      = "amount";
    public static final String ING_COL_UNIT        = "unit";
    public static final String ING_COL_CREATED_AT  = "created_at";
    public static final String ING_COL_UPDATED_AT  = "updated_at";

    // Colonnes nutritionnelles (valeurs pour 100 g)
    public static final String ING_COL_CARBS_100G   = "carbs_100g";
    public static final String ING_COL_PROTEIN_100G = "protein_100g";
    public static final String ING_COL_FAT_100G     = "fat_100g";
    public static final String ING_COL_FIBER_100G   = "fiber_100g";
    public static final String ING_COL_SALT_100G    = "salt_100g";

    // Alias génériques (compatibilité avec ancien code)
    public static final String COL_ID         = ING_COL_ID;
    public static final String COL_NAME       = ING_COL_NAME;
    public static final String COL_QR_CODE    = ING_COL_QR_CODE;
    public static final String COL_AMOUNT     = ING_COL_AMOUNT;
    public static final String COL_UNIT       = ING_COL_UNIT;
    public static final String COL_CREATED_AT = ING_COL_CREATED_AT;
    public static final String COL_UPDATED_AT = ING_COL_UPDATED_AT;

    // ============================================================
    //  TABLE RECIPES
    // ============================================================

    public static final String TABLE_RECIPES = "recipes";

    public static final String REC_COL_ID          = "id";
    public static final String REC_COL_NAME        = "name";
    public static final String REC_COL_DESCRIPTION = "description";
    public static final String REC_COL_IMAGE_PATH  = "image_path";

    // Alias pour compatibilité
    public static final String COL_RECIPE_ID   = REC_COL_ID;
    public static final String COL_RECIPE_NAME = REC_COL_NAME;
    public static final String COL_DESCRIPTION = REC_COL_DESCRIPTION;
    public static final String COL_IMAGE_PATH  = REC_COL_IMAGE_PATH;

    // ============================================================
    //  TABLE RECIPE_INGREDIENTS  (relation N-N : recette ↔ ingrédient)
    // ============================================================

    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    public static final String COL_RI_ID            = "id";
    public static final String COL_RI_RECIPE_ID     = "recipe_id";
    public static final String COL_RI_INGREDIENT_ID = "ingredient_id";
    public static final String COL_RI_QUANTITY      = "quantity";   // quantité (%) ou g pour cette recette

    // ============================================================
    //  SQL de création des tables
    // ============================================================

    // Table INGREDIENTS
    private static final String SQL_CREATE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_INGREDIENTS + " (" +
                    ING_COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ING_COL_NAME       + " TEXT NOT NULL, " +
                    ING_COL_QR_CODE    + " TEXT, " +
                    ING_COL_AMOUNT     + " REAL, " +
                    ING_COL_UNIT       + " TEXT, " +
                    // Colonnes nutritionnelles : toutes optionnelles, REAL
                    ING_COL_CARBS_100G   + " REAL, " +
                    ING_COL_PROTEIN_100G + " REAL, " +
                    ING_COL_FAT_100G     + " REAL, " +
                    ING_COL_FIBER_100G   + " REAL, " +
                    ING_COL_SALT_100G    + " REAL, " +
                    ING_COL_CREATED_AT + " INTEGER NOT NULL, " +
                    ING_COL_UPDATED_AT + " INTEGER NOT NULL" +
                    ");";

    // Table RECIPES
    private static final String SQL_CREATE_RECIPES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                    REC_COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    REC_COL_NAME        + " TEXT NOT NULL UNIQUE, " +
                    REC_COL_DESCRIPTION + " TEXT, " +
                    REC_COL_IMAGE_PATH  + " TEXT" +
                    ");";

    // Table RECIPE_INGREDIENTS (liaison Recette ↔ Ingrédient)
    private static final String SQL_CREATE_RECIPE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPE_INGREDIENTS + " (" +
                    COL_RI_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RI_RECIPE_ID     + " INTEGER NOT NULL, " +
                    COL_RI_INGREDIENT_ID + " INTEGER NOT NULL, " +
                    COL_RI_QUANTITY      + " REAL NOT NULL, " +
                    // FK vers recipes(id)
                    "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " +
                    TABLE_RECIPES + "(" + REC_COL_ID + ") ON DELETE CASCADE, " +
                    // FK vers ingredients(id)
                    "FOREIGN KEY(" + COL_RI_INGREDIENT_ID + ") REFERENCES " +
                    TABLE_INGREDIENTS + "(" + ING_COL_ID + ") ON DELETE CASCADE" +
                    ");";

    // ============================================================
    //  Constructeur / cycle de vie SQLiteOpenHelper
    // ============================================================

    public DataBaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Important pour que les FOREIGN KEY fonctionnent (ON DELETE CASCADE).
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INGREDIENTS);
        db.execSQL(SQL_CREATE_RECIPES);
        db.execSQL(SQL_CREATE_RECIPE_INGREDIENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Stratégie simple : on drop puis on recrée tout.
        // (OK pour un projet scolaire, à éviter en prod réelle.)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        onCreate(db);
    }
}
