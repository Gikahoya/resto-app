/**
package com.utaste.app.chef;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.data.sqlite.DataBaseHelper;

/**
 * Service pour gérer les ingrédients côté chef.
 *
 * Il s'occupe de :
 *  - créer / retrouver un ingrédient à partir de son QR code
 *  - lier cet ingrédient à une recette dans la table recipe_ingredients
 *  - enregistrer correctement la quantité dans la base

public class IngredientService {

    private final DataBaseHelper dbHelper;

    public IngredientService(Context context) {
        this.dbHelper = new DataBaseHelper(context);
    }

    /**
     * Ajoute (ou met à jour) un ingrédient pour une recette en utilisant :
     *  - le NOM de la recette
     *  - le nom de l'ingrédient (saisi par l'utilisateur)
     *  - le QR code scanné
     *  - la quantité
     *  - l'unité (optionnelle, ex: "g")
     *
     * @return true si tout s'est bien passé, false sinon.
    
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
            // recette introuvable
            return false;
        }
        return addIngredientToRecipeFromQr(recipeId, ingredientName, qrCode, quantity, unit);
    }

    /**
     * Variante quand tu connais déjà l'id de la recette.

    public boolean addIngredientToRecipeFromQr(
            long recipeId,
            String ingredientName,
            String qrCode,
            double quantity,
            String unit
    ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();

        db.beginTransaction();
        try {
            // 1) Récupérer ou créer l'ingrédient à partir du QR code
            long ingredientId = getOrInsertIngredient(db, ingredientName, qrCode, unit, now);

            // 2) Créer / mettre à jour la relation recette <-> ingrédient avec la quantité
            insertOrUpdateRecipeIngredient(db, recipeId, ingredientId, quantity);

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // ---------- Helpers privés ----------

    private long getRecipeIdByName(SQLiteDatabase db, String recipeName) {
        long id = -1L;

        String[] columns = { DataBaseHelper.COL_RECIPE_ID };
        String selection = DataBaseHelper.COL_RECIPE_NAME + " = ?";
        String[] args = { recipeName };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                columns,
                selection,
                args,
                null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RECIPE_ID));
            }
        }
        return id;
    }

    /**
     * Retourne l'id d'un ingrédient existant pour ce QR code,
     * ou insère un nouvel ingrédient si le QR n'est pas encore connu.

    private long getOrInsertIngredient(SQLiteDatabase db,
                                       String name,
                                       String qrCode,
                                       String unit,
                                       long now) {

        // 1) Essayer de trouver l'ingrédient par QR code
        String[] columns = { DataBaseHelper.COL_ID };
        String selection = DataBaseHelper.COL_QR_CODE + " = ?";
        String[] args = { qrCode };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                columns,
                selection,
                args,
                null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_ID));
            }
        }

        // 2) Pas trouvé → on crée un nouvel ingrédient
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_NAME, name);
        values.put(DataBaseHelper.COL_QR_CODE, qrCode);
        values.put(DataBaseHelper.COL_UNIT, unit);
        values.put(DataBaseHelper.COL_CREATED_AT, now);
        values.put(DataBaseHelper.COL_UPDATED_AT, now);

        return db.insertOrThrow(DataBaseHelper.TABLE_INGREDIENTS, null, values);
    }

    /**
     * Crée ou met à jour la ligne dans "recipe_ingredients" pour (recette, ingrédient).
     * Si une ligne existe déjà pour ce couple, on met simplement à jour la quantité.

    private void insertOrUpdateRecipeIngredient(SQLiteDatabase db,
                                                long recipeId,
                                                long ingredientId,
                                                double quantity) {

        String selection = DataBaseHelper.COL_RI_RECIPE_ID + " = ? AND " +
                DataBaseHelper.COL_RI_INGREDIENT_ID + " = ?";
        String[] args = {
                String.valueOf(recipeId),
                String.valueOf(ingredientId)
        };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPE_INGREDIENTS,
                new String[]{ DataBaseHelper.COL_RI_ID },
                selection,
                args,
                null, null, null
        )) {
            ContentValues values = new ContentValues();
            values.put(DataBaseHelper.COL_RI_RECIPE_ID, recipeId);
            values.put(DataBaseHelper.COL_RI_INGREDIENT_ID, ingredientId);
            values.put(DataBaseHelper.COL_RI_QUANTITY, quantity);

            if (cursor != null && cursor.moveToFirst()) {
                // ligne existe déjà → update de la quantité
                long rowId = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RI_ID));
                db.update(
                        DataBaseHelper.TABLE_RECIPE_INGREDIENTS,
                        values,
                        DataBaseHelper.COL_RI_ID + " = ?",
                        new String[]{ String.valueOf(rowId) }
                );
            } else {
                // pas encore de lien → insert
                db.insertOrThrow(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, null, values);
            }
        }
    }

    public void close() {
        dbHelper.close();
    }
}
*/