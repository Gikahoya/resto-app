package com.utaste.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "utaste.db";
    public static final int DB_VERSION = 2;

    // ===== TABLE INGREDIENTS =====
    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QR_CODE = "qr_code";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_UNIT = "unit";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    // ===== TABLE RECIPES =====
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_IMAGE_PATH = "image_path";

    // ===== TABLE RECIPE_INGREDIENTS =====
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_INGREDIENT_ID = "ingredient_id";
    public static final String COL_RI_QUANTITY = "quantity";

    // ===============================
    //   SQL : création des tables
    // ===============================
    private static final String SQL_CREATE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_INGREDIENTS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_QR_CODE + " TEXT, " +
                    COL_AMOUNT + " REAL, " +
                    COL_UNIT + " TEXT, " +
                    COL_CREATED_AT + " INTEGER NOT NULL, " +
                    COL_UPDATED_AT + " INTEGER NOT NULL" +
                    ");";

    private static final String SQL_CREATE_RECIPES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                    COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RECIPE_NAME + " TEXT NOT NULL, " +
                    COL_DESCRIPTION + " TEXT, " +
                    COL_IMAGE_PATH + " TEXT" +
                    ");";

    private static final String SQL_CREATE_RECIPE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPE_INGREDIENTS + " (" +
                    COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                    COL_RI_INGREDIENT_ID + " INTEGER NOT NULL, " +
                    COL_RI_QUANTITY + " REAL NOT NULL, " +
                    "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + COL_RI_INGREDIENT_ID + ") REFERENCES " + TABLE_INGREDIENTS + "(" + COL_ID + ") ON DELETE CASCADE" +
                    ");";

    // ===============================
    //   Constructeur
    // ===============================
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

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ing_name ON " + TABLE_INGREDIENTS + " (" + COL_NAME + " COLLATE NOCASE);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ing_qr ON " + TABLE_INGREDIENTS + " (" + COL_QR_CODE + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        onCreate(db);
    }
}

