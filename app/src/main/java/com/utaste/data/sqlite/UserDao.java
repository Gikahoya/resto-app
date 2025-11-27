package com.utaste.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDao {

    private final DataBaseHelper dbHelper;

    public UserDao(Context ctx) {
        this.dbHelper = new DataBaseHelper(ctx);
    }

    public Cursor getByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + DataBaseHelper.TABLE_USERS + " WHERE " + DataBaseHelper.COL_USER_EMAIL + "=?",
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
        return db.update(DataBaseHelper.TABLE_USERS, cv, DataBaseHelper.COL_USER_EMAIL + "=?", new String[]{ email });
    }

    public int resetPassword(String email, String newPwd) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseHelper.COL_USER_PWD, newPwd);
        return db.update(DataBaseHelper.TABLE_USERS, cv, DataBaseHelper.COL_USER_EMAIL + "=?", new String[]{ email });
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
}