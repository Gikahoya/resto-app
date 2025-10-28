
package com.utaste.domain.recipe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * IngredientDao — gère toutes les opérations CRUD sur la table "ingredients"
 */
public class IngredientDao {

    private com.utaste.data.sqlite.DataBaseHelper db;

    public IngredientDao(Context context) {
        this.db = new com.utaste.data.sqlite.DataBaseHelper(context.getApplicationContext());
    }

    // ============================
    // INSERT
    // ============================
    /** Ajoute un ingrédient et retourne son ID */
    public long insertIngredient(Ingredient ing) {
        long now = System.currentTimeMillis();

        SQLiteDatabase database = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_NAME, ing.getName());
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_QR_CODE, ing.getQrCode());
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_AMOUNT, ing.getAmount());
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_UNIT, Ingredient.unitToDb(ing.getUnit()));
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_CREATED_AT, now);
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_UPDATED_AT, now);

        long newId = database.insert(com.utaste.data.sqlite.DataBaseHelper.TABLE_INGREDIENTS, null, cv);
        database.close();
        return newId;
    }

    // ============================
    // SELECT by ID
    // ============================
    public Ingredient getIngredientById(int id) {
        SQLiteDatabase database = db.getReadableDatabase();

        String[] cols = {
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_ID,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_NAME,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_QR_CODE,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_AMOUNT,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_UNIT,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_CREATED_AT,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_UPDATED_AT
        };

        Cursor c = database.query(
                com.utaste.data.sqlite.DataBaseHelper.TABLE_INGREDIENTS,
                cols,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_ID + "=?",
                new String[]{ String.valueOf(id) },
                null, null, null
        );

        Ingredient result = null;
        if (c != null) {
            if (c.moveToFirst()) result = fromCursor(c);
            c.close();
        }

        database.close();
        return result;
    }

    // ============================
    // SELECT all
    // ============================
    public List<Ingredient> getAllIngredients() {
        SQLiteDatabase database = db.getReadableDatabase();

        String[] cols = {
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_ID,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_NAME,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_QR_CODE,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_AMOUNT,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_UNIT,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_CREATED_AT,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_UPDATED_AT
        };

        Cursor c = database.query(
                com.utaste.data.sqlite.DataBaseHelper.TABLE_INGREDIENTS,
                cols,
                null, null, null, null,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_NAME + " COLLATE NOCASE ASC"
        );

        List<Ingredient> list = new ArrayList<>();
        if (c != null) {
            while (c.moveToNext()) list.add(fromCursor(c));
            c.close();
        }

        database.close();
        return list;
    }

    // ============================
    // UPDATE
    // ============================
    public int updateIngredient(int id, Ingredient ing) {
        long now = System.currentTimeMillis();
        SQLiteDatabase database = db.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_NAME, ing.getName());
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_QR_CODE, ing.getQrCode());
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_AMOUNT, ing.getAmount());
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_UNIT, Ingredient.unitToDb(ing.getUnit()));
        cv.put(com.utaste.data.sqlite.DataBaseHelper.ING_COL_UPDATED_AT, now);

        int rows = database.update(
                com.utaste.data.sqlite.DataBaseHelper.TABLE_INGREDIENTS,
                cv,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_ID + "=?",
                new String[]{ String.valueOf(id) }
        );

        database.close();
        return rows;
    }

    // ============================
    // DELETE (one)
    // ============================
    public int deleteIngredient(int id) {
        SQLiteDatabase database = db.getWritableDatabase();
        int rows = database.delete(
                com.utaste.data.sqlite.DataBaseHelper.TABLE_INGREDIENTS,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_ID + "=?",
                new String[]{ String.valueOf(id) }
        );
        database.close();
        return rows;
    }

    // ============================
    // DELETE all
    // ============================
    public int deleteAllIngredients() {
        SQLiteDatabase database = db.getWritableDatabase();
        int rows = database.delete(com.utaste.data.sqlite.DataBaseHelper.TABLE_INGREDIENTS, null, null);
        database.close();
        return rows;
    }

    // ============================
    // SELECT by QR Code
    // ============================
    public Ingredient getByQrCode(String qrCode) {
        SQLiteDatabase database = db.getReadableDatabase();

        String[] cols = {
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_ID,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_NAME,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_QR_CODE,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_AMOUNT,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_UNIT,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_CREATED_AT,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_UPDATED_AT
        };

        Cursor c = database.query(
                com.utaste.data.sqlite.DataBaseHelper.TABLE_INGREDIENTS,
                cols,
                com.utaste.data.sqlite.DataBaseHelper.ING_COL_QR_CODE + "=?",
                new String[]{ qrCode },
                null, null, null
        );

        Ingredient result = null;
        if (c != null) {
            if (c.moveToFirst()) result = fromCursor(c);
            c.close();
        }

        database.close();
        return result;
    }

    // ============================
    // Mapping Cursor -> Ingredient
    // ============================
    private Ingredient fromCursor(Cursor c) {
        Ingredient ing = new Ingredient();
        ing.setId(c.getInt(c.getColumnIndexOrThrow(com.utaste.data.sqlite.DataBaseHelper.ING_COL_ID)));
        ing.setName(c.getString(c.getColumnIndexOrThrow(com.utaste.data.sqlite.DataBaseHelper.ING_COL_NAME)));
        ing.setQrCode(c.getString(c.getColumnIndexOrThrow(com.utaste.data.sqlite.DataBaseHelper.ING_COL_QR_CODE)));
        ing.setAmount(c.getDouble(c.getColumnIndexOrThrow(com.utaste.data.sqlite.DataBaseHelper.ING_COL_AMOUNT)));
        ing.setUnit(Ingredient.unitFromDb(c.getString(c.getColumnIndexOrThrow(com.utaste.data.sqlite.DataBaseHelper.ING_COL_UNIT))));
        ing.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(com.utaste.data.sqlite.DataBaseHelper.ING_COL_CREATED_AT)));
        ing.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(com.utaste.data.sqlite.DataBaseHelper.ING_COL_UPDATED_AT)));
        return ing;
    }
}
