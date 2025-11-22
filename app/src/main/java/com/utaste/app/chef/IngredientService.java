package com.utaste.app.chef;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.utaste.data.sqlite.DataBaseHelper;
import com.utaste.data.sqlite.RecipeIngredientDao;

/**
 * Service pour gérer les INGREDIENTS côté Chef.
 */
public class IngredientService {

    private final DataBaseHelper dbHelper;
    private final RecipeIngredientDao recipeIngredientDao;

    public IngredientService(@Nullable Context context) {
        this.dbHelper = new DataBaseHelper(context);
        this.recipeIngredientDao = new RecipeIngredientDao(context);
    }

    // =========================================================================
    //  API publique utilisée par l’UI (Activity)
    // =========================================================================

    public boolean addIngredientToRecipeFromQrByRecipeName(
            String recipeName,
            String ingredientName,
            String qrCode,
            double quantity,
            String unit
    ) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        long recipeId = getRecipeIdByName(db, recipeName);
        if (recipeId == -1L) {
            return false; // Recette introuvable
        }

        // On ferme la DB en lecture avant de potentiellement ouvrir en écriture
        // db.close(); // Pas nécessaire avec le helper qui gère une seule instance

        return addIngredientToRecipeFromQr(recipeId, ingredientName, qrCode, quantity, unit);
    }

    public boolean addIngredientToRecipeFromQr(
            long recipeId,
            String ingredientName,
            String qrCode,
            double quantity,
            String unit // L'unité de l'UI n'est qu'une suggestion pour les nouveaux ingrédients
    ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();

        db.beginTransaction();
        try {
            // Étape 1: Récupère ou crée l'ingrédient. L'unité de l'UI n'est utilisée que s'il est nouveau.
            long ingredientId = getOrInsertIngredient(db, ingredientName, qrCode, unit, now);

            // Étape 2: Récupère l'unité CANONIQUE de l'ingrédient depuis la DB, pour garantir la cohérence.
            String canonicalUnit = getIngredientUnit(db, ingredientId);

            // Étape 3: Utilise cette unité canonique pour l'association recette-ingrédient.
            recipeIngredientDao.insertOrUpdate(recipeId, ingredientId, quantity, canonicalUnit);

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public void close() {
        dbHelper.close();
        recipeIngredientDao.close();
    }

    // =========================================================================
    //  Helpers PRIVÉS
    // =========================================================================

    private long getRecipeIdByName(SQLiteDatabase db, String recipeName) {
        long id = -1L;
        String[] columns = { DataBaseHelper.COL_RECIPE_ID };
        String selection = DataBaseHelper.COL_RECIPE_NAME + " = ?";
        String[] args = { recipeName };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES, columns, selection, args, null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RECIPE_ID));
            }
        }
        return id;
    }

    private String getIngredientUnit(SQLiteDatabase db, long ingredientId) {
        String unit = null;
        String[] columns = { DataBaseHelper.COL_UNIT };
        String selection = DataBaseHelper.COL_ID + " = ?";
        String[] args = { String.valueOf(ingredientId) };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_INGREDIENTS, columns, selection, args, null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                unit = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_UNIT));
            }
        }
        // Le code erroné qui était ici a été supprimé.
        return unit;
    }

    private long getOrInsertIngredient(SQLiteDatabase db,
                                       String name,
                                       String qrCode,
                                       String unit, // Unité suggérée par l'UI
                                       long now) {

        // On vérifie d'abord si un ingrédient avec ce QR code existe déjà
        if (qrCode != null && !qrCode.isEmpty()) {
            String[] columns = { DataBaseHelper.COL_ID };
            String selection = DataBaseHelper.COL_QR_CODE + " = ?";
            String[] args = { qrCode };

            try (Cursor cursor = db.query(
                    DataBaseHelper.TABLE_INGREDIENTS, columns, selection, args, null, null, null
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    // L'ingrédient existe, on retourne son ID
                    return cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_ID));
                }
            }
        }

        // Si on arrive ici, l'ingrédient n'existe pas, on le crée.
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_NAME, name);
        values.put(DataBaseHelper.COL_QR_CODE, qrCode);
        values.put(DataBaseHelper.COL_UNIT, unit); // On utilise l'unité fournie par l'UI
        values.put(DataBaseHelper.COL_CREATED_AT, now);
        values.put(DataBaseHelper.COL_UPDATED_AT, now);

        // Insère le nouvel ingrédient et retourne son ID
        return db.insertOrThrow(DataBaseHelper.TABLE_INGREDIENTS, null, values);
    }
}
