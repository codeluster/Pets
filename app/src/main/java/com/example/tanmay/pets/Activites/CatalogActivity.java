package com.example.tanmay.pets.Activites;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.tanmay.pets.Adapters.PetCursorAdapter;
import com.example.tanmay.pets.Data.PetContract;
import com.example.tanmay.pets.R;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for pet data loader
    private static final int PET_LOADER = 0;

    // Adapter for the list view
    PetCursorAdapter mCursorAdapter;

    // Number of items in the cursor
    private static int numPets = Integer.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton addPet = findViewById(R.id.fab);

        addPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CatalogActivity.this, EditorActivity.class));
            }
        });

        // List View to be populated with pet data
        ListView petListView = findViewById(R.id.catalog_list_view);

        // Set a view to be displayed when the list is empty
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Instantiate the cursor adapter
        mCursorAdapter = new PetCursorAdapter(this, null);
        //Set cursor adapter on the list view
        petListView.setAdapter(mCursorAdapter);

        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Create a new intent to launch the editor activity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                // Get the uri of the pet that is clicked on
                Uri currentPetUri = ContentUris.withAppendedId(PetContract.PetEntry.CONTENT_URI, id);
                // Set pet uri as data in the intent
                intent.setData(currentPetUri);
                // Launch the activity
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PET_LOADER, null, this);

        if (numPets == 0) {
            // if there are no pets in the database
            invalidateOptionsMenu();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (numPets == 0) {
            // if there are no pets in the database
            invalidateOptionsMenu();
        }
    }

    // Inserts a dummy pet "Toto" into the database
    // For debugging & development purpose only

 /*   private void insertDummyPet() {

        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, PetContract.PetEntry.GENDER_MALE);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, 7);

        getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, values);
    }*/

    private void showDeleteAllPetsConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_pets_confirmation_dialog_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllPets();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) dialogInterface.dismiss();
            }
        });

        builder.create().show();

    }

    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetContract.PetEntry.CONTENT_URI, null, null);
        Log.v("Catalog Activity: ", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (numPets == 0) {
            // If there are no pets in the database then the option to
            // delete all pets makes no sense
            MenuItem deleteAllPets = menu.findItem(R.id.action_delete_all_entries);
            deleteAllPets.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
           /* case R.id.action_insert_dummy_data:
                insertDummyPet();
                return true;*/
            case R.id.action_delete_all_entries:
                showDeleteAllPetsConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection to specify the rows we need
        String[] projection = {
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED
        };

        // This loader will execute the Content Provider's query method on a background thread
        return new CursorLoader(this,
                PetContract.PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the cursor adapter with new data
        mCursorAdapter.swapCursor(data);

        // set number of pets
        numPets = data.getCount();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback when the data needs to be deleted
        mCursorAdapter.swapCursor(null);

    }
}
