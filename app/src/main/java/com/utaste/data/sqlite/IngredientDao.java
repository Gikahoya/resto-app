package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.NutritionFact;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) pour la table "ingredients".
 *
 * Cette classe encapsule TOUT l'accès à la table SQLite "ingredients".
 *
 * Schéma de la table (voir DataBaseHelper) :
 *   - id          (INTEGER, PK AUTOINCREMENT)
 *   - name        (TEXT, NOT NULL)
 *   - qr_code     (TEXT, nullable)
 *   - carbs_100g  (REAL, nullable)
 *   - protein_100g(REAL, nullable)
 *   - fat_100g    (REAL, nullable)
 *   - fiber_100g  (REAL, nullable)
 *   - salt_100g   (REAL, nullable)
 *   - created_at  (INTEGER, epoch millis)
 *   - updated_at  (INTEGER, epoch millis)
 */
public class IngredientDao {

    /** Accès à la base SQLite via helper. */
    private final DataBaseHelper dbHelper;

    /**
     * Constructeur.
     *
     * @param context Contexte Android (Activity, Application, etc.)
     */
    public IngredientDao(Context context) {
        this.dbHelper = new DataBaseHelper(context);
    }

    /**
     * Convertit la ligne courante du Cursor en objet métier {@link Ingredient}.
     *
     * IMPORTANT : le Cursor DOIT déjà être positionné sur une ligne valide
     * (après un moveToFirst() ou moveToNext()).
     */
    private Ingredient mapCursorToIngredient(Cursor c) {
        Ingredient ing = new Ingredient();

        int id = c.getInt(c.getColumnIndexOrThrow(DataBaseHelper.COL_ID));
        String name = c.getString(c.getColumnIndexOrThrow(DataBaseHelper.COL_NAME));
        String qrCode = c.getString(c.getColumnIndexOrThrow(DataBaseHelper.COL_QR_CODE));
        long createdAt = c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.COL_CREATED_AT));
        long updatedAt = c.getLong(c.getColumnIndexOrThrow(DataBaseHelper.COL_UPDATED_AT));

        // --- Nouveau : valeurs nutritionnelles / 100 g ---
        double carbs100   = safeGetDouble(c, DataBaseHelper.ING_COL_CARBS_100G);
        double protein100 = safeGetDouble(c, DataBaseHelper.ING_COL_PROTEIN_100G);
        double fat100     = safeGetDouble(c, DataBaseHelper.ING_COL_FAT_100G);
        double fiber100   = safeGetDouble(c, DataBaseHelper.ING_COL_FIBER_100G);
        double salt100    = safeGetDouble(c, DataBaseHelper.ING_COL_SALT_100G);

        // On construit l'objet de domaine
        ing.setId(id);
        ing.setName(name);
        ing.setQrCode(qrCode);
        ing.setCreatedAt(createdAt);
        ing.setUpdatedAt(updatedAt);

        // On attache un NutritionFact, même si c'est tout à 0.
        NutritionFact nf = new NutritionFact(
                carbs100,
                protein100,
                fat100,
                fiber100,
                salt100
        );
        ing.setNutritionFact(nf);

        return ing;
    }

    /**
     * Helper pour éviter les crash si la colonne est NULL.
     */
    private double safeGetDouble(Cursor c, String columnName) {
        int idx = c.getColumnIndex(columnName);
        if (idx == -1) return 0.0;
        if (c.isNull(idx)) return 0.0;
        return c.getDouble(idx);
    }

    // ============================================================
    //  CRUD de base
    // ============================================================

    /**
     * Insère un nouvel ingrédient dans la table.
     *
     * @param ingredient objet métier à insérer
     * @return id SQLite nouvellement généré, ou -1 en cas d'erreur.
     */
    public long insert(Ingredient ingredient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_NAME, ingredient.getName());
        values.put(DataBaseHelper.COL_QR_CODE, ingredient.getQrCode());
        values.put(DataBaseHelper.COL_CREATED_AT, now);
        values.put(DataBaseHelper.COL_UPDATED_AT, now);

        // --- Nouveau : nutrition / 100 g ---
        NutritionFact nf = ingredient.getNutritionFact();
        if (nf != null) {
            values.put(DataBaseHelper.ING_COL_CARBS_100G,   nf.getCarbsPer100g());
            values.put(DataBaseHelper.ING_COL_PROTEIN_100G, nf.getProteinPer100g());
            values.put(DataBaseHelper.ING_COL_FAT_100G,     nf.getFatPer100g());
            values.put(DataBaseHelper.ING_COL_FIBER_100G,   nf.getFiberPer100g());
            values.put(DataBaseHelper.ING_COL_SALT_100G,    nf.getSaltPer100g());
        }

        long id = db.insert(DataBaseHelper.TABLE_INGREDIENTS, null, values);

        if (id != -1) {
            ingredient.setId((int) id);
            ingredient.setCreatedAt(now);
            ingredient.setUpdatedAt(now);
        }
        return id;
    }

    /**
     * Met à jour un ingrédient existant (repéré par son id).
     *
     * @param ingredient objet à mettre à jour (id > 0 obligatoire)
     * @return nombre de lignes modifiées (0 si l'id n'existe pas).
     */
    public int update(Ingredient ingredient) {
        if (ingredient.getId() <= 0) return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_NAME, ingredient.getName());
        values.put(DataBaseHelper.COL_QR_CODE, ingredient.getQrCode());
        values.put(DataBaseHelper.COL_UPDATED_AT, now);

        // --- Nouveau : nutrition / 100 g ---
        NutritionFact nf = ingredient.getNutritionFact();
        if (nf != null) {
            values.put(DataBaseHelper.ING_COL_CARBS_100G,   nf.getCarbsPer100g());
            values.put(DataBaseHelper.ING_COL_PROTEIN_100G, nf.getProteinPer100g());
            values.put(DataBaseHelper.ING_COL_FAT_100G,     nf.getFatPer100g());
            values.put(DataBaseHelper.ING_COL_FIBER_100G,   nf.getFiberPer100g());
            values.put(DataBaseHelper.ING_COL_SALT_100G,    nf.getSaltPer100g());
        }

        int rows = db.update(
                DataBaseHelper.TABLE_INGREDIENTS,
                values,
                DataBaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(ingredient.getId())}
        );

        if (rows > 0) {
            ingredient.setUpdatedAt(now);
        }
        return rows;
    }

    /**
     * Supprime un ingrédient par id.
     *
     * @param id identifiant SQLite de l'ingrédient
     * @return nombre de lignes supprimées (0 ou 1).
     */
    public int deleteById(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DataBaseHelper.TABLE_INGREDIENTS,
                DataBaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    /**
     * Récupère un ingrédient par id.
     *
     * @param id identifiant de l'ingrédient
     * @return l'ingrédient ou null s'il n'existe pas.
     */
    @Nullable
    public Ingredient getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor c = db.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                null,
                DataBaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        )) {
            if (c != null && c.moveToFirst()) {
                return mapCursorToIngredient(c);
            }
        }
        return null;
    }

    /**
     * Récupère un ingrédient par QR code.
     * Utile pour le scan QR côté Chef.
     *
     * @param qrCode code QR exact à chercher
     * @return l'ingrédient ou null si aucun trouvé.
     */
    @Nullable
    public Ingredient getByQrCode(String qrCode) {
        if (qrCode == null) return null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor c = db.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                null,
                DataBaseHelper.COL_QR_CODE + " = ?",
                new String[]{qrCode},
                null, null, null
        )) {
            if (c != null && c.moveToFirst()) {
                return mapCursorToIngredient(c);
            }
        }
        return null;
    }

    /**
     * Renvoie la liste complète des ingrédients triés par nom (ordre alphabétique).
     */
    public List<Ingredient> getAll() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor c = db.query(
                DataBaseHelper.TABLE_INGREDIENTS,
                null,
                null,
                null,
                null,
                null,
                DataBaseHelper.COL_NAME + " COLLATE NOCASE ASC"
        )) {
            if (c != null && c.moveToFirst()) {
                do {
                    list.add(mapCursorToIngredient(c));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    // ============================================================
    //  Méthodes de compatibilité avec l'ancien code
    // ============================================================

    public List<Ingredient> getAllIngredients() {
        return getAll();
    }

    public long insertIngredient(Ingredient ingredient) {
        return insert(ingredient);
    }

    public int updateIngredient(int id, Ingredient ingredient) {
        ingredient.setId(id);
        return update(ingredient);
    }

    // ============================================================
    //  Fermeture
    // ============================================================

    /**
     * Ferme le helper SQLite si tu veux libérer la ressource.
     */
    public void close() {
        dbHelper.close();
    }
}
