package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.utaste.domain.recipe.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class IngredientDao {

    private final DataBaseHelper dbHelper;

    public IngredientDao(Context context) {
        this.dbHelper = new DataBaseHelper(context.getApplicationContext());
    }

    public void close() {
        dbHelper.close();
    }

    public long insertIngredient(Ingredient ing) {
        long now = System.currentTimeMillis();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.ING_COL_NAME, ing.getName());
        cv.put(DataBaseHelper.ING_COL_QR_CODE, ing.getQrCode());
        cv.put(DataBaseHelper.ING_COL_AMOUNT, ing.getAmount());
        cv.put(DataBaseHelper.ING_COL_UNIT, Ingredient.Unit.toDb(ing.getUnit()));
        cv.put(DataBaseHelper.ING_COL_CREATED_AT, now);
        cv.put(DataBaseHelper.ING_COL_UPDATED_AT, now);

        return database.insert(DataBaseHelper.TABLE_INGREDIENTS, null, cv);
    }

    public Ingredient getIngredientById(int id) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor c = database.query(
                DataBaseHelper.TABLE_INGREDIENTS, null,
                DataBaseHelper.ING_COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null
        );

        Ingredient result = null;
        if (c != null) {
            if (c.moveToFirst()) {
                result = fromCursor(c);
            }
            c.close();
        }
        return result;
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor c = database.query(DataBaseHelper.TABLE_INGREDIENTS, null, null, null, null, null, DataBaseHelper.ING_COL_NAME + " COLLATE NOCASE ASC");

        if (c != null) {
            while (c.moveToNext()) {
                list.add(fromCursor(c));
            }
            c.close();
        }
        return list;
    }

    public int updateIngredient(int id, Ingredient ing) {
        long now = System.currentTimeMillis();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.ING_COL_NAME, ing.getName());
        cv.put(DataBaseHelper.ING_COL_QR_CODE, ing.getQrCode());
        cv.put(DataBaseHelper.ING_COL_AMOUNT, ing.getAmount());
        cv.put(DataBaseHelper.ING_COL_UNIT, Ingredient.Unit.toDb(ing.getUnit()));
        cv.put(DataBaseHelper.ING_COL_UPDATED_AT, now);

        return database.update(
                DataBaseHelper.TABLE_INGREDIENTS,
                cv,
                DataBaseHelper.ING_COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int deleteIngredient(int id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.delete(
                DataBaseHelper.TABLE_INGREDIENTS,
                DataBaseHelper.ING_COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int deleteAllIngredients() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.delete(DataBaseHelper.TABLE_INGREDIENTS, null, null);
    }

    public Ingredient getByQrCode(String qrCode) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor c = database.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                null,
                DataBaseHelper.ING_COL_QR_CODE + "=?",
                new String[]{qrCode},
                null, null, null
        );

        Ingredient result = null;
        if (c != null) {
            if (c.moveToFirst()) {
                result = fromCursor(c);
            }
            c.close();
        }
        return result;
    }

    private Ingredient fromCursor(Cursor c) {
        Ingredient ing = new Ingredient();
        ing.setId(c.getInt(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_ID)));
        ing.setName(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_NAME)));
        ing.setQrCode(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_QR_CODE)));
        ing.setAmount(c.getDouble(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_AMOUNT)));
        ing.setUnit(Ingredient.Unit.fromDb(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_UNIT))));
        ing.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_CREATED_AT)));
        ing.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_UPDATED_AT)));
        return ing;
    }
}

