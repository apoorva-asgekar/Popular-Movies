package com.example.android.popularmoviesstage2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.popularmoviesstage2.database.FavMoviesContract.FavMovieEntry;

/**
 * Created by apoorva on 5/31/17.
 */

public class FavMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public FavMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("FavMoviesDbHelper", "Creating the database");
        final String SQL_CREATE_FAVMOVIES_TABLE = "CREATE TABLE " + FavMovieEntry.TABLE_NAME + " (" +
                FavMovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavMovieEntry.COLUMN_MOVIE_API_ID + " TEXT NOT NULL, " +
                FavMovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                FavMovieEntry.COLUMN_MOVIE_RATING + " TEXT NOT NULL, " +
                FavMovieEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                FavMovieEntry.COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                FavMovieEntry.COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL, " +
                " UNIQUE (" + FavMovieEntry.COLUMN_MOVIE_TITLE + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_FAVMOVIES_TABLE);
        Log.v("FavMoviesDbHelper", "Database created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavMovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
