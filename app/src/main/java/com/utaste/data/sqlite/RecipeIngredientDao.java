package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;

public class RecipeIngredientDao {
    private final DataBaseHelper dbHelper;

    public RecipeIngredientDao(@Nullable Context context) {
        this.dbHelper = new DataBaseHelper(context);
    }

    /**
     * Insère ou met à jour un ingrédient dans une recette.
     */
    public void insertOrUpdate(long recipeId, long ingredientId, double quantity, String unit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 1. Vérifier si le lien existe déjà
        long existingId = findByRecipeAndIngredient(db, recipeId, ingredientId);

        ContentValues values = new ContentValues();
        // CORRECTION : Utilisation de COL_RI_RECIPE_ID (la colonne de la table de liaison)
        values.put(DataBaseHelper.COL_RI_RECIPE_ID, recipeId);
        values.put(DataBaseHelper.COL_RI_INGREDIENT_ID, ingredientId);
        values.put(DataBaseHelper.COL_RI_QUANTITY, quantity);
        values.put(DataBaseHelper.COL_RI_UNIT, unit);

        // RETRAIT : created_at et updated_at ne sont pas dans le CREATE TABLE de recipe_ingredients

        if (existingId != -1L) {
            // UPDATE : On utilise COL_RI_ID
            String whereClause = DataBaseHelper.COL_RI_ID + " = ?";
            String[] whereArgs = { String.valueOf(existingId) };
            db.update(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, values, whereClause, whereArgs);
        } else {
            // INSERT
            db.insert(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, null, values);
        }
    }

    /**
     * Trouve l'ID de la liaison (COL_RI_ID) dans recipe_ingredients.
     */
    private long findByRecipeAndIngredient(SQLiteDatabase db, long recipeId, long ingredientId) {
        long id = -1L;
        // CORRECTION : On cible la colonne ID de la table de liaison
        String[] columns = { DataBaseHelper.COL_RI_ID };

        // CORRECTION : Clause WHERE sur les colonnes de liaison
        String selection = DataBaseHelper.COL_RI_RECIPE_ID + " = ? AND " + DataBaseHelper.COL_RI_INGREDIENT_ID + " = ?";
        String[] args = { String.valueOf(recipeId), String.valueOf(ingredientId) };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPE_INGREDIENTS, columns, selection, args, null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                // CORRECTION : Lecture de la bonne colonne
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RI_ID));
            }
        }
        return id;
    }
    // DANS RecipeIngredientDao.java

    /**
     * Supprime le lien entre une recette et un ingrédient.
     */
    public boolean deleteLink(long recipeId, long ingredientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Clause WHERE : recipe_id = ? AND ingredient_id = ?
        String whereClause = DataBaseHelper.COL_RI_RECIPE_ID + " = ? AND " + DataBaseHelper.COL_RI_INGREDIENT_ID + " = ?";
        String[] whereArgs = { String.valueOf(recipeId), String.valueOf(ingredientId) };

        int rowsDeleted = db.delete(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, whereClause, whereArgs);

        return rowsDeleted > 0;
    }

    public void close() {
        dbHelper.close();
    }
}