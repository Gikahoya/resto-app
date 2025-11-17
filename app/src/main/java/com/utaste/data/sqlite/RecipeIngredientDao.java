package com.utaste.data.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.NutritionFact;
import com.utaste.domain.recipe.RecipeNutritionEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO dédié à la table de liaison "recipe_ingredients".
 *
 * Son job ici : construire la liste d'entrées nutritionnelles
 * (RecipeNutritionEntry) pour une recette donnée, en joignant :
 *  - recipes
 *  - recipe_ingredients
 *  - ingredients (avec leurs NutritionFact)
 */
public class RecipeIngredientDao {

    private final DataBaseHelper dbHelper;

    public RecipeIngredientDao(Context context) {
        this.dbHelper = new DataBaseHelper(context);
    }

    /**
     * Récupère toutes les associations (ingrédient + quantité)
     * pour une recette donnée (identifiée par son nom),
     * et les convertit en liste de RecipeNutritionEntry.
     *
     * @param recipeName nom EXACT de la recette (colonne REC_COL_NAME)
     */
    public List<RecipeNutritionEntry> getNutritionEntriesForRecipe(String recipeName) {
        List<RecipeNutritionEntry> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // On fait un JOIN entre recipes, recipe_ingredients et ingredients.
        String sql =
                "SELECT " +
                        "ri." + DataBaseHelper.COL_RI_QUANTITY + " AS qty, " +
                        "i." + DataBaseHelper.COL_ID          + " AS ing_id, " +
                        "i." + DataBaseHelper.COL_NAME        + " AS ing_name, " +
                        "i." + DataBaseHelper.COL_QR_CODE     + " AS ing_qr, " +
                        "i." + DataBaseHelper.COL_AMOUNT      + " AS ing_amount, " +
                        "i." + DataBaseHelper.COL_UNIT        + " AS ing_unit, " +
                        "i." + DataBaseHelper.COL_CARBS_100G   + " AS ing_carbs100, " +
                        "i." + DataBaseHelper.COL_PROTEIN_100G + " AS ing_protein100, " +
                        "i." + DataBaseHelper.COL_FAT_100G     + " AS ing_fat100, " +
                        "i." + DataBaseHelper.COL_FIBER_100G   + " AS ing_fiber100, " +
                        "i." + DataBaseHelper.COL_SALT_100G    + " AS ing_salt100 " +
                        "FROM " + DataBaseHelper.TABLE_RECIPE_INGREDIENTS + " ri " +
                        "JOIN " + DataBaseHelper.TABLE_RECIPES + " r " +
                        "ON r." + DataBaseHelper.COL_RECIPE_ID + " = ri." + DataBaseHelper.COL_RI_RECIPE_ID + " " +
                        "JOIN " + DataBaseHelper.TABLE_INGREDIENTS + " i " +
                        "ON i." + DataBaseHelper.COL_ID + " = ri." + DataBaseHelper.COL_RI_INGREDIENT_ID + " " +
                        "WHERE r." + DataBaseHelper.COL_RECIPE_NAME + " = ?;";

        try (Cursor c = db.rawQuery(sql, new String[]{ recipeName })) {
            if (c != null && c.moveToFirst()) {
                do {
                    double qty = c.getDouble(c.getColumnIndexOrThrow("qty"));

                    Ingredient ing = new Ingredient();
                    ing.setId(c.getInt(c.getColumnIndexOrThrow("ing_id")));
                    ing.setName(c.getString(c.getColumnIndexOrThrow("ing_name")));
                    ing.setQrCode(c.getString(c.getColumnIndexOrThrow("ing_qr")));
                    ing.setAmount(c.getDouble(c.getColumnIndexOrThrow("ing_amount")));
                    String unitDb = c.getString(c.getColumnIndexOrThrow("ing_unit"));
                    ing.setUnit(Ingredient.unitFromDb(unitDb));

                    // Nutrition / 100g pour cet ingrédient
                    double carbs100   = c.getDouble(c.getColumnIndexOrThrow("ing_carbs100"));
                    double protein100 = c.getDouble(c.getColumnIndexOrThrow("ing_protein100"));
                    double fat100     = c.getDouble(c.getColumnIndexOrThrow("ing_fat100"));
                    double fiber100   = c.getDouble(c.getColumnIndexOrThrow("ing_fiber100"));
                    double salt100    = c.getDouble(c.getColumnIndexOrThrow("ing_salt100"));

                    NutritionFact nf = new NutritionFact(
                            carbs100,
                            protein100,
                            fat100
                    );
                    nf.setFibersPer100g(fiber100);
                    nf.setSaltPer100g(salt100);
                    ing.setNutritionFact(nf);

                    // Ici, qty est la quantité utilisée dans la recette.
                    // Dans ton UI, tu l'appelles "Quantité (%)".
                    // Si tu interprètes ça comme "grammes pour 100 g de recette",
                    // alors qty = grammes d'ingrédient pour 100 g de recette.
                    RecipeNutritionEntry entry = new RecipeNutritionEntry(ing, qty);
                    result.add(entry);
                } while (c.moveToNext());
            }
        }

        return result;
    }

    public void close() {
        dbHelper.close();
    }
}
