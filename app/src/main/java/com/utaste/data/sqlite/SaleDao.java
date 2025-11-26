package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.domain.sale.Sale;
import com.utaste.domain.sale.RecipeSalesSummary;

import java.util.ArrayList;
import java.util.List;

public class SaleDao {

    private final DataBaseHelper dbHelper;

    public SaleDao(Context context) {
        this.dbHelper = new DataBaseHelper(context);
    }

    public long insertSale(Sale sale) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_SALE_RECIPE_ID, sale.getRecipeId());
        values.put(DataBaseHelper.COL_SALE_RATING, sale.getRating());
        values.put(DataBaseHelper.COL_SALE_APPRECIATION, sale.getAppreciation());
        values.put(DataBaseHelper.COL_SALE_TIMESTAMP, sale.getTimestamp());

        return db.insert(DataBaseHelper.TABLE_SALES, null, values);
    }

    /**
     * Retourne la liste des recettes vendues avec :
     * - le nombre total de ventes (sales_count)
     * - la note moyenne (avg_rating)
     */
    public List<RecipeSalesSummary> getRecipeSalesSummary() {
        List<RecipeSalesSummary> result = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT " +
                        DataBaseHelper.TABLE_RECIPES + "." + DataBaseHelper.REC_COL_ID + " AS recipe_id, " +
                        DataBaseHelper.TABLE_RECIPES + "." + DataBaseHelper.REC_COL_NAME + " AS recipe_name, " +
                        "COUNT(" + DataBaseHelper.TABLE_SALES + "." + DataBaseHelper.COL_SALE_ID + ") AS sales_count, " +
                        "AVG(" + DataBaseHelper.TABLE_SALES + "." + DataBaseHelper.COL_SALE_RATING + ") AS avg_rating " +
                        "FROM " + DataBaseHelper.TABLE_SALES + " " +
                        "JOIN " + DataBaseHelper.TABLE_RECIPES + " ON " +
                        DataBaseHelper.TABLE_SALES + "." + DataBaseHelper.COL_SALE_RECIPE_ID +
                        " = " +
                        DataBaseHelper.TABLE_RECIPES + "." + DataBaseHelper.REC_COL_ID + " " +
                        "GROUP BY recipe_id, recipe_name " +
                        "ORDER BY sales_count DESC, avg_rating DESC";

        Cursor c = db.rawQuery(sql, null);

        if (c.moveToFirst()) {
            int idxRecipeId   = c.getColumnIndexOrThrow("recipe_id");
            int idxRecipeName = c.getColumnIndexOrThrow("recipe_name");
            int idxSalesCount = c.getColumnIndexOrThrow("sales_count");
            int idxAvgRating  = c.getColumnIndexOrThrow("avg_rating");

            do {
                long recipeId      = c.getLong(idxRecipeId);
                String recipeName  = c.getString(idxRecipeName);
                int salesCount     = c.getInt(idxSalesCount);
                double avgRating   = c.getDouble(idxAvgRating);

                result.add(new RecipeSalesSummary(recipeId, recipeName, salesCount, avgRating));
            } while (c.moveToNext());
        }

        c.close();
        db.close();

        return result;
    }
}
