package com.example.tanmay.pets;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tanmay.pets.data.PetContract;
import com.example.tanmay.pets.data.PetDbHelper;

public class CatalogActivity extends AppCompatActivity {

    FloatingActionButton addPet;

    @Override
    protected void onStart() {
        displayDatabaseInfo();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        addPet = findViewById(R.id.fab);

        addPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CatalogActivity.this, EditorActivity.class));
            }
        });

    }

    private void displayDatabaseInfo() {

        String[] projection = {
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED
        };

        Cursor cursor = getContentResolver().query(PetContract.PetEntry.CONTENT_URI, projection, null, null, null);
        ListView pet_list = findViewById(R.id.catalog_list_view);
        if (cursor.moveToFirst()) {
            PetCursorAdapter adapter = new PetCursorAdapter(this, cursor);
            pet_list.setAdapter(adapter);
        } else {
            View emptyView = findViewById(R.id.empty_view);
            pet_list.setEmptyView(emptyView);
        }
        cursor.close();
    }

    private void insertDummyPet() {
        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, PetContract.PetEntry.GENDER_MALE);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, 7);

        getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, values);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyPet();
                displayDatabaseInfo();
                return true;
            case R.id.action_delete_all_entries:
                //delete all entries
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class PetCursorAdapter extends CursorAdapter {

        public PetCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(R.layout.item_catalog, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView nameTextView = findViewById(R.id.pet_name);
            TextView summaryTextView = findViewById(R.id.pet_summary);

            int nameColumnIndex = cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_BREED);

            String petName = cursor.getString(nameColumnIndex);
            String petBreed = cursor.getString(breedColumnIndex);

            nameTextView.setText(petName);
            summaryTextView.setText(petBreed);

        }
    }
}
