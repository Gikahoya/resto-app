package com.utaste.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.data.sqlite.DataBaseHelper;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.data.sqlite.RecipeIngredientDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;
import com.utaste.domain.recipe.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

public class RecipeIngredientService {

    private final DataBaseHelper dbHelper;
    private final RecipeIngredientDao recipeIngredientDao;
    private final IngredientDao ingredientDao;
    private final RecipeDao recipeDao;

    public RecipeIngredientService(Context context) {
        this.dbHelper = new DataBaseHelper(context);
        this.recipeIngredientDao = new RecipeIngredientDao(context);
        this.ingredientDao = new IngredientDao(context);
        this.recipeDao = new RecipeDao(context);
    }

    /**
     * Méthode principale appelée par l'interface (Activity).
     * Elle prend des NOMS (String), trouve les IDs correspondants, et sauvegarde le lien.
     */
    public boolean addIngredientToRecipeByNames(
            String recipeName,
            String ingredientName,
            double quantity,
            String unit
    ) {
        // 1. Trouver l'objet Recette via son nom
        Recipe recipe = recipeDao.findByName(recipeName);
        if (recipe == null) {
            return false; // La recette n'existe pas
        }

        // 2. Trouver l'objet Ingrédient via son nom
        // On parcourt la liste car IngredientDao n'a pas forcément de méthode findByName optimisée
        Ingredient ingredient = null;
        for (Ingredient i : ingredientDao.getAll()) {
            if (i.getName().equalsIgnoreCase(ingredientName)) {
                ingredient = i;
                break;
            }
        }

        if (ingredient == null) {
            return false; // L'ingrédient n'existe pas
        }

        // 3. Appeler le DAO pour insérer le lien avec les IDs trouvés
        recipeIngredientDao.insertOrUpdate(recipe.getId(), ingredient.getId(), quantity, unit);

        return true;
    }

    // =========================================================================
    //  LECTURE DES DONNÉES (Pour afficher la liste des ingrédients d'une recette)
    // =========================================================================

    public List<RecipeIngredient> getIngredientsByRecipeId(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // ATTENTION : On filtre sur la colonne de liaison 'recipe_id'
        String selection = DataBaseHelper.COL_RI_RECIPE_ID + " = ?";
        String[] args = { String.valueOf(recipeId) };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPE_INGREDIENTS,
                null,
                selection,
                args,
                null, null, null
        )) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    RecipeIngredient ri = mapCursorToRecipeIngredient(cursor);
                    if (ri != null) {
                        list.add(ri);
                    }
                }
            }
        }
        return list;
    }

    // DANS RecipeIngredientService.java

    /**
     * Supprime un ingrédient d'une recette via leurs IDs.
     */
    public boolean removeIngredientFromRecipe(long recipeId, long ingredientId) {
        return recipeIngredientDao.deleteLink(recipeId, ingredientId);
    }

    /**
     * Transforme une ligne de la base de données en objet Java complet.
     */
    private RecipeIngredient mapCursorToRecipeIngredient(Cursor c) {
        // 1. Lire les IDs et infos dans la table de liaison
        // UTILISATION OBLIGATOIRE DES CONSTANTES COL_RI_...
        long rId = c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.COL_RI_RECIPE_ID));
        long iId = c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.COL_RI_INGREDIENT_ID));
        double qty = c.getDouble(c.getColumnIndexOrThrow(DataBaseHelper.COL_RI_QUANTITY));
        String unitStr = c.getString(c.getColumnIndexOrThrow(DataBaseHelper.COL_RI_UNIT));

        // 2. Récupérer les objets complets (Recette et Ingrédient) via leurs IDs
        // Note: Assurez-vous que recipeDao et ingredientDao ont une méthode getById() ou équivalent.
        // Si getById n'existe pas, il faut l'ajouter dans vos DAOs respectifs.
        Recipe recipe = recipeDao.findById(rId);
        Ingredient ingredient = ingredientDao.getById((int) iId);

        if (recipe == null || ingredient == null) {
            return null; // Donnée orpheline (ex: ingrédient supprimé), on ignore.
        }

        // 3. Retourner l'objet métier
        return new RecipeIngredient(recipe, ingredient, qty, unitStr);
    }

    public void close() {
        dbHelper.close();
        if (recipeDao != null) recipeDao.close();
        if (ingredientDao != null) ingredientDao.close();
        if (recipeIngredientDao != null) recipeIngredientDao.close();
    }
}