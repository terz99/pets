package com.example.android.pets.data;

/**
 * Created by terz99 on 4/12/17.
 */


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This contract only provides constants which are used to operate with SQL queries
 */
public final class PetContract {

    // The authority of the content database
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    // The base URI of the content database ( content://com.example.android.pets )
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // The path which is specific to the pets table in the content database
    public static final String PATH = "pets";


    // Private constructor so there cannot be any instance from this class
    private PetContract(){

    }

    /**
     * This class contains most of the constant variables, like column names and other constanst
     */
    public static class PetEntry implements BaseColumns {

        // This string helps the getType() method in the content provider to return the type of the
        // whole table
        public static final String CONTENT_LIST_TYPE  = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH;

        // This string helps the getType() method in the content provider to return the type of
        // a single row
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH + "/#";

        // The main URI of the database content
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        // Name of the table in the database
        public static final String TABLE_NAME = "pets";
        // Column id for the name of the pet
        public static final String COLUMN_NAME = "name";
        // Column id for the breed of the pet
        public static final String COLUMN_BREED = "breed";
        // Column id for the gender of the pet
        public static final String COLUMN_GENDER = "gender";
        // Column id for the weight of the pet
        public static final String COLUMN_WEIGHT = "weight";


        // Constant value for the male gender
        public static final int GENDER_MALE = 1;
        // Constant value for the female gender
        public static final int GENDER_FEMALE = 2;
        // Constant value for the unknown gender
        public static final int GENDER_UNKNOWN = 3;
    }
}
