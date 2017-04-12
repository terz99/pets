package com.example.android.pets.data;

/**
 * Created by terz99 on 4/12/17.
 */


import android.provider.BaseColumns;

/**
 * This contract only provides constants which are used to operate with SQL queries
 */
public final class PetContract {

    // Private constructor so there cannot be any instance from this class
    private PetContract(){

    }

    /**
     * This class contains most of the constant variables, like column names and other constanst
     */
    public static class PetEntry implements BaseColumns {

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
