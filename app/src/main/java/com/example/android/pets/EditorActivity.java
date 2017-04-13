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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetDbHelper;

import static com.example.android.pets.data.PetContract.*;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity {

    // Database helper instance to help us get link from readable and writable sqlite database
    private PetDbHelper mDbHelper;

    // Log tag
    private static final String TAG = EditorActivity.class.getSimpleName();

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = 1; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = 2; // Female
                    } else {
                        mGender = 0; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                insertPet();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * This method inserts a pet into the database
     */
    private void insertPet() {

        // Get the name of the pet from the edit text
        String name = mNameEditText.getText().toString().trim();

        // Get the breed of the pet from the edit text
        String breed = mBreedEditText.getText().toString().trim();
        if(breed.length() == 0){
            breed = "Unknown";
        }

        // Get the gender from the spinner
        String genderString = mGenderSpinner.getSelectedItem().toString();

        // See to the appropriate gender constant
        switch (genderString) {
            case "Male":
                mGender = PetEntry.GENDER_MALE;
                break;
            case "Female":
                mGender = PetEntry.GENDER_FEMALE;
                break;
            default:
                mGender = PetEntry.GENDER_UNKNOWN;
                break;
        }

        // Get the value from the weight edit text and TRY to parse it into integer
        int weight;
        try{
            weight = Integer.parseInt(mWeightEditText.getText().toString().trim());
        } catch (NumberFormatException nfe){
            // If the typed value is not valid
            Log.e(TAG, getString(R.string.no_weight));
            nfe.printStackTrace();
            Toast.makeText(EditorActivity.this, R.string.no_weight, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // If all the values are valid then add them to the content values and then insert it
        // to the database
        ContentValues cv = new ContentValues();

        cv.put(PetEntry.COLUMN_NAME, name);
        cv.put(PetEntry.COLUMN_BREED, breed);
        cv.put(PetEntry.COLUMN_GENDER, mGender);
        cv.put(PetEntry.COLUMN_WEIGHT, weight);

        // Insert the data using the content resolver which redirects the query to the
        // PetProvider and then inserts the data into the database and then return a
        // not null URI instance to ensure that the insert method has executed successfully.
        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, cv);

        // If the uri is null then there was something wrong with the insertion action
        // Otherwise, print a toast message indicating that the action was successful
        if(uri == null){
            Toast.makeText(EditorActivity.this, R.string.pet_insertion_fail, Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(EditorActivity.this, R.string.pet_insertion_success, Toast.LENGTH_SHORT)
                    .show();
            // Return to the CatalogActivity
            finish();
        }
    }
}