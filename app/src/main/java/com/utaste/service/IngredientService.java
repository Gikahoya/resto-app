package com.utaste.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.utaste.data.sqlite.DataBaseHelper;
import com.utaste.data.sqlite.RecipeIngredientDao;

public class IngredientService {

    private final DataBaseHelper dbHelper;
    private final RecipeIngredientDao recipeIngredientDao;

    public IngredientService(@Nullable Context context) {
        this.dbHelper = new DataBaseHelper(context);
        this.recipeIngredientDao = new RecipeIngredientDao(context);
    }

    private long getRecipeIdByName(SQLiteDatabase db, String recipeName) {
        long id = -1L;
        String[] columns = { DataBaseHelper.REC_COL_ID };
        String selection = DataBaseHelper.REC_COL_NAME + " = ?";
        String[] args = { recipeName };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES, columns, selection, args, null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_ID));
            }
        }
        return id;
    }

    private String getIngredientUnit(SQLiteDatabase db, long ingredientId) {
        return null;
    }

    private long getOrInsertIngredient(SQLiteDatabase db,
                                       String name,
                                       String qrCode,
                                       String unit,
                                       long now) {

        if (qrCode != null && !qrCode.isEmpty()) {
            String[] columns = { DataBaseHelper.ING_COL_ID };
            String selection = DataBaseHelper.ING_COL_QR_CODE + " = ?";
            String[] args = { qrCode };

            try (Cursor cursor = db.query(
                    DataBaseHelper.TABLE_INGREDIENTS, columns, selection, args, null, null, null
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.ING_COL_ID));
                }
            }
        }

        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.ING_COL_NAME, name);
        values.put(DataBaseHelper.ING_COL_QR_CODE, qrCode);
        values.put(DataBaseHelper.ING_COL_CREATED_AT, now);
        values.put(DataBaseHelper.ING_COL_UPDATED_AT, now);

        return db.insertOrThrow(DataBaseHelper.TABLE_INGREDIENTS, null, values);
    }
}