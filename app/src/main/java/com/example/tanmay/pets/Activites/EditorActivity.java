package com.example.tanmay.pets.Activites;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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

import com.example.tanmay.pets.Data.PetContract;
import com.example.tanmay.pets.R;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PET_LOADER = 0;

    private EditText mNameEditText, mBreedEditText, mWeightEditText;
    private Spinner mGenderSpinner;

    private int mGender = 0;
    private boolean mPetHasChanged = false;

    private Uri currentPetUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get the intent
        Intent intent = getIntent();
        // Get the uri of clicked pet
        currentPetUri = intent.getData();

        // If currentPetUri == null then open in add new pet mode
        if (currentPetUri == null) {
            setTitle(R.string.add_a_pet);

            // before creating menu executes onPrepareOptionsMenu()
            invalidateOptionsMenu();

        } else {
            // An existing pet has been clicked on and needs to be added
            setTitle(R.string.edit_pet);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_pet_name);
        mBreedEditText = findViewById(R.id.edit_pet_breed);
        mWeightEditText = findViewById(R.id.edit_pet_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);

        // If user touches any field means they have edited it, set mPetHasChanged to true
        View.OnTouchListener petEditListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mPetHasChanged = true;
                return false;
            }
        };

        setupSpinner();

        mNameEditText.setOnTouchListener(petEditListener);
        mBreedEditText.setOnTouchListener(petEditListener);
        mWeightEditText.setOnTouchListener(petEditListener);
        mGenderSpinner.setOnTouchListener(petEditListener);

        getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);

    }

    private boolean validateInput() {

        // Check if name is empty
        if (TextUtils.isEmpty(mNameEditText.getText().toString())) return false;
        // Check if breed is empty
        if (TextUtils.isEmpty(mBreedEditText.getText().toString())) return false;
        // Check if gender is unknown
        if (mGenderSpinner.getSelectedItemPosition() == PetContract.PetEntry.GENDER_UNKNOWN)
            return false;
        // Checks if weight is blank and if so puts it 0
        if (TextUtils.isEmpty(mWeightEditText.getText().toString()))
            mWeightEditText.setText(Integer.toString(0));

        // If al criteria are met
        return true;
    }

    private void savePet() {

        if (!validateInput()) {
            Log.i("info", "Failed validateInput check");
            return;
        }

        try {

            ContentValues contentValues = new ContentValues();
            contentValues.put(PetContract.PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString().trim());
            contentValues.put(PetContract.PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
            contentValues.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, Integer.parseInt(mWeightEditText.getText().toString().trim()));
            contentValues.put(PetContract.PetEntry.COLUMN_PET_GENDER, mGender);

            if (currentPetUri == null) {
                getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, contentValues);
                Toast.makeText(this, contentValues.get(PetContract.PetEntry.COLUMN_PET_NAME) + " added to database.", Toast.LENGTH_SHORT).show();
            } else {
                int rowsAffected = getContentResolver().update(currentPetUri, contentValues, null, null);
                if (rowsAffected == 0)
                    Toast.makeText(this, getString(R.string.editor_update_pet_failed), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, getString(R.string.editor_update_pet_successful), Toast.LENGTH_SHORT).show();
            }


            finish();

        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Please enter valid data", Toast.LENGTH_SHORT).show();
        }
    }

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

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) dialog.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentPetUri == null) {
            // Remove the option to delete pet when adding a new pet
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                savePet();
                return true;
            case R.id.action_delete:
                getContentResolver().delete(currentPetUri, null, null);
                finish();
                return true;
            case android.R.id.home:

                // If pet hasn't changed then navigate up
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User has clicked up therefore navigate up
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (currentPetUri != null) {

            String[] projection = {
                    PetContract.PetEntry._ID,
                    PetContract.PetEntry.COLUMN_PET_NAME,
                    PetContract.PetEntry.COLUMN_PET_BREED,
                    PetContract.PetEntry.COLUMN_PET_WEIGHT,
                    PetContract.PetEntry.COLUMN_PET_GENDER
            };

            // returns new cursor loader containing only one row acc to the currentPetUri
            return new CursorLoader(this,
                    currentPetUri,
                    projection,
                    null,
                    null,
                    null);
        } else return null;
    }

    // Cursor data will have only one row
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Prevent execution if new pet is to be added
        if (data == null || data.getCount() < 1) return;

        // Initially cursor is at -1
        if (data.moveToFirst()) {
            mNameEditText.setText(data.getString(data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME)));
            mBreedEditText.setText(data.getString(data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED)));
            mWeightEditText.setText(Integer.toString(data.getInt(data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_WEIGHT))));
            mGenderSpinner.setSelection(data.getInt(data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_GENDER)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Set all edit texts as blank
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        // Set Unknown Gender
        mGenderSpinner.setSelection(0);
    }
}
