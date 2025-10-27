package com.utaste.data.sqlite;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.utaste.domain.recipe.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) pour gérer les opérations CRUD sur les recettes.
 * Gère la table "recipes" de la base utaste.db.
 */
public class RecipeDao {

    private final SQLiteDatabase db;

    // ===============================
    //   Constructeur
    // ===============================
    public RecipeDao(Context context) {
        DataBaseHelper dbHelper = new DataBaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    // ===============================
    //   CREATE : ajouter une recette
    // ===============================
    public long insertRecipe(Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put("name", recipe.getName());
        values.put("description", recipe.getDescription());
        values.put("image_path", recipe.getImagePath());

        return db.insert("recipes", null, values);
    }

    // ===============================
    //   READ : récupérer une recette par ID
    // ===============================
    public Recipe getRecipeById(int id) {
        Cursor cursor = db.query(
                "recipes",
                new String[]{"id", "name", "description", "image_path"},
                "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Recipe recipe = null;
        if (cursor != null && cursor.moveToFirst()) {
            recipe = new Recipe(
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getString(cursor.getColumnIndexOrThrow("image_path"))
            );
            recipe.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            cursor.close();
        }
        return recipe;
    }

    // ===============================
    //   READ ALL : liste de toutes les recettes
    // ===============================
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        Cursor cursor = db.query("recipes",
                new String[]{"id", "name", "description", "image_path"},
                null, null, null, null,
                "name ASC");

        if (cursor.moveToFirst()) {
            do {
                Recipe recipe = new Recipe(
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image_path"))
                );
                recipe.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                recipes.add(recipe);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return recipes;
    }

    // ===============================
    //   UPDATE : modifier une recette
    // ===============================
    public int updateRecipe(Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put("name", recipe.getName());
        values.put("description", recipe.getDescription());
        values.put("image_path", recipe.getImagePath());

        return db.update("recipes", values, "id = ?", new String[]{String.valueOf(recipe.getId())});
    }

    // ===============================
    //   DELETE : supprimer une recette
    // ===============================
    public int deleteRecipe(int id) {
        return db.delete("recipes", "id = ?", new String[]{String.valueOf(id)});
    }

    // ===============================
    //   CLOSE : fermer la base
    // ===============================
    public void close() {
        db.close();
    }
}
