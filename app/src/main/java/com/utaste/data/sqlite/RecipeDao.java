package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.domain.recipe.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * RecipeDao — Gère les opérations CRUD (Create, Read, Update, Delete)
 * sur la table "recipes" dans la base de données SQLite.
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


    //   ajouter une recette
    //
    public long insertRecipe(Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put("name", recipe.getName());
        values.put("description", recipe.getDescription());
        values.put("image_path", recipe.getImagePath());

        return db.insert("recipes", null, values);
    }


    //   récupérer une recette par ID

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


    public int updateRecipe(Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put("name", recipe.getName());
        values.put("description", recipe.getDescription());
        values.put("image_path", recipe.getImagePath());

        return db.update("recipes", values, "id = ?", new String[]{String.valueOf(recipe.getId())});
    }


    //   DELETE : supprimer une recette

    public int deleteRecipe(int id) {
        return db.delete("recipes", "id = ?", new String[]{String.valueOf(id)});
    }


    //   CLOSE : fermer la base

    public void close() {
        db.close();
    }


    /**
     * Vérifie si une recette existe déjà selon son nom.
     */
    public boolean existsByName(String name) {
        try (Cursor c = db.query(
                "recipes",
                new String[]{"id"},
                "name = ?",
                new String[]{ name },
                null, null, null
        )) {
            return c != null && c.moveToFirst();
        }
    }

    /**
     * Insère une recette seulement si elle n’existe pas déjà.
     * Retourne l’ID de la ligne créée, ou -1 si le nom existe déjà.
     */
    public long insertIfAbsent(String name, String description, String imagePath) {
        if (existsByName(name)) return -1; // déjà présent
        Recipe recipe = new Recipe(name, description, imagePath);
        return insertRecipe(recipe);
    }

    /**
     * Met à jour une recette existante en fonction de son nom.
     * Retourne le nombre de lignes modifiées.
     */
    public int updateByName(String name, String newDescription, String newImagePath) {
        try (Cursor c = db.query(
                "recipes",
                new String[]{"id"},
                "name = ?",
                new String[]{ name },
                null, null, null
        )) {
            if (c == null || !c.moveToFirst()) return 0; // pas trouvé
            int id = c.getInt(c.getColumnIndexOrThrow("id"));
            Recipe r = new Recipe(name, newDescription, newImagePath);
            r.setId(id);
            return updateRecipe(r);
        }
    }

    /**
     * Supprime une recette selon son nom.
     * Retourne le nombre de lignes supprimées.
     */
    public int deleteByName(String name) {
        try (Cursor c = db.query(
                "recipes",
                new String[]{"id"},
                "name = ?",
                new String[]{ name },
                null, null, null
        )) {
            if (c == null || !c.moveToFirst()) return 0; // rien trouvé
            int id = c.getInt(c.getColumnIndexOrThrow("id"));
            return deleteRecipe(id);
        }
    }
}
