package com.example.android.eventtimerfinish;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class RiderDbHelper extends  SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rider.db";
    private static final int DATABASE_VERSION = 10;

    RiderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_RIDER_TABLE =  "CREATE TABLE " + RiderContract.RiderEntry.TABLE_NAME + " ("
                + RiderContract.RiderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RiderContract.RiderEntry.COLUMN_RIDER_NUM + " INTEGER NOT NULL, "
                + RiderContract.RiderEntry.COLUMN_DIVISION + " TEXT NOT NULL, "
                + RiderContract.RiderEntry.COLUMN_FENCE_NUM + " INTEGER DEFAULT 0, "
                + RiderContract.RiderEntry.COLUMN_RIDER_START + " INTEGER DEFAULT 0, "
                + RiderContract.RiderEntry.COLUMN_RIDER_FINISH + " INTEGER NOT NULL, "
                + RiderContract.RiderEntry.COLUMN_EDIT + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_RIDER_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RiderContract.RiderEntry.TABLE_NAME);
        onCreate(db);
    }
}
