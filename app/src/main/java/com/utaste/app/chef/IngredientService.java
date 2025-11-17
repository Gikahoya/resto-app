package com.utaste.app.chef;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.utaste.data.sqlite.DataBaseHelper;

/**
 * Service pour gérer les INGREDIENTS côté Chef.
 *
 * Rôle :
 *  1. Retrouver ou créer un ingrédient à partir de son QR code.
 *  2. Associer cet ingrédient à une recette dans la table "recipe_ingredients".
 *  3. Enregistrer la QUANTITÉ utilisée pour cette recette.
 *
 * Idée centrale :
 *   - le QR code identifie l’ingrédient (TABLE_INGREDIENTS)
 *   - la table "recipe_ingredients" stocke la quantité pour une recette donnée
 *
 * On passe par DataBaseHelper pour :
 *   - avoir un seul endroit où les noms de colonnes / tables sont définis
 *   - gérer la création et la mise à jour du schéma SQLite
 */
public class IngredientService {

    /** Accès central à la base SQLite. */
    private final DataBaseHelper dbHelper;

    /**
     * Constructeur.
     *
     * @param context Contexte Android (Activity, Application…).
     *                On le garde juste pour créer le DataBaseHelper.
     */
    public IngredientService(@Nullable Context context) {
        this.dbHelper = new DataBaseHelper(context);
    }

    // =========================================================================
    //  API publique utilisée par l’UI (Activity)
    // =========================================================================

    /**
     * Ajoute (ou met à jour) un ingrédient dans une recette,
     * en partant du NOM de la recette.
     *
     * Utilisé quand l’écran "Create/Modify/Delete Recipe"
     * ne connaît que le name de la recette.
     *
     * @param recipeName     Nom de la recette (doit exister dans TABLE_RECIPES.name)
     * @param ingredientName Nom affiché de l’ingrédient (ex: "Spaghetti")
     * @param qrCode         QR code scanné (peut être réutilisé plus tard)
     * @param quantity       Quantité utilisée pour cette recette (ex: 100.0)
     * @param unit           Unité (ex: "g", "mL", "pc") – optionnel, peut être null
     *
     * @return true si tout s’est bien passé, false sinon
     */
    public boolean addIngredientToRecipeFromQrByRecipeName(
            String recipeName,
            String ingredientName,
            String qrCode,
            double quantity,
            String unit
    ) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1) On récupère l’ID de la recette à partir de son nom.
        long recipeId = getRecipeIdByName(db, recipeName);
        if (recipeId == -1L) {
            // Recette introuvable → impossible de lier l’ingrédient.
            return false;
        }

        // 2) On délègue au cœur de la logique, qui travaille avec un recipeId.
        return addIngredientToRecipeFromQr(recipeId, ingredientName, qrCode, quantity, unit);
    }

    /**
     * Variante quand on connaît déjà l’ID de la recette.
     *
     * @param recipeId       ID de la recette (PRIMARY KEY dans TABLE_RECIPES)
     * @param ingredientName Nom de l’ingrédient
     * @param qrCode         QR code scanné
     * @param quantity       Quantité pour cette recette
     * @param unit           Unité ("g", "mL", ...), optionnelle
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
            // 1) On récupère (ou crée) l’ingrédient à partir du QR code.
            long ingredientId = getOrInsertIngredient(
                    db,
                    ingredientName,
                    qrCode,
                    unit,
                    now
            );

            // 2) On crée / met à jour la relation recette <-> ingrédient
            //    avec la quantité.
            insertOrUpdateRecipeIngredient(
                    db,
                    recipeId,
                    ingredientId,
                    quantity
            );

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            // En production on utiliserait un vrai logger.
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * À appeler depuis une Activity/Fragment quand tu n'utilises plus le service,
     * pour libérer proprement la connexion SQLite.
     */
    public void close() {
        dbHelper.close();
    }

    // =========================================================================
    //  Helpers PRIVÉS — détails d’implémentation
    // =========================================================================

    /**
     * Cherche l’ID d’une recette par son nom.
     *
     * @param db         base SQLite déjà ouverte
     * @param recipeName nom EXACT (COL_RECIPE_NAME)
     *
     * @return l’ID (COL_RECIPE_ID) ou -1 si aucune recette correspondante
     */
    private long getRecipeIdByName(SQLiteDatabase db, String recipeName) {
        long id = -1L;

        // Colonnes que l’on veut récupérer
        String[] columns = { DataBaseHelper.COL_RECIPE_ID };

        // Clause WHERE : "name = ?"
        String selection = DataBaseHelper.COL_RECIPE_NAME + " = ?";
        String[] args = { recipeName };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_RECIPES,
                columns,
                selection,
                args,
                null,
                null,
                null
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
     * Retourne l’ID d’un ingrédient existant pour ce QR code,
     * ou insère un nouvel ingrédient si le QR n’est pas encore connu.
     *
     * @param db      base SQLite
     * @param name    nom de l’ingrédient
     * @param qrCode  QR code scanné
     * @param unit    unité ("g", "mL", "pc", etc.) — peut être null
     * @param now     timestamp (System.currentTimeMillis())
     *
     * @return ID de l’ingrédient dans TABLE_INGREDIENTS
     */
    private long getOrInsertIngredient(SQLiteDatabase db,
                                       String name,
                                       String qrCode,
                                       String unit,
                                       long now) {

        // 1) Essayer de trouver l’ingrédient par QR code
        String[] columns = { DataBaseHelper.COL_ID };
        String selection = DataBaseHelper.COL_QR_CODE + " = ?";
        String[] args = { qrCode };

        try (Cursor cursor = db.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                columns,
                selection,
                args,
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                // Ingrédient déjà connu → on retourne son id
                return cursor.getLong(
                        cursor.getColumnIndexOrThrow(DataBaseHelper.COL_ID)
                );
            }
        }

        // 2) Pas trouvé → on crée un nouvel ingrédient
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_NAME, name);
        values.put(DataBaseHelper.COL_QR_CODE, qrCode);
        values.put(DataBaseHelper.COL_UNIT, unit);      // peut être null
        values.put(DataBaseHelper.COL_CREATED_AT, now);
        values.put(DataBaseHelper.COL_UPDATED_AT, now);

        // amount n’est pas forcément connu ici → on le laisse à NULL ou 0
        // selon la logique souhaitée.

        return db.insertOrThrow(
                DataBaseHelper.TABLE_INGREDIENTS,
                null,
                values
        );
    }

    /**
     * Crée ou met à jour la ligne dans "recipe_ingredients"
     * pour la paire (recette, ingrédient).
     *
     * Si une ligne existe déjà pour ce couple, on met simplement à jour la quantité.
     */
    private void insertOrUpdateRecipeIngredient(SQLiteDatabase db,
                                                long recipeId,
                                                long ingredientId,
                                                double quantity) {

        // WHERE recipe_id = ? AND ingredient_id = ?
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
                null,
                null,
                null
        )) {
            ContentValues values = new ContentValues();
            values.put(DataBaseHelper.COL_RI_RECIPE_ID, recipeId);
            values.put(DataBaseHelper.COL_RI_INGREDIENT_ID, ingredientId);
            values.put(DataBaseHelper.COL_RI_QUANTITY, quantity);

            if (cursor != null && cursor.moveToFirst()) {
                // Une ligne existe déjà → on UPDATE la quantité
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
                // Aucun lien encore → on INSERT
                db.insertOrThrow(
                        DataBaseHelper.TABLE_RECIPE_INGREDIENTS,
                        null,
                        values
                );
            }
        }
    }
}
