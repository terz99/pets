/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetDbHelper;

import static com.example.android.pets.data.PetContract.*;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int PET_LOADER_ID = 0;
    // Member object of the pet list view adapter
    private PetAdapter mPetAdapter;
    // Member object of the pet list view
    private ListView mPetListView;
    // Member object of the database helper class
    private PetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Get link from the pet list view
        mPetListView = (ListView) findViewById(R.id.pet_list);

        // Set empty view to the pet list view in case there is no data to be displayed
        mPetListView.setEmptyView(findViewById(R.id.empty_view));

        // Create a temporary null pet adapter attach it to the list view
        mPetAdapter = new PetAdapter(this, null);
        mPetListView.setAdapter(mPetAdapter);

        mPetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent editPetIntent = new Intent(CatalogActivity.this, EditorActivity.class);
                editPetIntent.setData(ContentUris.withAppendedId(PetEntry.CONTENT_URI, id));
                startActivity(editPetIntent);
            }
        });

        // Start loading the data
        getSupportLoaderManager().initLoader(PET_LOADER_ID, null, this);
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                //Insert fake data into the database
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method currently insert fake data into the database
     */
    private void insertPet() {

        /**
         * Create the content values instance and put the dummy data inside the content values
         */
        ContentValues cv = new ContentValues();

        cv.put(PetEntry.COLUMN_NAME, "Toto");
        cv.put(PetEntry.COLUMN_BREED, "Terrier");
        cv.put(PetEntry.COLUMN_GENDER, PetEntry.GENDER_MALE);
        cv.put(PetEntry.COLUMN_WEIGHT, 7);


        // Insert the data using the content resolver which redirects the query to the
        // PetProvider and then inserts the dummy data into the database and then return a
        // not null URI instance to ensure that the insert method has executed successfully.
        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, cv);

        // If the uri is null then there was something wrong with the insertion action
        // Otherwise, print a toast message indicating that the action was successful
        if(uri == null){
            Toast.makeText(CatalogActivity.this, R.string.dummy_insertion_fail, Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(CatalogActivity.this, R.string.dummy_insertion_success, Toast.LENGTH_SHORT)
                    .show();
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){

            case PET_LOADER_ID:

                String[] projection = {
                        PetEntry._ID,
                        PetEntry.COLUMN_NAME,
                        PetEntry.COLUMN_BREED
                };

                return new CursorLoader(this,
                        PetEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null);

            default:
                throw new IllegalArgumentException("Unknown loader id " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPetAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPetAdapter.swapCursor(null);
    }
}
