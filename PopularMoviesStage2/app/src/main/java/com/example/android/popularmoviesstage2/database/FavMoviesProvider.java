package com.example.android.popularmoviesstage2.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.popularmoviesstage2.database.FavMoviesContract.FavMovieEntry;

/**
 * Created by apoorva on 6/1/17.
 */

public class FavMoviesProvider extends ContentProvider {

    public static final int CODE_FAVOURITES = 100;
    public static final int CODE_FAVOURITES_ID = 101;

    public static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavMoviesDbHelper mDbHelper;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavMoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, FavMoviesContract.PATH_FAVOURITES, CODE_FAVOURITES);
        matcher.addURI(authority, FavMoviesContract.PATH_FAVOURITES + "/#", CODE_FAVOURITES_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new FavMoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_FAVOURITES:
                cursor = db.query(FavMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_FAVOURITES_ID:
                selection = FavMovieEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(FavMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Query is not supported for " + uri.toString());
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        throw new RuntimeException("getType is not implemented");
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri insertedRecord = null;

        switch (sUriMatcher.match(uri)) {
            case CODE_FAVOURITES:
                long insertedRecordId = db.insert(FavMoviesContract.FavMovieEntry.TABLE_NAME,
                        null,
                        values);
                if (insertedRecordId > 0) {
                    insertedRecord =
                            ContentUris.withAppendedId(FavMoviesContract.FavMovieEntry.CONTENT_URI,
                                    insertedRecordId);
                }
                break;
            default:
                throw new IllegalArgumentException("insert is not supported for " + uri.toString());
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertedRecord;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int numberOfRowsDeleted = 0;
        switch (sUriMatcher.match(uri)) {
            case CODE_FAVOURITES:
                db.delete(FavMovieEntry.TABLE_NAME,
                        where,
                        whereArgs);
                break;
            default:
                throw new IllegalArgumentException("delete is not supported for " + uri.toString());
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return numberOfRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("update is not supported.");
    }
}
