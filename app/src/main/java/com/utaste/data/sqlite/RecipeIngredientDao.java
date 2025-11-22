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
     * Si la combinaison recipeId/ingredientId existe, la quantité et l'unité sont mises à jour.
     * Sinon, une nouvelle entrée est créée.
     *
     * @param recipeId L'ID de la recette.
     * @param ingredientId L'ID de l'ingrédient.
     * @param quantity La nouvelle quantité.
     * @param unit La nouvelle unité.
     */
    public void insertOrUpdate(long recipeId, long ingredientId, double quantity, String unit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // On vérifie si l'entrée existe déjà
        long existingId = findByRecipeAndIngredient(db, recipeId, ingredientId);
        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_RECIPE_ID, recipeId);
        values.put(DataBaseHelper.COL_RI_INGREDIENT_ID, ingredientId);
        values.put(DataBaseHelper.COL_RI_QUANTITY, quantity);
        values.put(DataBaseHelper.COL_UNIT, unit); // <-- AJOUT DE L'UNITÉ
        values.put(DataBaseHelper.COL_UPDATED_AT, now);

        if (existingId != -1L) {
            // L'entrée existe, on la met à jour
            String whereClause = DataBaseHelper.COL_ID + " = ?";
            String[] whereArgs = { String.valueOf(existingId) };
            db.update(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, values, whereClause, whereArgs);
        } else {
            // L'entrée n'existe pas, on l'insère
            values.put(DataBaseHelper.COL_CREATED_AT, now);
            db.insert(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, null, values);
        }
    }

    /**
     * Trouve l'ID d'une entrée RecipeIngredient par son recipeId et ingredientId.
     *
     * @param db La base de données.
     * @param recipeId L'ID de la recette.
     * @param ingredientId L'ID de l'ingrédient.
     * @return L'ID de l'entrée ou -1 si elle n'est pas trouvée.
     */
    private long findByRecipeAndIngredient(SQLiteDatabase db, long recipeId, long ingredientId) {
        long id = -1L;
        String[] columns = { DataBaseHelper.COL_ID };
        String selection = DataBaseHelper.COL_RECIPE_ID + " = ? AND " + DataBaseHelper.COL_RI_INGREDIENT_ID + " = ?";
        String[] args = { String.valueOf(recipeId), String.valueOf(ingredientId) };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPE_INGREDIENTS, columns, selection, args, null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_ID));
            }
        }
        return id;
    }

    public void close() {
        dbHelper.close();
    }
}
