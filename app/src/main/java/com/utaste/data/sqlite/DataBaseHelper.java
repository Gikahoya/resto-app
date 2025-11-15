package com.utaste.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "utaste.db";
    public static final int DB_VERSION = 1;

    // --- Ingredients Table ---
    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String ING_COL_ID = "id";
    public static final String ING_COL_NAME = "name";
    public static final String ING_COL_QR_CODE = "qr_code";
    public static final String ING_COL_AMOUNT = "amount";
    public static final String ING_COL_UNIT = "unit";
    public static final String ING_COL_CREATED_AT = "created_at";
    public static final String ING_COL_UPDATED_AT = "updated_at";

    // --- Recipes Table ---
    public static final String TABLE_RECIPES = "recipes";
    public static final String REC_COL_ID = "id";
    public static final String REC_COL_NAME = "name";
    public static final String REC_COL_DESCRIPTION = "description";
    public static final String REC_COL_IMAGE_PATH = "image_path";


    private static final String SQL_CREATE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_INGREDIENTS + " (" +
                    ING_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ING_COL_NAME + " TEXT NOT NULL, " +
                    ING_COL_QR_CODE + " TEXT, " +
                    ING_COL_AMOUNT + " REAL NOT NULL, " +
                    ING_COL_UNIT + " TEXT NOT NULL, " +
                    ING_COL_CREATED_AT + " INTEGER NOT NULL, " +
                    ING_COL_UPDATED_AT + " INTEGER NOT NULL" +
                    ");";

    private static final String SQL_CREATE_RECIPES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                    REC_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    REC_COL_NAME + " TEXT NOT NULL UNIQUE, " +
                    REC_COL_DESCRIPTION + " TEXT, " +
                    REC_COL_IMAGE_PATH + " TEXT" +
                    ");";

    public DataBaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INGREDIENTS);
        db.execSQL(SQL_CREATE_RECIPES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }
}
