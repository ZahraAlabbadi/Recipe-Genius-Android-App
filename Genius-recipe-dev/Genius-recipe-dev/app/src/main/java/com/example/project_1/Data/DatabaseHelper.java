package com.example.project_1.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.project_1.Model.User;
import com.example.project_1.Util.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(@Nullable Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_REGISTER_TABLE = "CREATE TABLE " + Constants.TABLE_NAME + "("
                + Constants.KEY_ID + " INTEGER PRIMARY KEY,"
                + Constants.KEY_USERNAME + " TEXT,"
                + Constants.KEY_PASSWORD + " TEXT,"
                + Constants.KEY_EMAIL + " TEXT UNIQUE,"
                + Constants.KEY_SPECIAL_INFO + " TEXT)"; // Add this line
        db.execSQL(CREATE_REGISTER_TABLE);
        Log.d(TAG, "Table created: " + Constants.TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);
        onCreate(db);
        Log.d(TAG, "Database upgraded to version: " + newVersion);
    }

    /**
     * Add a new user to the database
     */
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.KEY_USERNAME, user.getUserName());
        values.put(Constants.KEY_PASSWORD, user.getPassword());
        values.put(Constants.KEY_EMAIL, user.getEmail());
        values.put(Constants.KEY_SPECIAL_INFO, user.getSpecialInfo()); // Add this line


        long result = db.insert(Constants.TABLE_NAME, null, values);
        db.close();

        if (result == -1) {
            Log.d(TAG, "Failed to insert user");
        } else {
            Log.d(TAG, "User inserted successfully with ID: " + result);
        }
        return result;
    }


    /**
     * Check if an email exists in the database
     */
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME,
                new String[]{Constants.KEY_EMAIL}, // Select email column
                Constants.KEY_EMAIL + "=?",       // WHERE email = ?
                new String[]{email},              // Email argument
                null, null, null);

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();

        return exists;
    }

    /**
     * Check if a special info exists in the database
     */

    public boolean checkSpecialInfo(String email, String specialInfo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME,
                new String[]{Constants.KEY_SPECIAL_INFO},
                Constants.KEY_EMAIL + "=? AND " + Constants.KEY_SPECIAL_INFO + "=?",
                new String[]{email, specialInfo},
                null, null, null);

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }



    /**
     * Update the user's password in the database
     *
     * @param email       The user's email
     * @param newPassword The new password to set
     * @return true if the update was successful, false otherwise
     */
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.KEY_PASSWORD, newPassword);

        int rowsAffected = db.update(
                Constants.TABLE_NAME,  // Table name
                values,                // Values to update
                Constants.KEY_EMAIL + "=?", // WHERE clause
                new String[]{email}    // WHERE arguments
        );

        return rowsAffected > 0; // Return true if at least one row was updated
    }


    /**
     * Check if a user exists by email and password
     */
    public Boolean checkUser(String email, String password) {
        String[] columns = {Constants.KEY_ID};
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.KEY_EMAIL + "=? AND " + Constants.KEY_PASSWORD + "=?";
        String[] selectionArgs = {email, password};
        Cursor cursor = db.query(Constants.TABLE_NAME, columns, selection, selectionArgs, null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;

        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

}
