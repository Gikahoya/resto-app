package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.utaste.domain.recipe.Ingredient;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.utaste.domain.recipe.NutritionFact;

public class IngredientDao {

    private final DataBaseHelper dbHelper;
    // On instancie Gson une seule fois pour la performance
    private final Gson gson = new Gson();

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
        cv.put(DataBaseHelper.COL_NAME, ing.getName());
        cv.put(DataBaseHelper.COL_QR_CODE, ing.getQrCode());
        cv.put(DataBaseHelper.COL_AMOUNT, ing.getAmount());
        cv.put(DataBaseHelper.COL_UNIT, Ingredient.Unit.toDb(ing.getUnit()));
        cv.put(DataBaseHelper.COL_CREATED_AT, now);
        cv.put(DataBaseHelper.COL_UPDATED_AT, now);

        // ================== CORRECTION (LIGNE MANQUANTE) ==================
        if (ing.getNutritionFact() != null) {
            String json = gson.toJson(ing.getNutritionFact());
            cv.put(DataBaseHelper.COL_NUTRITION_FACTS_JSON, json);
        }
        // =================================================================

        return database.insert(DataBaseHelper.TABLE_INGREDIENTS, null, cv);
    }

    public Ingredient getIngredientById(int id) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        try (Cursor c = database.query(
                DataBaseHelper.TABLE_INGREDIENTS, null,
                DataBaseHelper.COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null
        )) {
            if (c != null && c.moveToFirst()) {
                return fromCursor(c);
            }
        }
        return null;
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        try (Cursor c = database.query(DataBaseHelper.TABLE_INGREDIENTS, null, null, null, null, null, DataBaseHelper.COL_NAME + " COLLATE NOCASE ASC")) {
            if (c != null) {
                while (c.moveToNext()) {
                    list.add(fromCursor(c));
                }
            }
        }
        return list;
    }

    public int updateIngredient(int id, Ingredient ing) {
        long now = System.currentTimeMillis();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_NAME, ing.getName());
        cv.put(DataBaseHelper.COL_QR_CODE, ing.getQrCode());
        cv.put(DataBaseHelper.COL_AMOUNT, ing.getAmount());
        cv.put(DataBaseHelper.COL_UNIT, Ingredient.Unit.toDb(ing.getUnit()));
        cv.put(DataBaseHelper.COL_UPDATED_AT, now);

        // ================== CORRECTION (LIGNE MANQUANTE) ==================
        if (ing.getNutritionFact() != null) {
            String json = gson.toJson(ing.getNutritionFact());
            cv.put(DataBaseHelper.COL_NUTRITION_FACTS_JSON, json);
        } else {
            // Important : si on enlève les infos, il faut mettre la colonne à NULL
            cv.putNull(DataBaseHelper.COL_NUTRITION_FACTS_JSON);
        }
        // =================================================================

        return database.update(
                DataBaseHelper.TABLE_INGREDIENTS,
                cv,
                DataBaseHelper.COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int deleteIngredient(int id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.delete(
                DataBaseHelper.TABLE_INGREDIENTS,
                DataBaseHelper.COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    private Ingredient fromCursor(Cursor c) {
        Ingredient ing = new Ingredient();
        ing.setId(c.getInt(c.getColumnIndexOrThrow(DataBaseHelper.COL_ID)));
        ing.setName(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.COL_NAME)));
        ing.setQrCode(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.COL_QR_CODE)));
        ing.setAmount(c.getDouble(c.getColumnIndexOrThrow(DataBaseHelper.COL_AMOUNT)));
        ing.setUnit(Ingredient.Unit.fromDb(c.getString(c.getColumnIndexOrThrow(DataBaseHelper.COL_UNIT))));
        ing.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.COL_CREATED_AT)));
        ing.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.COL_UPDATED_AT)));

        int nutritionColumnIndex = c.getColumnIndex(DataBaseHelper.COL_NUTRITION_FACTS_JSON);
        if (nutritionColumnIndex != -1 && !c.isNull(nutritionColumnIndex)) {
            String json = c.getString(nutritionColumnIndex);
            if (json != null && !json.isEmpty()) {
                ing.setNutritionFact(gson.fromJson(json, NutritionFact.class));
            }
        }
        return ing;
    }

    public Ingredient findById(long ingredientId) {
        return null;
    }
}