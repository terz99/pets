package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by terz99 on 4/12/17.
 */

public class PetDbHelper extends SQLiteOpenHelper{

    private static final String TAG = "PetDbHelper";
    // The name of the database
    private static final String DATABASE_NAME = "pets.db";
    // The current version of the database
    private static final int DATABASE_VERSION = 1;


    /**
     * Public constructor which calls super constructor to create database with given name and
     * version
     * @param context is the Context from where the database is created
     */
    public PetDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method executes the SQL_CREATE_TABLE_ENTRY command in SQL
     * @param sqLiteDatabase is the database
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // SQL command to create a table
        final String SQL_CREATE_TABLE_ENTRY = "CREATE TABLE "
                + PetContract.PetEntry.TABLE_NAME + "("
                + PetContract.PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetContract.PetEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + PetContract.PetEntry.COLUMN_BREED + " TEXT DEFAULT \"Unknown\", "
                + PetContract.PetEntry.COLUMN_GENDER + " INTEGER DEFAULT 0, "
                + PetContract.PetEntry.COLUMN_WEIGHT + " INTEGER NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_ENTRY);

        Log.i(TAG, "Database created");
    }

    /**
     * This method upgrades a newer version of the database and drop the older version
     * @param sqLiteDatabase is the database which is being upgraded
     * @param i is the id of the older version
     * @param i1 is the id of the newer version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        // SQL command to delete (drop) table
        final String SQL_DELETE_TABLE_ENTRY =
                "DROP TABLE IF EXISTS " + PetContract.PetEntry.TABLE_NAME;

        sqLiteDatabase.execSQL(SQL_DELETE_TABLE_ENTRY);
        onCreate(sqLiteDatabase);
    }
}
