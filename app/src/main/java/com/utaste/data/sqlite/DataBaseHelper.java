package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.utaste.domain.recipe.Ingredient;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "utaste.db";
    public static final int DB_VERSION = 7;

    // Colonnes communes
    public static final String COL_ID         = "id";
    public static final String COL_NAME       = "name";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    // Table Ingredients
    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String ING_COL_ID          = COL_ID;
    public static final String ING_COL_NAME        = COL_NAME;
    public static final String ING_COL_QR_CODE     = "qr_code";
    public static final String ING_COL_CARBS_100G   = "carbs_100g";
    public static final String ING_COL_PROTEIN_100G = "protein_100g";
    public static final String ING_COL_FAT_100G     = "fat_100g";
    public static final String ING_COL_FIBER_100G   = "fiber_100g";
    public static final String ING_COL_SALT_100G    = "salt_100g";
    public static final String ING_COL_SATURATED_FAT_100G = "saturated_fat_100g";
    public static final String ING_COL_SUGARS_100G  = "sugars_100g";
    public static final String ING_COL_CREATED_AT  = COL_CREATED_AT;
    public static final String ING_COL_UPDATED_AT  = COL_UPDATED_AT;

    // Table Recipes
    public static final String TABLE_RECIPES = "recipes";
    public static final String REC_COL_ID          = COL_ID;
    public static final String REC_COL_NAME        = COL_NAME;
    public static final String REC_COL_DESCRIPTION = "description";
    public static final String REC_COL_IMAGE_PATH  = "image_path";

    // Table Recipe-Ingredients
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID            = COL_ID;
    public static final String COL_RI_RECIPE_ID     = "recipe_id";
    public static final String COL_RI_INGREDIENT_ID = "ingredient_id";
    public static final String COL_RI_QUANTITY      = "quantity";
    public static final String COL_RI_UNIT          = "unit";

    // Table Sales
    public static final String TABLE_SALES = "sales";
    public static final String COL_SALE_ID = COL_ID;
    public static final String COL_SALE_RECIPE_ID = "recipe_id";
    public static final String COL_SALE_RATING = "rating";
    public static final String COL_SALE_APPRECIATION = "appreciation";
    public static final String COL_SALE_TIMESTAMP = "timestamp";

    // Table Users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = COL_ID;
    public static final String COL_USER_FIRST = "first";
    public static final String COL_USER_LAST = "last";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PWD = "password";
    public static final String COL_USER_ROLE = "role";

    private static final String SQL_CREATE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_INGREDIENTS + " (" +
                    ING_COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ING_COL_NAME       + " TEXT NOT NULL UNIQUE, " +
                    ING_COL_QR_CODE    + " TEXT, " +
                    ING_COL_CARBS_100G   + " REAL, " +
                    ING_COL_PROTEIN_100G + " REAL, " +
                    ING_COL_FAT_100G     + " REAL, " +
                    ING_COL_FIBER_100G   + " REAL, " +
                    ING_COL_SALT_100G    + " REAL, " +
                    ING_COL_SATURATED_FAT_100G + " REAL, " +
                    ING_COL_SUGARS_100G  + " REAL, " +
                    ING_COL_CREATED_AT + " INTEGER NOT NULL, " +
                    ING_COL_UPDATED_AT + " INTEGER NOT NULL" +
                    ");";

    private static final String SQL_CREATE_RECIPES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                    REC_COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    REC_COL_NAME        + " TEXT NOT NULL UNIQUE, " +
                    REC_COL_DESCRIPTION + " TEXT, " +
                    REC_COL_IMAGE_PATH  + " TEXT" +
                    ");";

    private static final String SQL_CREATE_RECIPE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPE_INGREDIENTS + " (" +
                    COL_RI_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RI_RECIPE_ID     + " INTEGER NOT NULL, " +
                    COL_RI_INGREDIENT_ID + " INTEGER NOT NULL, " +
                    COL_RI_QUANTITY      + " REAL NOT NULL, " +
                    COL_RI_UNIT          + " TEXT, " +
                    "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " +
                    TABLE_RECIPES + "(" + REC_COL_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + COL_RI_INGREDIENT_ID + ") REFERENCES " +
                    TABLE_INGREDIENTS + "(" + ING_COL_ID + ") ON DELETE CASCADE" +
                    ");";

    private static final String SQL_CREATE_SALES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SALES + " (" +
                    COL_SALE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_SALE_RECIPE_ID + " INTEGER NOT NULL, " +
                    COL_SALE_RATING + " INTEGER NOT NULL, " +
                    COL_SALE_APPRECIATION + " TEXT, " +
                    COL_SALE_TIMESTAMP + " INTEGER NOT NULL" +
                    ");";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_FIRST + " TEXT, " +
                    COL_USER_LAST + " TEXT, " +
                    COL_USER_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COL_USER_PWD + " TEXT NOT NULL, " +
                    COL_USER_ROLE + " TEXT" +
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
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_INGREDIENTS);
        db.execSQL(SQL_CREATE_RECIPES);
        db.execSQL(SQL_CREATE_RECIPE_INGREDIENTS);
        db.execSQL(SQL_CREATE_SALES);

        addDefaultIngredients(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void addDefaultIngredients(SQLiteDatabase db) {
    }
}