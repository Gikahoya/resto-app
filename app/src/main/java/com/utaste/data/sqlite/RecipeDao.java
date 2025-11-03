package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.domain.recipe.Recipe;

public class RecipeDao {

    private final SQLiteDatabase db;

    public RecipeDao(Context context) {
        DataBaseHelper dbHelper = new DataBaseHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * Tries to insert a recipe. Because the 'name' column is UNIQUE,
     * this will fail if a recipe with the same name already exists.
     * @return The row ID of the new recipe, or -1 if the name already exists.
     */
    public long insertIfAbsent(String name, String description, String imagePath) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.REC_COL_NAME, name);
        values.put(DataBaseHelper.REC_COL_DESCRIPTION, description);
        values.put(DataBaseHelper.REC_COL_IMAGE_PATH, imagePath);

        return db.insertWithOnConflict(DataBaseHelper.TABLE_RECIPES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Checks if a recipe with the given name exists.
     * @param name The name to check.
     * @return true if the recipe exists, false otherwise.
     */
    public boolean exists(String name) {
        Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                new String[]{DataBaseHelper.REC_COL_ID},
                DataBaseHelper.REC_COL_NAME + " = ?",
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
     * @param name The name of the recipe to find.
     * @return A recipe or null if not found
     */
    public Recipe findByName(String name) {
        Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                null,
                DataBaseHelper.REC_COL_NAME + " = ?",
                new String[]{name},
                null, null, null, "1"
        );
        Recipe recipe = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_ID));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_DESCRIPTION));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.REC_COL_IMAGE_PATH));

                recipe = new Recipe(name, description, imagePath);
                recipe.setId(id);
            }
            cursor.close();
        }
        return recipe;
    }

    /**
     * Updates a recipe based on its name.
     * @return The number of rows affected (should be 1 or 0).
     */
    public int updateByName(String name, String description, String imagePath) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.REC_COL_DESCRIPTION, description);
        values.put(DataBaseHelper.REC_COL_IMAGE_PATH, imagePath);

        return db.update(
                DataBaseHelper.TABLE_RECIPES,
                values,
                DataBaseHelper.REC_COL_NAME + " = ?",
                new String[]{name}
        );
    }

    /**
     * Deletes a recipe based on its name.
     * @param name The name of the recipe to delete.
     * @return The number of rows deleted.
     */
    public int deleteByName(String name) {
        return db.delete(
                DataBaseHelper.TABLE_RECIPES,
                DataBaseHelper.REC_COL_NAME + " = ?",
                new String[]{name}
        );
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        db.close();
    }
}
