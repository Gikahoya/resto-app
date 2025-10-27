package com.utaste.domain.recipe;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
/**
 * Classe SQLiteOpenHelper pour gérer la base de données des ingrédients.
 * Crée une table simple avec :
 *  - id (PRIMARY KEY)
 *  - name (TEXT)
 *  - qr_code (TEXT)
 *  - amount (REAL)
 *  - unit (TEXT)
 *  - created_at / updated_at (timestamps)
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "utaste.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QR_CODE = "qr_code";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_UNIT = "unit";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    // ===============================
    //   SQL : création de la table
    // ===============================
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_INGREDIENTS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_QR_CODE + " TEXT, " +
                    COL_AMOUNT + " REAL NOT NULL, " +
                    COL_UNIT + " TEXT NOT NULL, " +
                    COL_CREATED_AT + " INTEGER NOT NULL, " +
                    COL_UPDATED_AT + " INTEGER NOT NULL" +
                    ");";

    // Index pour accélérer les recherches par nom ou QR
    private static final String SQL_INDEX_NAME =
            "CREATE INDEX IF NOT EXISTS idx_ing_name ON " + TABLE_INGREDIENTS +
                    " (" + COL_NAME + " COLLATE NOCASE);";

    private static final String SQL_INDEX_QR =
            "CREATE INDEX IF NOT EXISTS idx_ing_qr ON " + TABLE_INGREDIENTS +
                    " (" + COL_QR_CODE + ");";

    // ===============================
    //   Constructeur
    // ===============================
    public DataBaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ===============================
    //   Configuration de la base
    // ===============================
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Si un jour tu ajoutes des clés étrangères :
        // db.setForeignKeyConstraintsEnabled(true);
    }

    // ===============================
    //   Création initiale de la DB
    // ===============================
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
        db.execSQL(SQL_INDEX_NAME);
        db.execSQL(SQL_INDEX_QR);
    }

    // ===============================
    //   Mise à jour (migration simple)
    // ===============================
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Pour l’instant : recrée la table si version différente
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        onCreate(db);
    }

}
