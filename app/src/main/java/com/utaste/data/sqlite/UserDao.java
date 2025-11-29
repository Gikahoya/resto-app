package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utaste.domain.user.Role;
import com.utaste.domain.user.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final DataBaseHelper dbHelper;

    public UserDao(Context ctx) {
        this.dbHelper = new DataBaseHelper(ctx);
    }

    // ================== MÉTHODES EXISTANTES (gardées) ==================

    public Cursor getByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + DataBaseHelper.TABLE_USERS +
                        " WHERE " + DataBaseHelper.COL_USER_EMAIL + "=?",
                new String[]{ email }
        );
    }

    public boolean exists(String email) {
        try (Cursor c = getByEmail(email)) {
            return c != null && c.moveToFirst();
        }
    }

    public long insertIfAbsent(String first, String last, String email, String pwd, String role) {
        if (exists(email)) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_USER_FIRST, first);
        cv.put(DataBaseHelper.COL_USER_LAST,  last);
        cv.put(DataBaseHelper.COL_USER_EMAIL, email);
        cv.put(DataBaseHelper.COL_USER_PWD,   pwd);
        cv.put(DataBaseHelper.COL_USER_ROLE,  role);
        return db.insert(DataBaseHelper.TABLE_USERS, null, cv);
    }

    public int updateProfile(String email, String first, String last) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_USER_FIRST, first);
        cv.put(DataBaseHelper.COL_USER_LAST,  last);
        return db.update(
                DataBaseHelper.TABLE_USERS,
                cv,
                DataBaseHelper.COL_USER_EMAIL + "=?",
                new String[]{ email }
        );
    }

    public int resetPassword(String email, String newPwd) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_USER_PWD, newPwd);
        return db.update(
                DataBaseHelper.TABLE_USERS,
                cv,
                DataBaseHelper.COL_USER_EMAIL + "=?",
                new String[]{ email }
        );
    }

    public void resetUsersTable() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_USERS);
        String SQL_CREATE_USERS =
                "CREATE TABLE IF NOT EXISTS " + DataBaseHelper.TABLE_USERS + " (" +
                        DataBaseHelper.COL_USER_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DataBaseHelper.COL_USER_FIRST + " TEXT, " +
                        DataBaseHelper.COL_USER_LAST  + " TEXT, " +
                        DataBaseHelper.COL_USER_EMAIL + " TEXT NOT NULL UNIQUE, " +
                        DataBaseHelper.COL_USER_PWD   + " TEXT NOT NULL, " +
                        DataBaseHelper.COL_USER_ROLE  + " TEXT" +
                        ");";
        db.execSQL(SQL_CREATE_USERS);
    }

    public void close() {
        dbHelper.close();
    }

    // ================== NOUVELLES MÉTHODES POUR LE REPOSITORY ==================

    /** Convertit une ligne du curseur en objet User de domaine. */
    private User mapUser(Cursor c) {
        if (c == null) return null;

        User u = new User();

        int idxId = c.getColumnIndex(DataBaseHelper.COL_USER_ID);
        if (idxId != -1) {
            int dbId = c.getInt(idxId);
            u.id = String.valueOf(dbId); // on stocke l'id SQLite sous forme de String
        }

        int idxFirst = c.getColumnIndex(DataBaseHelper.COL_USER_FIRST);
        if (idxFirst != -1) {
            u.firstName = c.getString(idxFirst);
        }

        int idxLast = c.getColumnIndex(DataBaseHelper.COL_USER_LAST);
        if (idxLast != -1) {
            u.lastName = c.getString(idxLast);
        }

        int idxEmail = c.getColumnIndex(DataBaseHelper.COL_USER_EMAIL);
        if (idxEmail != -1) {
            u.email = c.getString(idxEmail);
        }

        int idxPwd = c.getColumnIndex(DataBaseHelper.COL_USER_PWD);
        if (idxPwd != -1) {
            u.password = c.getString(idxPwd);
        }

        int idxRole = c.getColumnIndex(DataBaseHelper.COL_USER_ROLE);
        if (idxRole != -1) {
            String roleStr = c.getString(idxRole);
            if (roleStr != null) {
                try {
                    u.role = Role.valueOf(roleStr);
                } catch (IllegalArgumentException e) {
                    u.role = null;
                }
            }
        }

        return u;
    }

    /** Récupère un utilisateur par email (objet User directement). */
    public User findUserByEmail(String email) {
        try (Cursor c = getByEmail(email)) {
            if (c != null && c.moveToFirst()) {
                return mapUser(c);
            }
            return null;
        }
    }

    /** Récupère un utilisateur par id SQLite. */
    public User findUserById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(
                    DataBaseHelper.TABLE_USERS,
                    null,
                    DataBaseHelper.COL_USER_ID + "=?",
                    new String[]{ String.valueOf(id) },
                    null, null, null,
                    "1"
            );
            if (c != null && c.moveToFirst()) {
                return mapUser(c);
            }
            return null;
        } finally {
            if (c != null) c.close();
        }
    }

    /** Retourne la liste de tous les utilisateurs. */
    public List<User> getAllUsersDomain() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(
                    DataBaseHelper.TABLE_USERS,
                    null,
                    null,
                    null,
                    null,
                    null,
                    DataBaseHelper.COL_USER_FIRST + " COLLATE NOCASE ASC"
            );
            if (c != null) {
                while (c.moveToNext()) {
                    User u = mapUser(c);
                    if (u != null) list.add(u);
                }
            }
        } finally {
            if (c != null) c.close();
        }
        return list;
    }

    /** Insère un nouvel utilisateur (utilisé par le repository). */
    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_USER_FIRST, user.firstName);
        cv.put(DataBaseHelper.COL_USER_LAST,  user.lastName);
        cv.put(DataBaseHelper.COL_USER_EMAIL, user.email);
        cv.put(DataBaseHelper.COL_USER_PWD,   user.password);
        cv.put(DataBaseHelper.COL_USER_ROLE,  user.role != null ? user.role.name() : null);
        return db.insert(DataBaseHelper.TABLE_USERS, null, cv);
    }

    /**
     * Met à jour un utilisateur en DB.
     * On se base sur l'id SQLite (COL_USER_ID) si possible, sinon fallback sur l'email.
     */
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_USER_FIRST, user.firstName);
        cv.put(DataBaseHelper.COL_USER_LAST,  user.lastName);
        cv.put(DataBaseHelper.COL_USER_EMAIL, user.email);
        cv.put(DataBaseHelper.COL_USER_PWD,   user.password);
        cv.put(DataBaseHelper.COL_USER_ROLE,  user.role != null ? user.role.name() : null);

        String where;
        String[] args;

        if (user.id != null) {
            where = DataBaseHelper.COL_USER_ID + "=?";
            args  = new String[]{ user.id };
        } else {
            // fallback : on se base sur l'email
            where = DataBaseHelper.COL_USER_EMAIL + "=?";
            args  = new String[]{ user.email };
        }

        return db.update(DataBaseHelper.TABLE_USERS, cv, where, args);
    }

    /** Supprime un utilisateur par email. */
    public int deleteUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DataBaseHelper.TABLE_USERS,
                DataBaseHelper.COL_USER_EMAIL + "=?",
                new String[]{ email }
        );
    }
}
