package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * UserDao — accès simple à la table "users" (création + opérations de base)
 */
public class UserDao extends SQLiteOpenHelper {

    // — DB config
    private static final String DB_NAME = "utaste.db";
    private static final int    DB_VERSION = 1;

    // — Table/colonnes
    public static final String T_USERS  = "users";
    public static final String C_ID     = "id";
    public static final String C_FIRST  = "first";
    public static final String C_LAST   = "last";
    public static final String C_EMAIL  = "email";
    public static final String C_PWD    = "password";
    public static final String C_ROLE   = "role"; // ADMIN/CHEF/WAITER

    // — SQL de création
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + T_USERS + " (" +
                    C_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    C_FIRST + " TEXT, " +
                    C_LAST  + " TEXT, " +
                    C_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    C_PWD   + " TEXT NOT NULL, " +
                    C_ROLE  + " TEXT" +
                    ");";

    public UserDao(Context ctx) {
        super(ctx.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        onCreate(db);
    }


    /** Retourne un Cursor sur l'utilisateur avec cet email (ou vide si absent). */
    public Cursor getByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + T_USERS + " WHERE " + C_EMAIL + "=?",
                new String[]{ email }
        );
    }

    /** true si un utilisateur avec cet email existe déjà */
    public boolean exists(String email) {
        try (Cursor c = getByEmail(email)) {
            return c != null && c.moveToFirst();
        }
    }

    /** Insère l'utilisateur si l'email n'existe pas encore. Renvoie rowId ou -1 */
    public long insertIfAbsent(String first, String last, String email, String pwd, String role) {
        if (exists(email)) return -1;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_FIRST, first);
        cv.put(C_LAST,  last);
        cv.put(C_EMAIL, email);
        cv.put(C_PWD,   pwd);
        cv.put(C_ROLE,  role);
        return db.insert(T_USERS, null, cv);
    }

    /** Met à jour prénom/nom pour l'email donné. Renvoie le nb de lignes modifiées */
    public int updateProfile(String email, String first, String last) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_FIRST, first);
        cv.put(C_LAST,  last);
        // WHERE email = ?
        return db.update(T_USERS, cv, C_EMAIL + "=?", new String[]{ email });
    }

    /** Change le mot de passe pour l'email donné. Renvoie le nb de lignes modifiées */
    public int resetPassword(String email, String newPwd) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_PWD, newPwd);
        // WHERE email = ?
        return db.update(T_USERS, cv, C_EMAIL + "=?", new String[]{ email });
    }

    /** Réinitialise uniquement la table users (drop + create) */
    public void resetUsersTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        db.execSQL(SQL_CREATE_USERS);
    }
    // ⚠ Méthode de test uniquement — supprime entièrement la DB
    public void resetWholeDatabase(Context ctx) {
        close();
        ctx.deleteDatabase(DB_NAME);
    }



}
