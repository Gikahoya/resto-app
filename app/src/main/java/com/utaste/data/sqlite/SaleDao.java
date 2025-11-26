package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;
import com.utaste.domain.sale.Sale;

import java.util.ArrayList;
import java.util.List;

public class SaleDao {

    private final DataBaseHelper dbHelper;

    public SaleDao(Context context) { this.dbHelper = new DataBaseHelper(context); }

    public long insertSale(Sale sale) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_SALE_RECIPE_ID, sale.getRecipeId());
        values.put(DataBaseHelper.COL_SALE_RATING, sale.getRating());
        values.put(DataBaseHelper.COL_SALE_APPRECIATION, sale.getAppreciation());
        values.put(DataBaseHelper.COL_SALE_TIMESTAMP, sale.getTimestamp());

        return db.insert(DataBaseHelper.TABLE_SALES, null, values);
    }

}
