package com.utaste.app.chef;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.utaste.data.sqlite.DataBaseHelper;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.RecipeIngredient;
import com.google.gson.Gson;
import com.utaste.domain.recipe.NutritionFact;
import java.util.ArrayList;
import java.util.List;

public class IngredientService {

    private final DataBaseHelper dbHelper;
    private final Gson gson = new Gson();

    public IngredientService(Context context) {
        this.dbHelper = new DataBaseHelper(context.getApplicationContext());
    }

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
            // On ne passe pas d'ID, donc ça va créer si ça n'existe pas.
            long ingredientId = getOrInsertIngredient(db, -1, ingredientName, qrCode, unit, now);
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

    // =====================================================================
    //  NOUVELLE MÉTHODE : Mettre à jour un ingrédient dans une recette
    // =====================================================================
    public boolean updateIngredientInRecipe(
            long recipeId,
            long ingredientId, // On a besoin de l'ID de l'ingrédient à modifier
            String newName,
            String newQrCode,
            double newQuantity,
            String newUnit
    ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();
        db.beginTransaction();
        try {
            // On passe l'ID. La méthode va mettre à jour l'ingrédient existant.
            getOrInsertIngredient(db, ingredientId, newName, newQrCode, newUnit, now);

            // On met à jour la quantité dans la table de liaison.
            insertOrUpdateRecipeIngredient(db, recipeId, ingredientId, newQuantity);

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        // ... (cette méthode est déjà correcte, pas de changement)
        List<RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        final String SQL_QUERY = "SELECT " +
                "i." + DataBaseHelper.COL_ID + ", " +
                "i." + DataBaseHelper.COL_NAME + ", " +
                "i." + DataBaseHelper.COL_QR_CODE + ", " +
                "i." + DataBaseHelper.COL_AMOUNT + ", " +
                "i." + DataBaseHelper.COL_UNIT + ", " +
                "i." + DataBaseHelper.COL_NUTRITION_FACTS_JSON + ", " +
                "ri." + DataBaseHelper.COL_RI_QUANTITY +
                " FROM " + DataBaseHelper.TABLE_RECIPE_INGREDIENTS + " ri" +
                " INNER JOIN " + DataBaseHelper.TABLE_INGREDIENTS + " i ON ri." + DataBaseHelper.COL_RI_INGREDIENT_ID + " = i." + DataBaseHelper.COL_ID +
                " WHERE ri." + DataBaseHelper.COL_RI_RECIPE_ID + " = ?";

        try (Cursor cursor = db.rawQuery(SQL_QUERY, new String[]{ String.valueOf(recipeId) })) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_ID)));
                    ingredient.setName(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_NAME)));
                    ingredient.setQrCode(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_QR_CODE)));
                    // Les amount/unit de la table ingredient ne nous intéressent pas ici,
                    // car on gère la quantité dans recipe_ingredients.

                    int nutritionColumnIndex = cursor.getColumnIndex(DataBaseHelper.COL_NUTRITION_FACTS_JSON);
                    if (nutritionColumnIndex != -1 && !cursor.isNull(nutritionColumnIndex)) {
                        String json = cursor.getString(nutritionColumnIndex);
                        if (json != null && !json.isEmpty()) {
                            ingredient.setNutritionFact(gson.fromJson(json, NutritionFact.class));
                        }
                    }

                    double quantityInRecipe = cursor.getDouble(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RI_QUANTITY));

                    RecipeIngredient recipeIngredient = new RecipeIngredient(null, ingredient, quantityInRecipe);
                    list.add(recipeIngredient);
                }
            }
        }
        return list;
    }

    public boolean removeIngredientFromRecipe(long recipeId, long ingredientId) {
        // ... (cette méthode est déjà correcte, pas de changement)
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DataBaseHelper.COL_RI_RECIPE_ID + " = ? AND " +
                DataBaseHelper.COL_RI_INGREDIENT_ID + " = ?";
        String[] whereArgs = { String.valueOf(recipeId), String.valueOf(ingredientId) };

        int deletedRows = db.delete(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, whereClause, whereArgs);
        return deletedRows > 0;
    }

    public void close() {
        dbHelper.close();
    }

    // =====================================================================
    //  MODIFICATION de la méthode `getOrInsertIngredient`
    // =====================================================================
    private long getOrInsertIngredient(SQLiteDatabase db,
                                       long existingId, // Nouvel argument : l'ID s'il existe
                                       String name,
                                       String qrCode,
                                       String unit,
                                       long now) {

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_NAME, name);
        values.put(DataBaseHelper.COL_QR_CODE, qrCode);
        values.put(DataBaseHelper.COL_UNIT, unit); // Unité de base (pas la quantité)
        values.put(DataBaseHelper.COL_UPDATED_AT, now);

        if (existingId != -1) {
            // ----- MODE MISE À JOUR -----
            // On a un ID, donc on met à jour la ligne existante.
            String whereClause = DataBaseHelper.COL_ID + " = ?";
            String[] whereArgs = { String.valueOf(existingId) };
            db.update(DataBaseHelper.TABLE_INGREDIENTS, values, whereClause, whereArgs);
            return existingId; // On retourne l'ID qu'on a mis à jour.
        } else {
            // ----- MODE CRÉATION -----
            // Pas d'ID, on cherche par QR code pour éviter les doublons.
            String[] columns   = { DataBaseHelper.COL_ID };
            String   selection = DataBaseHelper.COL_QR_CODE + " = ?";
            String[] args      = { qrCode };

            try (Cursor cursor = db.query(DataBaseHelper.TABLE_INGREDIENTS, columns, selection, args, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // L'ingrédient existe déjà avec ce QR code, on le réutilise.
                    return cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_ID));
                }
            }

            // L'ingrédient n'existe pas, on le crée.
            values.put(DataBaseHelper.COL_CREATED_AT, now);
            return db.insertOrThrow(DataBaseHelper.TABLE_INGREDIENTS, null, values);
        }
    }

    private void insertOrUpdateRecipeIngredient(SQLiteDatabase db,
                                                long recipeId,
                                                long ingredientId,
                                                double quantity) {
        // ... (cette méthode est déjà correcte, pas de changement)
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_RI_RECIPE_ID,    recipeId);
        values.put(DataBaseHelper.COL_RI_INGREDIENT_ID, ingredientId);
        values.put(DataBaseHelper.COL_RI_QUANTITY,     quantity);

        db.insertWithOnConflict(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean addIngredientToRecipeFromQrByRecipeName(String recipeName, String ingredientName, String qr, double qty, String g) {
        return false;
    }
}