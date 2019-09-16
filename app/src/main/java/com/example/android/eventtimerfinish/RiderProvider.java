package com.example.android.eventtimerfinish;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.security.Provider;

public class RiderProvider extends ContentProvider {

    public static final String LOG_TAG = RiderProvider.class.getSimpleName();
    private static final int RIDERS = 1000;
    private static final int RIDER_ID = 1001;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(RiderContract.CONTENT_AUTHORITY, RiderContract.PATH_RIDER, RIDERS);
        sUriMatcher.addURI(RiderContract.CONTENT_AUTHORITY, RiderContract.PATH_RIDER + "/#", RIDER_ID);
    }

    private RiderDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new RiderDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case RIDERS:
                cursor = database.query(RiderContract.RiderEntry.TABLE_NAME, projection, selection, selectionArgs,null, null, sortOrder);
                break;
            case RIDER_ID:
                selection = RiderContract.RiderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(RiderContract.RiderEntry.TABLE_NAME, projection, selection, selectionArgs,null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RIDERS:
                return insertRider(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertRider(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(RiderContract.RiderEntry.COLUMN_RIDER_NUM);
        if (name == null) {
            throw new IllegalArgumentException("Requires a number");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(RiderContract.RiderEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RIDERS:
                return updateRider(uri, contentValues, selection, selectionArgs);
            case RIDER_ID:
                selection = RiderContract.RiderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateRider(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateRider(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(RiderContract.RiderEntry.COLUMN_RIDER_NUM)) {
            String name = values.getAsString(RiderContract.RiderEntry.COLUMN_RIDER_NUM);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(RiderContract.RiderEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RIDERS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(RiderContract.RiderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case RIDER_ID:
                // Delete a single row given by the ID in the URI
                selection = RiderContract.RiderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(RiderContract.RiderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RIDERS:
                return RiderContract.RiderEntry.CONTENT_LIST_TYPE;
            case RIDER_ID:
                return RiderContract.RiderEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
