package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.NutritionFact;

import java.util.ArrayList;
import java.util.List;

public class IngredientDao {

    private final DataBaseHelper dbHelper;

    public IngredientDao(Context context) {
        this.dbHelper = new DataBaseHelper(context);
    }

    private Ingredient mapCursorToIngredient(Cursor c) {
        Ingredient ing = new Ingredient();

        ing.setId(c.getInt(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_ID)));
        ing.setName(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_NAME)));
        ing.setQrCode(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_QR_CODE)));
        ing.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_CREATED_AT)));
        ing.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.ING_COL_UPDATED_AT)));

        // Nutrition facts
        double carbs100 = safeGetDouble(c, DataBaseHelper.ING_COL_CARBS_100G);
        double protein100 = safeGetDouble(c, DataBaseHelper.ING_COL_PROTEIN_100G);
        double fat100 = safeGetDouble(c, DataBaseHelper.ING_COL_FAT_100G);
        double fiber100 = safeGetDouble(c, DataBaseHelper.ING_COL_FIBER_100G);
        double salt100 = safeGetDouble(c, DataBaseHelper.ING_COL_SALT_100G);
        double saturatedFat100 = safeGetDouble(c, DataBaseHelper.ING_COL_SATURATED_FAT_100G);
        double sugars100 = safeGetDouble(c, DataBaseHelper.ING_COL_SUGARS_100G);

        NutritionFact nf = new NutritionFact(
                carbs100,
                protein100,
                fat100,
                fiber100,
                salt100,
                saturatedFat100,
                sugars100
        );
        ing.setNutritionFact(nf);

        return ing;
    }

    private double safeGetDouble(Cursor c, String columnName) {
        int idx = c.getColumnIndex(columnName);
        if (idx == -1 || c.isNull(idx)) {
            return 0.0;
        }
        return c.getDouble(idx);
    }

    public long insert(Ingredient ingredient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.ING_COL_NAME, ingredient.getName());
        values.put(DataBaseHelper.ING_COL_QR_CODE, ingredient.getQrCode());
        values.put(DataBaseHelper.ING_COL_CREATED_AT, now);
        values.put(DataBaseHelper.ING_COL_UPDATED_AT, now);

        NutritionFact nf = ingredient.getNutritionFact();
        if (nf != null) {
            values.put(DataBaseHelper.ING_COL_CARBS_100G, nf.getCarbsPer100g());
            values.put(DataBaseHelper.ING_COL_PROTEIN_100G, nf.getProteinPer100g());
            values.put(DataBaseHelper.ING_COL_FAT_100G, nf.getFatPer100g());
            values.put(DataBaseHelper.ING_COL_FIBER_100G, nf.getFiberPer100g());
            values.put(DataBaseHelper.ING_COL_SALT_100G, nf.getSaltPer100g());
            values.put(DataBaseHelper.ING_COL_SATURATED_FAT_100G, nf.getSaturatedFatPer100g());
            values.put(DataBaseHelper.ING_COL_SUGARS_100G, nf.getSugarsPer100g());
        }

        long id = db.insert(DataBaseHelper.TABLE_INGREDIENTS, null, values);

        if (id != -1) {
            ingredient.setId((int) id);
            ingredient.setCreatedAt(now);
            ingredient.setUpdatedAt(now);
        }
        return id;
    }

    public int update(Ingredient ingredient) {
        if (ingredient.getId() <= 0) return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.ING_COL_NAME, ingredient.getName());
        values.put(DataBaseHelper.ING_COL_QR_CODE, ingredient.getQrCode());
        values.put(DataBaseHelper.ING_COL_UPDATED_AT, now);

        NutritionFact nf = ingredient.getNutritionFact();
        if (nf != null) {
            values.put(DataBaseHelper.ING_COL_CARBS_100G, nf.getCarbsPer100g());
            values.put(DataBaseHelper.ING_COL_PROTEIN_100G, nf.getProteinPer100g());
            values.put(DataBaseHelper.ING_COL_FAT_100G, nf.getFatPer100g());
            values.put(DataBaseHelper.ING_COL_FIBER_100G, nf.getFiberPer100g());
            values.put(DataBaseHelper.ING_COL_SALT_100G, nf.getSaltPer100g());
            values.put(DataBaseHelper.ING_COL_SATURATED_FAT_100G, nf.getSaturatedFatPer100g());
            values.put(DataBaseHelper.ING_COL_SUGARS_100G, nf.getSugarsPer100g());
        }

        int rows = db.update(
                DataBaseHelper.TABLE_INGREDIENTS,
                values,
                DataBaseHelper.ING_COL_ID + " = ?",
                new String[]{String.valueOf(ingredient.getId())}
        );

        if (rows > 0) {
            ingredient.setUpdatedAt(now);
        }
        return rows;
    }

    public int deleteById(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DataBaseHelper.TABLE_INGREDIENTS,
                DataBaseHelper.ING_COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    @Nullable
    public Ingredient getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                null,
                DataBaseHelper.ING_COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        )) {
            if (c != null && c.moveToFirst()) {
                return mapCursorToIngredient(c);
            }
        }
        return null;
    }

    public long getIdByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long id = -1L;
        String[] columns = {DataBaseHelper.ING_COL_ID};
        String selection = DataBaseHelper.ING_COL_NAME + " = ?";
        String[] selectionArgs = {name};

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                columns, selection, selectionArgs, null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.ING_COL_ID));
            }
        }
        return id;
    }

    @Nullable
    public Ingredient getByQrCode(String qrCode) {
        if (qrCode == null) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                null,
                DataBaseHelper.ING_COL_QR_CODE + " = ?",
                new String[]{qrCode},
                null, null, null
        )) {
            if (c != null && c.moveToFirst()) {
                return mapCursorToIngredient(c);
            }
        }
        return null;
    }

    public List<Ingredient> getAll() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                null, null, null, null, null,
                DataBaseHelper.ING_COL_NAME + " COLLATE NOCASE ASC"
        )) {
            if (c != null && c.moveToFirst()) {
                do {
                    list.add(mapCursorToIngredient(c));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public void close() {
        dbHelper.close();
    }
}