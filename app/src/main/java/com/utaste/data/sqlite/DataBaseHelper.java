package com.utaste.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {

    // =====================
    //  Configuration DB
    // =====================

    public static final String DB_NAME = "utaste.db";
    public static final int DB_VERSION = 3; // Assurez-vous que la version est bien 3

    // =====================
    //  TABLE : USERS (AJOUT)
    // =====================
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_FIRST = "first";
    public static final String COL_USER_LAST = "last";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PWD = "password";
    public static final String COL_USER_ROLE = "role";

    // =====================
    //  TABLE : INGREDIENTS
    // =====================
    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QR_CODE = "qr_code";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_UNIT = "unit";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";
    public static final String COL_NUTRITION_FACTS_JSON = "nutrition_facts_json";

    // =====================
    //  TABLE : RECIPES
    // =====================
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_IMAGE_PATH = "image_path";

    // =======================================
    //  TABLE : RECIPE_INGREDIENTS (link table)
    // =======================================
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_INGREDIENT_ID = "ingredient_id";
    public static final String COL_RI_QUANTITY = "quantity";
    public static final String COL_RI_QUANTITY_G = "quantity_g";

    // =====================
    //  SQL de création
    // =====================

    // AJOUT : Requête de création pour la table users
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COL_USER_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_FIRST + " TEXT, " +
                    COL_USER_LAST  + " TEXT, " +
                    COL_USER_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COL_USER_PWD   + " TEXT NOT NULL, " +
                    COL_USER_ROLE  + " TEXT" +
                    ");";

    private static final String SQL_CREATE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_INGREDIENTS + " (" +
                    COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME        + " TEXT NOT NULL, " +
                    COL_QR_CODE     + " TEXT, " +
                    COL_AMOUNT      + " REAL, " +
                    COL_UNIT        + " TEXT, " +
                    // N'oubliez pas cette colonne que nous avons ajoutée plus tôt
                    COL_NUTRITION_FACTS_JSON + " TEXT, " +
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
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Exécution de toutes les requêtes de création
        db.execSQL(SQL_CREATE_USERS); // AJOUT
        db.execSQL(SQL_CREATE_INGREDIENTS);
        db.execSQL(SQL_CREATE_RECIPES);
        db.execSQL(SQL_CREATE_RECIPE_INGREDIENTS);

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ing_name ON "
                + TABLE_INGREDIENTS + " (" + COL_NAME + " COLLATE NOCASE);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ing_qr ON "
                + TABLE_INGREDIENTS + " (" + COL_QR_CODE + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Stratégie simple : on recrée tout.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS); // AJOUT
        onCreate(db);
    }
}