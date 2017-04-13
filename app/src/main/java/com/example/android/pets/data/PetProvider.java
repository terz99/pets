package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.example.android.pets.data.PetContract.*;

/**
 * Created by terz99 on 4/12/17.
 */

public class PetProvider extends ContentProvider{

    // Log tag
    private static final String LOG_TAG = PetProvider.class.getSimpleName();

    // Database helper instance for linking the database to the pet content provider
    private PetDbHelper mDbHelper;

    // URI Matcher
    private UriMatcher sUriMatcher;

    // Constants for the URI matcher
    // Matching code for the URI matcher in case of needing the whole pets table
    private final static int PETS = 100;
    // Matching code for the URI matcher in case of needing a pet with a unique ID
    private final static int PET_ID = 101;

    /**
     * This method instantiates the pet database helper
     * @return true
     */
    @Override
    public boolean onCreate() {

        // Initialize the URI matcher with NO MATCH
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Set the URI matcher's matching codes
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH, PETS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH + "/#", PET_ID);

        // Initialzie the database helper
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // See the match of the URI provided
        int match = sUriMatcher.match(uri);

        // The cursor which this method will return
        Cursor retCursor;

        switch (match){

            // If the uri matches the uri which provides the whole pets table
            case PETS:

                // Send query to the database and retrieve a cursor instance from it
                retCursor = db.query(PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            case PET_ID:

                // Set selection String to retrieve information about the pet with the given id
                selection = PetEntry._ID + "=?";
                // All the selection Arguments are stored in selectionArgs String array
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };

                // Send query to the database and retrieve a cursor instance from it
                retCursor = db.query(PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            default:
                // The URI is invalid
                throw new UnsupportedOperationException("Unknown uri: " + uri.toString());
        }

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
