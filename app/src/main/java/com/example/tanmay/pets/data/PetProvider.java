package com.example.tanmay.pets.data;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    private static final int PETS = 1000;
    private static final int PETS_ID = 1001;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PETS_ID);
    }


    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        mDbHelper = new PetDbHelper(getContext());

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        mDbHelper = new PetDbHelper(getContext());
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case PETS:
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("The provided Uri " + uri.toString() + " is not valid");
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case PETS:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("The provided Uri " + uri.toString() + " is not valid");
        }
    }

    private Uri insertItem(Uri uri, ContentValues contentValues) {

        if (validateData(contentValues)) {

            SQLiteDatabase database = mDbHelper.getWritableDatabase();
            long rowID = database.insert(PetContract.PetEntry.TABLE_NAME, null, contentValues);
            if (rowID == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;
            }
            return ContentUris.withAppendedId(uri, rowID);
        } else return null;
    }

    private boolean validateData(ContentValues values) {

        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null) throw new IllegalArgumentException("Pet requires a name");

        String breed = values.getAsString(PetContract.PetEntry.COLUMN_PET_BREED);
        if (breed == null) throw new IllegalArgumentException("Pet's breed needs to be specified");

        int weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (!(weight > 0))
            throw new IllegalArgumentException("Pet's weight cannot be less than 1 kg");

        int gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (!(gender == PetContract.PetEntry.GENDER_UNKNOWN || gender == PetContract.PetEntry.GENDER_MALE || gender == PetContract.PetEntry.GENDER_FEMALE))
            throw new IllegalArgumentException("Pet's gender needs to be specified");

        return true;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        switch (sUriMatcher.match(uri)) {

            case PETS:
                return deleteItem(selection, selectionArgs);
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteItem(selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion not supported for " + uri);
        }
    }

    private int deleteItem(String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        return database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        if (values.size() == 0) return 0;

        if (validateData(values)) {

            switch (sUriMatcher.match(uri)) {
                case PETS:
                    return updateItem(values, selection, selectionArgs);
                case PETS_ID:
                    selection = PetContract.PetEntry._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    return updateItem(values, selection, selectionArgs);
                default:
                    throw new IllegalArgumentException("Update is not supported for " + uri);
            }
        } else return -1;

    }

    private int updateItem(ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        database.update(PetContract.PetEntry.TABLE_NAME, values, selection, selectionArgs);

        return 0;
    }

}
