package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecipeDao {

    private final SQLiteDatabase db;
    private final Context context;

    public RecipeDao(Context context) {
        this.context = context.getApplicationContext();
        DataBaseHelper dbHelper = new DataBaseHelper(this.context);
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * Tries to insert a recipe. Because the 'name' column is UNIQUE,
     * this will fail if a recipe with the same name already exists.
     * @return The row ID of the new recipe, or -1 if the name already exists.
     */
    public long insertIfAbsent(String name, String description, String imagePath) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_RECIPE_NAME, name);
        values.put(DataBaseHelper.COL_DESCRIPTION, description);
        values.put(DataBaseHelper.COL_IMAGE_PATH, imagePath);

        return db.insertWithOnConflict(
                DataBaseHelper.TABLE_RECIPES,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    /**
     * Checks if a recipe with the given name exists.
     */
    public boolean exists(String name) {
        Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                new String[]{DataBaseHelper.COL_RECIPE_ID},
                DataBaseHelper.COL_RECIPE_NAME + " = ?",
                new String[]{name},
                null, null, null
        );
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    /**
     * Finds a recipe by its name.
     */
    public Recipe findByName(String name) {
        Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                null,
                DataBaseHelper.COL_RECIPE_NAME + " = ?",
                new String[]{name},
                null, null, null,
                "1"
        );
        Recipe recipe = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RECIPE_ID));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_DESCRIPTION));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_IMAGE_PATH));

                recipe = new Recipe(name, description, imagePath);
                recipe.setId(id);
            }
            cursor.close();
        }
        return recipe;
    }

    /**
     * Updates a recipe based on its name.
     */
    public int updateByName(String name, String description, String imagePath) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_DESCRIPTION, description);
        values.put(DataBaseHelper.COL_IMAGE_PATH, imagePath);

        return db.update(
                DataBaseHelper.TABLE_RECIPES,
                values,
                DataBaseHelper.COL_RECIPE_NAME + " = ?",
                new String[]{name}
        );
    }

    /**
     * Deletes a recipe based on its name.
     */
    public int deleteByName(String name) {
        return db.delete(
                DataBaseHelper.TABLE_RECIPES,
                DataBaseHelper.COL_RECIPE_NAME + " = ?",
                new String[]{name}
        );
    }

    public Recipe findById(long id) {
        Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                null,
                DataBaseHelper.COL_RECIPE_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null,
                "1"
        );
        Recipe recipe = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RECIPE_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_DESCRIPTION));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_IMAGE_PATH));

                recipe = new Recipe(name, description, imagePath);
                recipe.setId((int) id);
            }
            cursor.close();
        }
        return recipe;
    }

    /**
     * Returns ALL recipes from DB, ordered by name.
     */
    public List<Recipe> getAll() {
        List<Recipe> result = new ArrayList<>();

        Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                null,
                null,
                null,
                null,
                null,
                DataBaseHelper.COL_RECIPE_NAME + " ASC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RECIPE_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RECIPE_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_DESCRIPTION));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_IMAGE_PATH));

                    Recipe r = new Recipe(name, description, imagePath);
                    r.setId(id);
                    result.add(r);
                }
            } finally {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * Simple wrapper used by RecipeCaloricBalanceActivity.
     */
    public Recipe getByName(String selectedName) {
        return findByName(selectedName);
    }

    /**
     * Row used for nutritional computation:
     * one ingredient + its quantity in grams for a given recipe.
     */
    public static class RecipeIngredientRow {
        public final Ingredient ingredient;
        public final double quantityInGrams;

        public RecipeIngredientRow(Ingredient ingredient, double quantityInGrams) {
            this.ingredient = ingredient;
            this.quantityInGrams = quantityInGrams;
        }
    }

    /**
     * Returns all ingredients + quantity (in grams) for a given recipe.
     *
     * ⚠️ Vérifie que les constantes TABLE_RECIPE_INGREDIENTS,
     * COL_RI_RECIPE_ID, COL_RI_INGREDIENT_ID, COL_RI_QUANTITY_G
     * existent bien dans ton DataBaseHelper, sinon adapte les noms.
     */
    public List<RecipeIngredientRow> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredientRow> result = new ArrayList<>();

        // On récupère : id ingrédient + quantité (en g) dans la table de liaison
        String[] columns = {
                DataBaseHelper.COL_RI_INGREDIENT_ID,
                DataBaseHelper.COL_RI_QUANTITY_G
        };

        Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPE_INGREDIENTS,
                columns,
                DataBaseHelper.COL_RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null, null, null
        );

        if (cursor != null) {
            IngredientDao ingredientDao = new IngredientDao(context);
            try {
                while (cursor.moveToNext()) {
                    long ingredientId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RI_INGREDIENT_ID));
                    double qtyGrams = cursor.getDouble(
                            cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RI_QUANTITY_G));

                    Ingredient ing = ingredientDao.findById(ingredientId);
                    if (ing != null) {
                        result.add(new RecipeIngredientRow(ing, qtyGrams));
                    }
                }
            } finally {
                cursor.close();
                ingredientDao.close();
            }
        }

        return result;
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        db.close();
    }
}
