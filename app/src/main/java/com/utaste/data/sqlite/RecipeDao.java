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

    // =========================================================
    // CRUD de base
    // =========================================================

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
        Cursor cursor = null;
        try {
            cursor = db.query(
                    DataBaseHelper.TABLE_RECIPES,
                    new String[]{DataBaseHelper.REC_COL_ID},
                    DataBaseHelper.REC_COL_NAME + " = ?",
                    new String[]{name},
                    null, null, null
            );
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public Recipe findByName(String name) {
        Cursor cursor = null;
        Recipe recipe = null;

        try {
            cursor = db.query(
                    DataBaseHelper.TABLE_RECIPES,
                    null,
                    DataBaseHelper.REC_COL_NAME + " = ?",
                    new String[]{name},
                    null, null, null,
                    "1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_ID));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_DESCRIPTION));
                String imagePath   = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_IMAGE_PATH));

                recipe = new Recipe(name, description, imagePath);
                recipe.setId(id);
            }
        } finally {
            if (cursor != null) cursor.close();
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

    public Recipe findById(long recipeId) {
        Cursor cursor = null;
        Recipe recipe = null;

        try {
            cursor = db.query(
                    DataBaseHelper.TABLE_RECIPES,
                    null, // null pour récupérer toutes les colonnes
                    DataBaseHelper.REC_COL_ID + " = ?", // Clause WHERE sur l'ID
                    new String[]{String.valueOf(recipeId)}, // Argument de la clause WHERE
                    null, null, null,
                    "1" // Limiter à un seul résultat
            );

            if (cursor != null && cursor.moveToFirst()) {
                // On utilise les constantes de DataBaseHelper pour plus de robustesse
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_DESCRIPTION));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_IMAGE_PATH));

                // On crée l'objet Recipe avec les données trouvées
                recipe = new Recipe(name, description, imagePath);
                // On n'oublie pas de définir son ID
                recipe.setId(recipeId);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return recipe;
    }


    // =========================================================
    // 1) Récupérer toutes les recettes
    // =========================================================

    public List<Recipe> getAll() {
        List<Recipe> list = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(
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
            }
        } catch (Exception e) {
            e.printStackTrace(); // évite le crash brutal
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    /** Liste de noms de recettes (au cas où un écran en a besoin) */
    public List<String> getAllNames() {
        List<String> names = new ArrayList<>();
        for (Recipe r : getAll()) {
            names.add(r.getName());
        }
        return names;
    }

    /** Alias plus simple pour les autres classes */
    public Recipe getByName(String selectedName) {
        return findByName(selectedName);
    }

    public boolean deleteIngredientFromRecipe(long id, long id1) {
        return false;
    }

    public boolean updateIngredientQuantityForRecipe(long recipeId, long ingredientId, double newQuantity) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_RI_QUANTITY, newQuantity);

        String whereClause = DataBaseHelper.COL_RI_RECIPE_ID + " = ? AND " +
                DataBaseHelper.COL_RI_INGREDIENT_ID + " = ?";
        String[] whereArgs = {
                String.valueOf(recipeId),
                String.valueOf(ingredientId)
        };

        int rowsAffected = db.update(
                DataBaseHelper.TABLE_RECIPE_INGREDIENTS,
                values,
                whereClause,
                whereArgs
        );

        // La mise à jour est réussie si exactement une ligne a été modifiée.
        return rowsAffected == 1;
    }

    // =========================================================
    // 2) Recette → Ingrédients + quantités
    // =========================================================

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
        Cursor c = null;

        String sql =
                "SELECT i." + DataBaseHelper.ING_COL_ID + "      AS ing_id, " +
                        "i." + DataBaseHelper.ING_COL_NAME + "    AS ing_name, " +
                        "i." + DataBaseHelper.ING_COL_QR_CODE + " AS ing_qr, " +
                        "ri." + DataBaseHelper.COL_RI_QUANTITY + " AS qty " +
                        "FROM " + DataBaseHelper.TABLE_RECIPE_INGREDIENTS + " ri " +
                        "JOIN " + DataBaseHelper.TABLE_INGREDIENTS + " i " +
                        " ON ri." + DataBaseHelper.COL_RI_INGREDIENT_ID + " = i." + DataBaseHelper.ING_COL_ID +
                        " WHERE ri." + DataBaseHelper.COL_RI_RECIPE_ID + " = ?";

        try {
            c = db.rawQuery(sql, new String[]{ String.valueOf(recipeId) });

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
            }
        } catch (Exception e) {
            e.printStackTrace(); // évite un crash si la DB a un souci
        } finally {
            if (c != null) c.close();
        }

        return rows;
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
