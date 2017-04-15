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
import android.util.Log;
import android.widget.Toast;

import com.example.android.pets.R;

import static com.example.android.pets.data.PetContract.*;

/**
 * Created by terz99 on 4/12/17.
 */

public class PetProvider extends ContentProvider{

    // Invalid data code
    public static final int INVALID_DATA  = -1;

    // Log tag
    private static final String LOG_TAG = PetProvider.class.getSimpleName();

    // Valid data code
    public static final int VALID_DATA = 1;

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

    // Overriden method which helps the ContentProvider to access the query method in the database
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

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    // Overridden method which helps the content provider to access the getType method of the
    // database
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        // Match the provided URI with the originals
        final int match = sUriMatcher.match(uri);
        switch (match){

            // If the uri matches with the whole table URI then...
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;

            // If the URI matches with the single row URI then...
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;

            // Otherwise throw an exception
            default:
                throw new IllegalArgumentException("Unknown uri " + uri.toString());
        }
    }

    // Overriden method which helps the ContentProvider to access the insert method in the database
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        // Match the provided URI with the original URIs
        final int match = sUriMatcher.match(uri);
        
        switch (match){

            // If the URI matches with the WHOLE TABLE URI then insert the data
            case PETS:
                return insertPet(uri, contentValues);

            // Otherwise throw UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri.toString());
        }
    }


    /**
     * This method inserts a new pet into the database
     * @param uri is the specific URI which is directed to the whole table
     * @param contentValues is a ContentValues instance which contains the data which needs to be
     *                      inserted into the database
     * @return the URI of the newly created row (if exists, otherwise returns null)
     */
    private Uri insertPet(Uri uri, ContentValues contentValues) {

        // Check if the data is valid
        if(isDataValid(contentValues) == INVALID_DATA){
            return null;
        }

        // Get writeable database with the help from our database helper
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the data contained in the content values to the database and return the new
        // row's id
        long id = db.insert(PetEntry.TABLE_NAME, null, contentValues);

        // If the insertion action failed then log an error message
        if(id == -1){
            Log.e(LOG_TAG, "Failed to insert a new pet: " + uri.toString());
            return null;
        }

        // Notify the database of some changes
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the uri of the new valid row of the database
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * This method checks the validity of the data contained in contentValues
     * @param contentValues is a content values instance which contains the data which needs to be
     *                      checked
     * @return a constant integer code (VALID_DATA or INVALID_DATA)
     */
    private int isDataValid(ContentValues contentValues) {

        if(contentValues.containsKey(PetEntry.COLUMN_NAME)){
            String value = contentValues.getAsString(PetEntry.COLUMN_NAME);
            if(value == null || value.length() == 0){
                Toast.makeText(getContext(), R.string.no_name, Toast.LENGTH_SHORT).show();
                return INVALID_DATA;
            }
        }

        return VALID_DATA;
    }

    // Overriden method which helps the ContentProvider to access the delete method in the database
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Match the provided URI with the originals
        final int match = sUriMatcher.match(uri);

        int rowsDeleted;
        switch (match){

            // If the query should be on the whole table then...
            case PETS:
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                // If some rows are deleted then notify the database
                if(rowsDeleted > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;

            // If the query is directed to a row with a specific id then assign that id to the
            // selection String and selectionArgs String array
            case PET_ID:

                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                // If some rows are deleted then notify the database
                if(rowsDeleted > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;

            default:
                // Otherwise throw an exception
                throw new UnsupportedOperationException("Unknown uri: " + uri.toString());
        }
    }

    // Overriden method which helps the ContentProvider to access the update method in the database
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {


        // Match the URI with the originals
        final int match = sUriMatcher.match(uri);

        switch (match){

            // If the update is directed to the whole table then...
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);

            // If the update is directed to specific rows in the table then apply those
            // specifications to the selection String and selectionArgs String array
            case PET_ID:

                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);

            default:
                // Otherwise throw an exception
                throw new UnsupportedOperationException("Unknown uri " + uri.toString());
        }
    }


    /**
     * This method checks the data validity, gets link from the database and inserts the
     * data
     * @param uri is the URI of the whole table or specific row or rows
     * @param contentValues is the data
     * @param selection specification
     * @param selectionArgs specification arguments
     * @return the number of rows which are affected
     */
    private int updatePet(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        // Return argument
        int retArg;

        // If the data is not valid return INVALID_DATA code
        // Otherwise, get link from the database, update it and then toast the number of rows
        // affected
        if(isDataValid(contentValues) == INVALID_DATA){
            retArg = INVALID_DATA;
        } else {

            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            retArg = db.update(PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        }

        // If some rows are updated then notify the database
        if(retArg > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows affected or return INVALID_DATA
        return retArg;
    }
}
