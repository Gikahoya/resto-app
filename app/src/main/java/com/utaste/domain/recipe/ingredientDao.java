
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

    private IngredientDb db;

    public IngredientDao(Context context) {
        this.db = new IngredientDb(context.getApplicationContext());
    }

    // ============================
    // INSERT
    // ============================
    /** Ajoute un ingrédient et retourne son ID */
    public long insertIngredient(Ingredient ing) {
        long now = System.currentTimeMillis();

        SQLiteDatabase database = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(IngredientDb.COL_NAME, ing.getName());
        cv.put(IngredientDb.COL_QR_CODE, ing.getQrCode());
        cv.put(IngredientDb.COL_AMOUNT, ing.getAmount());
        cv.put(IngredientDb.COL_UNIT, Ingredient.unitToDb(ing.getUnit()));
        cv.put(IngredientDb.COL_CREATED_AT, now);
        cv.put(IngredientDb.COL_UPDATED_AT, now);

        long newId = database.insert(IngredientDb.TABLE_INGREDIENTS, null, cv);
        database.close();
        return newId;
    }

    // ============================
    // SELECT by ID
    // ============================
    public Ingredient getIngredientById(int id) {
        SQLiteDatabase database = db.getReadableDatabase();

        String[] cols = {
                IngredientDb.COL_ID,
                IngredientDb.COL_NAME,
                IngredientDb.COL_QR_CODE,
                IngredientDb.COL_AMOUNT,
                IngredientDb.COL_UNIT,
                IngredientDb.COL_CREATED_AT,
                IngredientDb.COL_UPDATED_AT
        };

        Cursor c = database.query(
                IngredientDb.TABLE_INGREDIENTS,
                cols,
                IngredientDb.COL_ID + "=?",
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
                IngredientDb.COL_ID,
                IngredientDb.COL_NAME,
                IngredientDb.COL_QR_CODE,
                IngredientDb.COL_AMOUNT,
                IngredientDb.COL_UNIT,
                IngredientDb.COL_CREATED_AT,
                IngredientDb.COL_UPDATED_AT
        };

        Cursor c = database.query(
                IngredientDb.TABLE_INGREDIENTS,
                cols,
                null, null, null, null,
                IngredientDb.COL_NAME + " COLLATE NOCASE ASC"
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
        cv.put(IngredientDb.COL_NAME, ing.getName());
        cv.put(IngredientDb.COL_QR_CODE, ing.getQrCode());
        cv.put(IngredientDb.COL_AMOUNT, ing.getAmount());
        cv.put(IngredientDb.COL_UNIT, Ingredient.unitToDb(ing.getUnit()));
        cv.put(IngredientDb.COL_UPDATED_AT, now);

        int rows = database.update(
                IngredientDb.TABLE_INGREDIENTS,
                cv,
                IngredientDb.COL_ID + "=?",
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
                IngredientDb.TABLE_INGREDIENTS,
                IngredientDb.COL_ID + "=?",
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
        int rows = database.delete(IngredientDb.TABLE_INGREDIENTS, null, null);
        database.close();
        return rows;
    }

    // ============================
    // SELECT by QR Code
    // ============================
    public Ingredient getByQrCode(String qrCode) {
        SQLiteDatabase database = db.getReadableDatabase();

        String[] cols = {
                IngredientDb.COL_ID,
                IngredientDb.COL_NAME,
                IngredientDb.COL_QR_CODE,
                IngredientDb.COL_AMOUNT,
                IngredientDb.COL_UNIT,
                IngredientDb.COL_CREATED_AT,
                IngredientDb.COL_UPDATED_AT
        };

        Cursor c = database.query(
                IngredientDb.TABLE_INGREDIENTS,
                cols,
                IngredientDb.COL_QR_CODE + "=?",
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
        ing.setId(c.getInt(c.getColumnIndexOrThrow(IngredientDb.COL_ID)));
        ing.setName(c.getString(c.getColumnIndexOrThrow(IngredientDb.COL_NAME)));
        ing.setQrCode(c.getString(c.getColumnIndexOrThrow(IngredientDb.COL_QR_CODE)));
        ing.setAmount(c.getDouble(c.getColumnIndexOrThrow(IngredientDb.COL_AMOUNT)));
        ing.setUnit(Ingredient.unitFromDb(c.getString(c.getColumnIndexOrThrow(IngredientDb.COL_UNIT))));
        ing.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(IngredientDb.COL_CREATED_AT)));
        ing.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(IngredientDb.COL_UPDATED_AT)));
        return ing;
    }
}
