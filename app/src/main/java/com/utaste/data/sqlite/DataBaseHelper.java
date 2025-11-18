package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.utaste.domain.recipe.Ingredient;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "utaste.db";
    public static final int DB_VERSION = 3;

    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String ING_COL_ID          = "id";
    public static final String ING_COL_NAME        = "name";
    public static final String ING_COL_QR_CODE     = "qr_code";
    public static final String ING_COL_AMOUNT      = "amount";
    public static final String ING_COL_UNIT        = "unit";
    public static final String ING_COL_CREATED_AT  = "created_at";
    public static final String ING_COL_UPDATED_AT  = "updated_at";
    public static final String ING_COL_CARBS_100G   = "carbs_100g";
    public static final String ING_COL_PROTEIN_100G = "protein_100g";
    public static final String ING_COL_FAT_100G     = "fat_100g";
    public static final String ING_COL_FIBER_100G   = "fiber_100g";
    public static final String ING_COL_SALT_100G    = "salt_100g";

    public static final String COL_ID         = ING_COL_ID;
    public static final String COL_NAME       = ING_COL_NAME;
    public static final String COL_QR_CODE    = ING_COL_QR_CODE;
    public static final String COL_AMOUNT     = ING_COL_AMOUNT;
    public static final String COL_UNIT       = ING_COL_UNIT;
    public static final String COL_CREATED_AT = ING_COL_CREATED_AT;
    public static final String COL_UPDATED_AT = ING_COL_UPDATED_AT;

    public static final String TABLE_RECIPES = "recipes";
    public static final String REC_COL_ID          = "id";
    public static final String REC_COL_NAME        = "name";
    public static final String REC_COL_DESCRIPTION = "description";
    public static final String REC_COL_IMAGE_PATH  = "image_path";

    public static final String COL_RECIPE_ID   = REC_COL_ID;
    public static final String COL_RECIPE_NAME = REC_COL_NAME;
    public static final String COL_DESCRIPTION = REC_COL_DESCRIPTION;
    public static final String COL_IMAGE_PATH  = REC_COL_IMAGE_PATH;

    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID            = "id";
    public static final String COL_RI_RECIPE_ID     = "recipe_id";
    public static final String COL_RI_INGREDIENT_ID = "ingredient_id";
    public static final String COL_RI_QUANTITY      = "quantity";

    private static final String SQL_CREATE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_INGREDIENTS + " (" +
                    ING_COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ING_COL_NAME       + " TEXT NOT NULL, " +
                    ING_COL_QR_CODE    + " TEXT, " +
                    ING_COL_AMOUNT     + " REAL, " +
                    ING_COL_UNIT       + " TEXT, " +
                    ING_COL_CARBS_100G   + " REAL, " +
                    ING_COL_PROTEIN_100G + " REAL, " +
                    ING_COL_FAT_100G     + " REAL, " +
                    ING_COL_FIBER_100G   + " REAL, " +
                    ING_COL_SALT_100G    + " REAL, " +
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
                    "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " +
                    TABLE_RECIPES + "(" + REC_COL_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + COL_RI_INGREDIENT_ID + ") REFERENCES " +
                    TABLE_INGREDIENTS + "(" + ING_COL_ID + ") ON DELETE CASCADE" +
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
        db.execSQL(SQL_CREATE_INGREDIENTS);
        db.execSQL(SQL_CREATE_RECIPES);
        db.execSQL(SQL_CREATE_RECIPE_INGREDIENTS);

        addDefaultIngredients(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        onCreate(db);
    }

    private void addDefaultIngredients(SQLiteDatabase db) {
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();

        // Flour
        values.put(ING_COL_NAME, "Flour");
        values.put(ING_COL_AMOUNT, 1000);
        values.put(ING_COL_UNIT, Ingredient.Unit.GRAMME.name());
        values.put(ING_COL_CREATED_AT, now);
        values.put(ING_COL_UPDATED_AT, now);
        db.insert(TABLE_INGREDIENTS, null, values);

        // Egg
        values.clear();
        values.put(ING_COL_NAME, "Egg");
        values.put(ING_COL_AMOUNT, 12);
        values.put(ING_COL_UNIT, Ingredient.Unit.PIECE.name());
        values.put(ING_COL_CREATED_AT, now);
        values.put(ING_COL_UPDATED_AT, now);
        db.insert(TABLE_INGREDIENTS, null, values);

        // Milk
        values.clear();
        values.put(ING_COL_NAME, "Milk");
        values.put(ING_COL_AMOUNT, 1);
        values.put(ING_COL_UNIT, Ingredient.Unit.LITRE.name());
        values.put(ING_COL_CREATED_AT, now);
        values.put(ING_COL_UPDATED_AT, now);
        db.insert(TABLE_INGREDIENTS, null, values);

        // Sugar
        values.clear();
        values.put(ING_COL_NAME, "Sugar");
        values.put(ING_COL_AMOUNT, 500);
        values.put(ING_COL_UNIT, Ingredient.Unit.GRAMME.name());
        values.put(ING_COL_CREATED_AT, now);
        values.put(ING_COL_UPDATED_AT, now);
        db.insert(TABLE_INGREDIENTS, null, values);

        // Salt
        values.clear();
        values.put(ING_COL_NAME, "Salt");
        values.put(ING_COL_AMOUNT, 250);
        values.put(ING_COL_UNIT, Ingredient.Unit.GRAMME.name());
        values.put(ING_COL_CREATED_AT, now);
        values.put(ING_COL_UPDATED_AT, now);
        db.insert(TABLE_INGREDIENTS, null, values);
    }
}
