
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

    private DataBaseHelper db;

    public IngredientDao(Context context) {
        this.db = new DataBaseHelper(context.getApplicationContext());
    }

    // ============================
    // INSERT
    // ============================
    /** Ajoute un ingrédient et retourne son ID */
    public long insertIngredient(Ingredient ing) {
        long now = System.currentTimeMillis();

        SQLiteDatabase database = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_NAME, ing.getName());
        cv.put(DataBaseHelper.COL_QR_CODE, ing.getQrCode());
        cv.put(DataBaseHelper.COL_AMOUNT, ing.getAmount());
        cv.put(DataBaseHelper.COL_UNIT, Ingredient.unitToDb(ing.getUnit()));
        cv.put(DataBaseHelper.COL_CREATED_AT, now);
        cv.put(DataBaseHelper.COL_UPDATED_AT, now);

        long newId = database.insert(DataBaseHelper.TABLE_INGREDIENTS, null, cv);
        database.close();
        return newId;
    }

    // ============================
    // SELECT by ID
    // ============================
    public Ingredient getIngredientById(int id) {
        SQLiteDatabase database = db.getReadableDatabase();

        String[] cols = {
                DataBaseHelper.COL_ID,
                DataBaseHelper.COL_NAME,
                DataBaseHelper.COL_QR_CODE,
                DataBaseHelper.COL_AMOUNT,
                DataBaseHelper.COL_UNIT,
                DataBaseHelper.COL_CREATED_AT,
                DataBaseHelper.COL_UPDATED_AT
        };

        Cursor c = database.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                cols,
                DataBaseHelper.COL_ID + "=?",
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
                DataBaseHelper.COL_ID,
                DataBaseHelper.COL_NAME,
                DataBaseHelper.COL_QR_CODE,
                DataBaseHelper.COL_AMOUNT,
                DataBaseHelper.COL_UNIT,
                DataBaseHelper.COL_CREATED_AT,
                DataBaseHelper.COL_UPDATED_AT
        };

        Cursor c = database.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                cols,
                null, null, null, null,
                DataBaseHelper.COL_NAME + " COLLATE NOCASE ASC"
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
        cv.put(DataBaseHelper.COL_NAME, ing.getName());
        cv.put(DataBaseHelper.COL_QR_CODE, ing.getQrCode());
        cv.put(DataBaseHelper.COL_AMOUNT, ing.getAmount());
        cv.put(DataBaseHelper.COL_UNIT, Ingredient.unitToDb(ing.getUnit()));
        cv.put(DataBaseHelper.COL_UPDATED_AT, now);

        int rows = database.update(
                DataBaseHelper.TABLE_INGREDIENTS,
                cv,
                DataBaseHelper.COL_ID + "=?",
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
                DataBaseHelper.TABLE_INGREDIENTS,
                DataBaseHelper.COL_ID + "=?",
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
        int rows = database.delete(DataBaseHelper.TABLE_INGREDIENTS, null, null);
        database.close();
        return rows;
    }

    // ============================
    // SELECT by QR Code
    // ============================
    public Ingredient getByQrCode(String qrCode) {
        SQLiteDatabase database = db.getReadableDatabase();

        String[] cols = {
                DataBaseHelper.COL_ID,
                DataBaseHelper.COL_NAME,
                DataBaseHelper.COL_QR_CODE,
                DataBaseHelper.COL_AMOUNT,
                DataBaseHelper.COL_UNIT,
                DataBaseHelper.COL_CREATED_AT,
                DataBaseHelper.COL_UPDATED_AT
        };

        Cursor c = database.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                cols,
                DataBaseHelper.COL_QR_CODE + "=?",
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
        ing.setId(c.getInt(c.getColumnIndexOrThrow(DataBaseHelper.COL_ID)));
        ing.setName(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.COL_NAME)));
        ing.setQrCode(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.COL_QR_CODE)));
        ing.setAmount(c.getDouble(c.getColumnIndexOrThrow(DataBaseHelper.COL_AMOUNT)));
        ing.setUnit(Ingredient.unitFromDb(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.COL_UNIT))));
        ing.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.COL_CREATED_AT)));
        ing.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.COL_UPDATED_AT)));
        return ing;
    }
}
