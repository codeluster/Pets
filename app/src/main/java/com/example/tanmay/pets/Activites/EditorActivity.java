package com.example.tanmay.pets.Activites;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tanmay.pets.Data.PetContract;
import com.example.tanmay.pets.Data.PetDbHelper;
import com.example.tanmay.pets.R;

public class EditorActivity extends AppCompatActivity {

    private EditText mNameEditText, mBreedEditText, mWeightEditText;
    private Spinner mGenderSpinner;
    private int mGender = 0;
    private PetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get the intent
        Intent intent = getIntent();
        // Get the uri of clicked pet
        Uri currentPetUri = intent.getData();

        // If currentPetUri == null then open in add new pet mode
        if (currentPetUri == null) {
            setTitle(R.string.add_a_pet);
        } else {
            // An existing pet has been clicked on and needs to be added
            setTitle(R.string.edit_pet);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_pet_name);
        mBreedEditText = findViewById(R.id.edit_pet_breed);
        mWeightEditText = findViewById(R.id.edit_pet_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);

        setupSpinner();

        mDbHelper = new PetDbHelper(this);

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

    private void insertPet() {

        try {

            ContentValues contentValues = new ContentValues();
            contentValues.put(PetContract.PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString().trim());
            contentValues.put(PetContract.PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
            contentValues.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, Integer.parseInt(mWeightEditText.getText().toString()));
            contentValues.put(PetContract.PetEntry.COLUMN_PET_GENDER, mGender);

            getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, contentValues);

            finish();

            Toast.makeText(this, contentValues.get(PetContract.PetEntry.COLUMN_PET_NAME) + " added to database.", Toast.LENGTH_SHORT).show();

        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Please enter valid data", Toast.LENGTH_SHORT).show();
        }
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
                insertPet();
                return true;
            case R.id.action_delete:
                //delete entry
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
