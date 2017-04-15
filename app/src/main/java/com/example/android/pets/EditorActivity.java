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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    // Query bundle key for URI
    private static final String QUERY_BUNDLE_URI = "uri";

    // Single pet loader ID
    private static final int SINGLE_PET_LOADER_ID = 1;

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

    // URI which is passed from a previous activity
    // If the URI is null then this activity has Add a Pet mode, otherwise it has Edit Pet mode
    private Uri mUri;

    // Boolean value to see whether the pet has changed or not. This boolean value is used
    // for showing a pop-up dialog when the user has not finished editting or adding a pet but
    // wants to return to the previous activity
    private boolean mPetHasChanged = false;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get the intent, i.e. the activity from where this activity is launched
        Intent intent = getIntent();

        // See if there is URI passed from the previous activity
        // If there is no URI passed then this activity should be in Add a Pet mode
        // Otherwise, it should be in Edit Pet mode
        mUri = intent.getData();
        if(mUri == null){
            // Set "Add a Pet" title to the appbar
            setTitle(getString(R.string.add_a_pet_mode));
            // If the activity is in add a pet mode then hide the delete options menu
            invalidateOptionsMenu();
        } else {
            // Set "Edit Pet" title to the appbar
            setTitle(getString(R.string.edit_pet_mode));
            // Pass that bundle to the Loader to fetch data from the pet with the provided URI
            // Start loading
            getSupportLoaderManager().initLoader(SINGLE_PET_LOADER_ID, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Set touch listeners to all of the views
        mGenderSpinner.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
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
                savePet();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * This method inserts a pet into the database
     */
    private void savePet() {

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


        // Check if the data should be inserted or updated
        // The data should be inserted if mUri is null, since the URI is null that means that
        // This is Add a Pet mode
        // Otherwise, update the pet because it is Edit Pet mode
        if(mUri == null){

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
        } else {

            // Update the pet and see how many rows will be updated (normally one row should be
            // updated, if more than one rows or none are updated then there is something wrong)
            int rowsUpdated = getContentResolver().update(mUri, cv, null, null);

            // If rowsUpdate is 0, that means that no pet has been updated and that is some
            // malfunction. The app then toasts a message to the user that something has failed
            if(rowsUpdated == 0){
                Toast.makeText(EditorActivity.this, R.string.pet_update_fail, Toast.LENGTH_SHORT)
                        .show();
            } else {

                // Otherwise, the pet update is successful and the user gets a toast message that
                // the pet update was successful
                Toast.makeText(EditorActivity.this, R.string.pet_update_success, Toast.LENGTH_SHORT)
                        .show();
                // Return to the CatalogActivity
                finish();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){

            // Check if it is the loader for single pet
            case SINGLE_PET_LOADER_ID:

                // Return a CursorLoader which will only return a Cursor with one row, i.e.
                // the pet which is requested
                return new CursorLoader(this,
                        mUri,
                        null,
                        null,
                        null,
                        null);

            default:
                throw new IllegalArgumentException("Unknown loader id " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // See if the CursorLoader returned a valid Cursor
        if(data != null && data.getCount() != 0){

            // If so, set the data from the Cursor to the edit text fields on the activity
            data.moveToPosition(0);
            mBreedEditText.setText(data.getString(data.getColumnIndex(PetEntry.COLUMN_BREED)));
            mNameEditText.setText(data.getString(data.getColumnIndex(PetEntry.COLUMN_NAME)));
            mWeightEditText.setText(data.getString(data.getColumnIndex(PetEntry.COLUMN_WEIGHT)));
            mGenderSpinner.setSelection(data.getInt(data.getColumnIndex(PetEntry.COLUMN_GENDER)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // If the loader is reset, then we need to clear the input fields
        mBreedEditText.setText("");
        mNameEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * This method shows a pop-up dialog which shows the user whether or not should the selected pet
     * be deleted
     */
    private void showDeleteConfirmationDialog(){

        // Building the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set a dialog message
        builder.setMessage(R.string.delete_dialog_msg);
        // Set positive button function
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePet();
            }
        });
        // Set negative buttion function
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        // Show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method deletes a pet from the database using the PetProvider via the getContentResolver
     */
    private void deletePet() {

        // See how many rows where deleted in the database. Normally the number should be one since
        // we are deleting only one animal
        int rowsDeleted = getContentResolver().delete(mUri, null, null);

        // If the number of animals deleted are greater than 0 then toast a message saying
        // that the deletion was successful
        if(rowsDeleted > 0){
            Toast.makeText(this, R.string.deletion_successful, Toast.LENGTH_SHORT).show();
            // Return to CatalogActivity
            finish();
        } else {
            // Otherwise, toast a message that the deletion failed
            Toast.makeText(this, R.string.deletion_fail, Toast.LENGTH_SHORT).show();
        }
    }
}