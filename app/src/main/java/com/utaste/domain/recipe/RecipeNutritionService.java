package com.utaste.domain.recipe;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.data.sqlite.DataBaseHelper;
import com.utaste.data.sqlite.IngredientDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Service qui lit la table recipe_ingredients + ingredients
 * et renvoie un RecipeNutritionSummary pour une recette donnée.
 */
public class RecipeNutritionService {

    private final DataBaseHelper dbHelper;
    private final IngredientDao ingredientDao;

    public RecipeNutritionService(Context context) {
        this.dbHelper = new DataBaseHelper(context);
        this.ingredientDao = new IngredientDao(context);
    }

    /**
     * Calcule le bilan nutritionnel pour une recette donnée.
     *
     * @param recipeId id de la recette (colonne REC_COL_ID)
     */
    public RecipeNutritionSummary computeForRecipeId(int recipeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<RecipeNutritionEntry> entries = new ArrayList<>();

        String[] cols = {
                DataBaseHelper.COL_RI_INGREDIENT_ID,
                DataBaseHelper.COL_RI_QUANTITY
        };
        String where = DataBaseHelper.COL_RI_RECIPE_ID + " = ?";
        String[] args = { String.valueOf(recipeId) };

        try (Cursor c = db.query(
                DataBaseHelper.TABLE_RECIPE_INGREDIENTS,
                cols,
                where,
                args,
                null, null, null
        )) {
            if (c != null && c.moveToFirst()) {
                do {
                    int ingredientId =
                            c.getInt(c.getColumnIndexOrThrow(DataBaseHelper.COL_RI_INGREDIENT_ID));
                    double qty =
                            c.getDouble(c.getColumnIndexOrThrow(DataBaseHelper.COL_RI_QUANTITY));

                    Ingredient ing = ingredientDao.getById(ingredientId);
                    if (ing != null) {
                        // qty est interprété comme des grammes pour NutrtionFact
                        entries.add(new RecipeNutritionEntry(ing, qty));
                    }
                } while (c.moveToNext());
            }
        }

        return NutritionCalculator.computeSummary(entries);
    }

    public void close() {
        ingredientDao.close();
        dbHelper.close();
    }
}
