package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// SQLiteOpenHelper est supprimé, cette classe ne gère plus la création/mise à jour de la DB.
// Elle ne fait que l'utiliser.

/**
 * UserDao — accès simple à la table "users".
 * Utilise DataBaseHelper pour accéder à la base de données.
 */
public class UserDao {

    // Référence au gestionnaire central de la base de données.
    private final DataBaseHelper dbHelper;

    /**
     * Constructeur qui initialise le helper de base de données.
     * @param ctx Le contexte de l'application.
     */
    public UserDao(Context ctx) {
        this.dbHelper = new DataBaseHelper(ctx.getApplicationContext());
    }

    /**
     * Retourne un Cursor sur l'utilisateur avec cet email (ou un Cursor vide si absent).
     * @param email L'email de l'utilisateur à rechercher.
     * @return Un Cursor pointant sur le résultat.
     */
    public Cursor getByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + DataBaseHelper.TABLE_USERS + " WHERE " + DataBaseHelper.COL_USER_EMAIL + "=?",
                new String[]{ email }
        );
    }

    /**
     * Vérifie si un utilisateur avec cet email existe déjà.
     * @param email L'email à vérifier.
     * @return true si l'utilisateur existe, false sinon.
     */
    public boolean exists(String email) {
        try (Cursor c = getByEmail(email)) {
            return c != null && c.moveToFirst();
        }
    }

    /**
     * Insère un nouvel utilisateur si son email n'existe pas encore.
     * @return L'ID de la nouvelle ligne, ou -1 si l'utilisateur existait déjà ou en cas d'erreur.
     */
    public long insertIfAbsent(String first, String last, String email, String pwd, String role) {
        if (exists(email)) {
            return -1;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_USER_FIRST, first);
        cv.put(DataBaseHelper.COL_USER_LAST,  last);
        cv.put(DataBaseHelper.COL_USER_EMAIL, email);
        cv.put(DataBaseHelper.COL_USER_PWD,   pwd);
        cv.put(DataBaseHelper.COL_USER_ROLE,  role);
        return db.insert(DataBaseHelper.TABLE_USERS, null, cv);
    }

    /**
     * Met à jour le prénom et le nom pour un email donné.
     * @return Le nombre de lignes modifiées (0 ou 1).
     */
    public int updateProfile(String email, String first, String last) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_USER_FIRST, first);
        cv.put(DataBaseHelper.COL_USER_LAST,  last);
        // Clause WHERE
        return db.update(DataBaseHelper.TABLE_USERS, cv, DataBaseHelper.COL_USER_EMAIL + "=?", new String[]{ email });
    }

    /**
     * Change le mot de passe pour un email donné.
     * @return Le nombre de lignes modifiées (0 ou 1).
     */
    public int resetPassword(String email, String newPwd) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_USER_PWD, newPwd);
        // Clause WHERE
        return db.update(DataBaseHelper.TABLE_USERS, cv, DataBaseHelper.COL_USER_EMAIL + "=?", new String[]{ email });
    }

    /**
     * Réinitialise uniquement la table des utilisateurs (supprime toutes les lignes).
     * A utiliser avec précaution.
     */
    public void resetUsersTable() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DataBaseHelper.TABLE_USERS, null, null);
    }

    /**
     * Méthode de test qui supprime l'intégralité du fichier de la base de données.
     * @param ctx Le contexte de l'application.
     */
    public void resetWholeDatabase(Context ctx) {
        dbHelper.close();
        ctx.deleteDatabase(DataBaseHelper.DB_NAME);
    }

}