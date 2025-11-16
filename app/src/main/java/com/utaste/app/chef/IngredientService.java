package com.utaste.app.chef;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.data.sqlite.DataBaseHelper;

/**
 * Service pour gérer les ingrédients côté Chef.
 *
 * Il s'occupe de :
 *  1. Retrouver ou créer un ingrédient à partir de son QR code.
 *  2. Lier cet ingrédient à une recette dans la table {@code recipe_ingredients}.
 *  3. Enregistrer la quantité utilisée dans la recette.
 *
 * L'idée : le code QR identifie l'ingrédient, et on stocke la quantité
 * spécifique à la recette dans la table de lien.
 */
public class IngredientService {

    /** Accès central à la base SQLite. */
    private final DataBaseHelper dbHelper;

    public IngredientService(Context context) {
        this.dbHelper = new DataBaseHelper(context);
    }

    // ---------------------------------------------------------------------
    //  API publique
    // ---------------------------------------------------------------------

    /**
     * Ajoute (ou met à jour) un ingrédient pour une recette en utilisant :
     *  - le NOM de la recette
     *  - le nom de l'ingrédient (saisi)
     *  - le QR code scanné
     *  - la quantité
     *  - l'unité (optionnelle, ex: "g")
     *
     * Cette méthode est pratique côté UI, car dans l'écran tu as surtout
     * le nom de la recette, pas forcément son id.
     *
     * @return true si tout s'est bien passé, false sinon.
     */
    public boolean addIngredientToRecipeFromQrByRecipeName(
            String recipeName,
            String ingredientName,
            String qrCode,
            double quantity,
            String unit
    ) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1) On récupère l'ID de la recette à partir de son nom.
        long recipeId = getRecipeIdByName(db, recipeName);
        if (recipeId == -1L) {
            // Recette introuvable → on ne fait rien.
            return false;
        }

        // 2) On délègue au helper qui travaille avec l'ID.
        return addIngredientToRecipeFromQr(recipeId, ingredientName, qrCode, quantity, unit);
    }

    /**
     * Variante quand tu connais déjà l'ID de la recette.
     */
    public boolean addIngredientToRecipeFromQr(
            long recipeId,
            String ingredientName,
            String qrCode,
            double quantity,
            String unit
    ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();

        db.beginTransaction();
        try {
            // 1) Récupérer ou créer l'ingrédient à partir du QR code
            long ingredientId = getOrInsertIngredient(db, ingredientName, qrCode, unit, now);

            // 2) Créer / mettre à jour la relation recette <-> ingrédient avec la quantité
            insertOrUpdateRecipeIngredient(db, recipeId, ingredientId, quantity);

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            // En debug tu verras la stacktrace dans Logcat
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * À appeler par exemple dans onDestroy() d'une Activity ou d'un ViewModel.
     */
    public void close() {
        dbHelper.close();
    }

    // ---------------------------------------------------------------------
    //  Helpers privés
    // ---------------------------------------------------------------------

    /**
     * Cherche l'ID d'une recette via son nom.
     *
     * @return l'id si trouvé, -1 sinon.
     */
    private long getRecipeIdByName(SQLiteDatabase db, String recipeName) {
        long id = -1L;

        String[] columns   = { DataBaseHelper.COL_RECIPE_ID };
        String   selection = DataBaseHelper.COL_RECIPE_NAME + " = ?";
        String[] args      = { recipeName };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                columns,
                selection,
                args,
                null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RECIPE_ID)
                );
            }
        }
        return id;
    }

    /**
     * Retourne l'id d'un ingrédient existant pour ce QR code,
     * ou insère un nouvel ingrédient si le QR n'est pas encore connu.
     */
    private long getOrInsertIngredient(SQLiteDatabase db,
                                       String name,
                                       String qrCode,
                                       String unit,
                                       long now) {

        // ---- 1) Essayer de trouver l'ingrédient par QR code ----
        String[] columns   = { DataBaseHelper.COL_ID };
        String   selection = DataBaseHelper.COL_QR_CODE + " = ?";
        String[] args      = { qrCode };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                columns,
                selection,
                args,
                null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                // Ingrédient déjà connu → on retourne juste son id
                return cursor.getLong(
                        cursor.getColumnIndexOrThrow(DataBaseHelper.COL_ID)
                );
            }
        }

        // ---- 2) Pas trouvé → on crée un nouvel ingrédient ----
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_NAME, name);
        values.put(DataBaseHelper.COL_QR_CODE, qrCode);
        values.put(DataBaseHelper.COL_UNIT, unit);
        // amount peut rester null pour l’instant
        values.put(DataBaseHelper.COL_CREATED_AT, now);
        values.put(DataBaseHelper.COL_UPDATED_AT, now);

        return db.insertOrThrow(DataBaseHelper.TABLE_INGREDIENTS, null, values);
    }

    /**
     * Crée ou met à jour la ligne dans {@code recipe_ingredients} pour (recette, ingrédient).
     * Si une ligne existe déjà pour ce couple, on met simplement à jour la quantité.
     */
    private void insertOrUpdateRecipeIngredient(SQLiteDatabase db,
                                                long recipeId,
                                                long ingredientId,
                                                double quantity) {

        String selection = DataBaseHelper.COL_RI_RECIPE_ID + " = ? AND " +
                DataBaseHelper.COL_RI_INGREDIENT_ID + " = ?";
        String[] args = {
                String.valueOf(recipeId),
                String.valueOf(ingredientId)
        };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPE_INGREDIENTS,
                new String[]{ DataBaseHelper.COL_RI_ID },
                selection,
                args,
                null, null, null
        )) {
            ContentValues values = new ContentValues();
            values.put(DataBaseHelper.COL_RI_RECIPE_ID,    recipeId);
            values.put(DataBaseHelper.COL_RI_INGREDIENT_ID, ingredientId);
            values.put(DataBaseHelper.COL_RI_QUANTITY,     quantity);

            if (cursor != null && cursor.moveToFirst()) {
                // Ligne existe déjà → on fait un UPDATE
                long rowId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DataBaseHelper.COL_RI_ID)
                );
                db.update(
                        DataBaseHelper.TABLE_RECIPE_INGREDIENTS,
                        values,
                        DataBaseHelper.COL_RI_ID + " = ?",
                        new String[]{ String.valueOf(rowId) }
                );
            } else {
                // Aucun lien pour ce couple (recette, ingrédient) → INSERT
                db.insertOrThrow(DataBaseHelper.TABLE_RECIPE_INGREDIENTS, null, values);
            }
        }
    }
}
