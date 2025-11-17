package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeDao {

    private final SQLiteDatabase db;

    public RecipeDao(Context context) {
        DataBaseHelper dbHelper = new DataBaseHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    // =======================================================================
    // CRUD de base (comme tu avais déjà)
    // =======================================================================

    public long insertIfAbsent(String name, String description, String imagePath) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.REC_COL_NAME,        name);
        values.put(DataBaseHelper.REC_COL_DESCRIPTION, description);
        values.put(DataBaseHelper.REC_COL_IMAGE_PATH,  imagePath);

        return db.insertWithOnConflict(
                DataBaseHelper.TABLE_RECIPES,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    public boolean exists(String name) {
        Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                new String[]{DataBaseHelper.REC_COL_ID},
                DataBaseHelper.REC_COL_NAME + " = ?",
                new String[]{name},
                null, null, null
        );
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public Recipe findByName(String name) {
        Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                null,
                DataBaseHelper.REC_COL_NAME + " = ?",
                new String[]{name},
                null, null, null,
                "1"
        );

        Recipe recipe = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_ID));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_DESCRIPTION));
                String imagePath   = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_IMAGE_PATH));

                recipe = new Recipe(name, description, imagePath);
                recipe.setId(id);
            }
            cursor.close();
        }
        return recipe;
    }

    public int updateByName(String name, String description, String imagePath) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.REC_COL_DESCRIPTION, description);
        values.put(DataBaseHelper.REC_COL_IMAGE_PATH,  imagePath);

        return db.update(
                DataBaseHelper.TABLE_RECIPES,
                values,
                DataBaseHelper.REC_COL_NAME + " = ?",
                new String[]{name}
        );
    }

    public int deleteByName(String name) {
        return db.delete(
                DataBaseHelper.TABLE_RECIPES,
                DataBaseHelper.REC_COL_NAME + " = ?",
                new String[]{name}
        );
    }

    // =======================================================================
    // 1) Récupérer toutes les recettes (pour le Spinner du bilan calorique)
    // =======================================================================

    public List<Recipe> getAll() {
        List<Recipe> list = new ArrayList<>();

        Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                null,
                null, null, null, null,
                DataBaseHelper.REC_COL_NAME + " COLLATE NOCASE ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_DESCRIPTION));
                String imagePath   = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_IMAGE_PATH));

                Recipe r = new Recipe(name, description, imagePath);
                r.setId(id);
                list.add(r);
            }
            cursor.close();
        }
        return list;
    }

    public List<String> getAllNames() {
        return java.util.Collections.emptyList();
    }

    public Recipe getByName(String selectedName) {
        return null;
    }

    // =======================================================================
    // 2) Récupérer les ingrédients + quantité pour une recette donnée
    //    -> utilisé par RecipeCaloricBalanceActivity
    // =======================================================================

    public static class RecipeIngredientRow {
        public final Ingredient ingredient;
        public final double quantityInGrams;

        public RecipeIngredientRow(Ingredient ingredient, double quantityInGrams) {
            this.ingredient = ingredient;
            this.quantityInGrams = quantityInGrams;
        }
    }

    public List<RecipeIngredientRow> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredientRow> rows = new ArrayList<>();

        // Jointure recipe_ingredients ↔ ingredients
        String sql =
                "SELECT i." + DataBaseHelper.ING_COL_ID + "      AS ing_id, " +
                        "i." + DataBaseHelper.ING_COL_NAME + "    AS ing_name, " +
                        "i." + DataBaseHelper.ING_COL_QR_CODE + " AS ing_qr, " +
                        "ri." + DataBaseHelper.COL_RI_QUANTITY + " AS qty " +
                        "FROM " + DataBaseHelper.TABLE_RECIPE_INGREDIENTS + " ri " +
                        "JOIN " + DataBaseHelper.TABLE_INGREDIENTS + " i " +
                        " ON ri." + DataBaseHelper.COL_RI_INGREDIENT_ID + " = i." + DataBaseHelper.ING_COL_ID +
                        " WHERE ri." + DataBaseHelper.COL_RI_RECIPE_ID + " = ?";

        Cursor c = db.rawQuery(sql, new String[]{ String.valueOf(recipeId) });

        if (c != null) {
            while (c.moveToNext()) {
                int ingId   = c.getInt(c.getColumnIndexOrThrow("ing_id"));
                String name = c.getString(c.getColumnIndexOrThrow("ing_name"));
                String qr   = c.getString(c.getColumnIndexOrThrow("ing_qr"));
                double qty  = c.getDouble(c.getColumnIndexOrThrow("qty"));

                Ingredient ing = new Ingredient();
                ing.setId(ingId);
                ing.setName(name);
                ing.setQrCode(qr);

                rows.add(new RecipeIngredientRow(ing, qty));
            }
            c.close();
        }
        return rows;
    }

    public void close() {
        db.close();
    }
}
