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
     * Insère ou met à jour une liaison ingrédient-recette.
     */
    public void insertOrUpdate(long recipeId, long ingredientId, double quantity, String unit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Vérifier si le lien existe déjà pour décider entre INSERT et UPDATE
        long existingLinkId = findByRecipeAndIngredient(db, recipeId, ingredientId);

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_RI_RECIPE_ID, recipeId);
        values.put(DataBaseHelper.COL_RI_INGREDIENT_ID, ingredientId);
        values.put(DataBaseHelper.COL_RI_QUANTITY, quantity);
        values.put(DataBaseHelper.COL_RI_UNIT, unit);

        if (existingLinkId != -1L) {
            // Le lien existe, on met à jour
            String whereClause = DataBaseHelper.COL_RI_ID + " = ?";
            String[] whereArgs = { String.valueOf(existingLinkId) };
            db.update(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, values, whereClause, whereArgs);
        } else {
            // Le lien n'existe pas, on l'insère
            db.insert(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, null, values);
        }
    }

    /**
     * CORRIGÉ : Met à jour un lien existant. C'est la méthode que ModifyIngredientQuantityActivity devrait utiliser.
     */
    public boolean updateLink(long recipeId, long ingredientId, double newQuantity, String newUnit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_RI_QUANTITY, newQuantity);
        values.put(DataBaseHelper.COL_RI_UNIT, newUnit);

        String whereClause = DataBaseHelper.COL_RI_RECIPE_ID + " = ? AND " + DataBaseHelper.COL_RI_INGREDIENT_ID + " = ?";
        String[] whereArgs = { String.valueOf(recipeId), String.valueOf(ingredientId) };

        int rowsAffected = db.update(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, values, whereClause, whereArgs);

        // La mise à jour est réussie si exactement une ligne a été modifiée.
        return rowsAffected == 1;
    }


    /**
     * Trouve l'ID de la liaison (COL_RI_ID) dans la table recipe_ingredients.
     * Retourne -1 si non trouvé.
     */
    private long findByRecipeAndIngredient(SQLiteDatabase db, long recipeId, long ingredientId) {
        long id = -1L;
        String[] columns = { DataBaseHelper.COL_RI_ID };
        String selection = DataBaseHelper.COL_RI_RECIPE_ID + " = ? AND " + DataBaseHelper.COL_RI_INGREDIENT_ID + " = ?";
        String[] args = { String.valueOf(recipeId), String.valueOf(ingredientId) };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPE_INGREDIENTS, columns, selection, args, null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RI_ID));
            }
        }
        return id;
    }

    /**
     * Supprime le lien entre une recette et un ingrédient.
     */
    public boolean deleteLink(long recipeId, long ingredientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DataBaseHelper.COL_RI_RECIPE_ID + " = ? AND " + DataBaseHelper.COL_RI_INGREDIENT_ID + " = ?";
        String[] whereArgs = { String.valueOf(recipeId), String.valueOf(ingredientId) };
        int rowsDeleted = db.delete(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, whereClause, whereArgs);
        return rowsDeleted > 0;
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}