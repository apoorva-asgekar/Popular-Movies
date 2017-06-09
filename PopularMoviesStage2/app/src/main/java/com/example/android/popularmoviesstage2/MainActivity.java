package com.example.android.popularmoviesstage2;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import com.example.android.popularmoviesstage2.database.FavMoviesContract;
import com.example.android.popularmoviesstage2.database.FavMoviesContract.FavMovieEntry;
import com.example.android.popularmoviesstage2.databinding.ActivityMainBinding;
import com.example.android.popularmoviesstage2.utilities.ImageUtils;
import com.example.android.popularmoviesstage2.utilities.MovieJsonUtils;
import com.example.android.popularmoviesstage2.utilities.NetworkUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements MovieAdapter.MovieAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private MovieAdapter mMovieAdapter;

    private SharedPreferences sharedPreferences;

    private static final int NETWORK_MOVIES_LOADER_ID = 11;
    private static final int DATABASE_MOVIES_LOADER_ID = 22;

    private static boolean prefUpdated = false;

    private ActivityMainBinding mainBinding;

    //2 LoaderCallbacks - 1 for the network access and 1 for loading favourites from the database.
    LoaderManager.LoaderCallbacks<List<Movie>> networkLoadListener = new LoaderManager.LoaderCallbacks<List<Movie>>() {
        @Override
        public Loader<List<Movie>> onCreateLoader(int i, Bundle bundle) {
            return new AsyncTaskLoader<List<Movie>>(MainActivity.this) {

                List<Movie> mListOfMovies;

                @Override
                protected void onStartLoading() {
                    if (mListOfMovies != null) {
                        deliverResult(mListOfMovies);
                    } else {
                        mainBinding.pbLoadingIndicator.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                }

                @Override
                public List<Movie> loadInBackground() {
                    List<Movie> listOfMovies = null;
                    try {
                        String sortOrderPref = sharedPreferences
                                .getString(getString(R.string.pref_sort_key)
                                        , getString(R.string.pref_sort_popular_value));
                        String movieUrl = NetworkUtils.getRequestUrlWithPreference(sortOrderPref,
                                getContext());
                        if (NetworkUtils.isNetworkConnected(getContext())) {
                            listOfMovies = MovieJsonUtils.getMovieDetails(movieUrl);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "JSONException thrown by the network request.", e);
                    }
                    return listOfMovies;
                }

                @Override
                public void deliverResult(List<Movie> data) {
                    mListOfMovies = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
            mainBinding.pbLoadingIndicator.setVisibility(View.INVISIBLE);
            mMovieAdapter.setMovieData(movies);
            if (movies == null) {
                showErrorMessage();
            } else {
                showMovies();
            }
        }

        /**
         * Not using this method, but it needs to be overridden when implementing
         * loader callbacks interface.
         */
        @Override
        public void onLoaderReset(Loader<List<Movie>> loader) {
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> databaseLoadListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    String[] projection = new String[]{
                            FavMovieEntry._ID,
                            FavMovieEntry.COLUMN_MOVIE_API_ID,
                            FavMovieEntry.COLUMN_MOVIE_TITLE,
                            FavMovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                            FavMovieEntry.COLUMN_MOVIE_RATING,
                            FavMovieEntry.COLUMN_MOVIE_OVERVIEW,
                            FavMovieEntry.COLUMN_MOVIE_POSTER_PATH
                    };
                    return new CursorLoader(MainActivity.this,
                            FavMoviesContract.FavMovieEntry.CONTENT_URI,
                            projection,
                            null,
                            null,
                            null);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    List<Movie> listOfFavMovies = new ArrayList<Movie>();

                    Log.v(LOG_TAG, "Cursor is: " + DatabaseUtils.dumpCursorToString(data));
                    data.moveToFirst();

                    while (!data.isAfterLast()) {
                        int movieId = data.getInt(
                                data.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_API_ID));
                        String title = data.getString(
                                data.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_TITLE));
                        String plot = data.getString(
                                data.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_OVERVIEW));
                        Float userRating = Float.parseFloat(data.getString(
                                data.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_RATING)));
                        String releaseDate = data.getString(
                                data.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_RELEASE_DATE));
                        String posterLink = ImageUtils.IMAGE_DIRECTORY + "/" + data.getString(
                                data.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_POSTER_PATH));
                        Movie newMovie =
                                new Movie(movieId, title, posterLink, plot, userRating, releaseDate);
                        if (newMovie != null) {
                            listOfFavMovies.add(newMovie);
                        }
                        data.moveToNext();
                    }
                    mMovieAdapter.setMovieData(listOfFavMovies);
                    if (listOfFavMovies == null) {
                        showErrorMessage();
                    } else {
                        String sortOrderPref = sharedPreferences
                                .getString(getString(R.string.pref_sort_key)
                                        , getString(R.string.pref_sort_popular_value));
                        if (!NetworkUtils.isNetworkConnected(MainActivity.this) &&
                                !sortOrderPref.equals(getString(R.string.pref_sort_favourites_value))) {
                            Toast.makeText(MainActivity.this,
                                    R.string.no_network_message, Toast.LENGTH_LONG).show();
                        }
                        showMovies();
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int orientation = GridLayout.VERTICAL;
        int span = getResources().getInteger(R.integer.gridlayout_span);
        boolean reverseLayout = false;

        GridLayoutManager layoutManager = new GridLayoutManager(this, span, orientation, reverseLayout);

        mainBinding.recyclerviewMovie.setHasFixedSize(true);
        mainBinding.recyclerviewMovie.setLayoutManager(layoutManager);

        mMovieAdapter = new MovieAdapter(this, this);
        mainBinding.recyclerviewMovie.setAdapter(mMovieAdapter);

        //Depending on the settings - load the movies either from the network or
        // from the stored database Favourites
        String sortOrderPref = sharedPreferences
                .getString(getString(R.string.pref_sort_key)
                        , getString(R.string.pref_sort_popular_value));
        if (sortOrderPref.equals(getString(R.string.pref_sort_favourites_value)) ||
                !NetworkUtils.isNetworkConnected(this)) {
            getLoaderManager().initLoader(DATABASE_MOVIES_LOADER_ID, null, databaseLoadListener);
        } else {
            getLoaderManager().initLoader(NETWORK_MOVIES_LOADER_ID, null, networkLoadListener);
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }

    /**
     * If preferences have been updated only then start a new load.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (prefUpdated) {
            prefUpdated = false;
            invalidateData();
            String sortOrderPref = sharedPreferences
                    .getString(getString(R.string.pref_sort_key)
                            , getString(R.string.pref_sort_popular_value));
            if (sortOrderPref.equals(getString(R.string.pref_sort_favourites_value)) ||
                    !NetworkUtils.isNetworkConnected(this)) {
                getLoaderManager()
                        .restartLoader(DATABASE_MOVIES_LOADER_ID, null, databaseLoadListener);
            } else {
                getLoaderManager()
                        .restartLoader(NETWORK_MOVIES_LOADER_ID, null, networkLoadListener);
            }
        }
    }

    /**
     * Override the onDestroy method and unregister the change listener.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * This method is to respond to clicks on each of the movie items in the Grid.
     *
     * @param movie - Movie Object containing the details of the selected movie poster.
     */
    @Override
    public void onClick(Movie movie) {
        Intent detailActivityIntent = new Intent(this, DetailActivity.class);
        //TODOCOMPLETED - Add movie object to the intent.
        Bundle b = new Bundle();
        b.putParcelable("MOVIE", movie);
        detailActivityIntent.putExtra("movieBundle", b);
        startActivity(detailActivityIntent);
    }

    //Sets null data in the adapter
    private void invalidateData() {
        mMovieAdapter.setMovieData(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsActIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsActIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Setting the flag stating preferences have changed to true.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        prefUpdated = true;
    }

    /**
     * This method will make the movies visible and the error message invisible.
     */
    private void showMovies() {
        mainBinding.recyclerviewMovie.setVisibility(View.VISIBLE);
        mainBinding.tvErrorMessageDisplay.setVisibility(View.INVISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movie data.
     */
    private void showErrorMessage() {
        mainBinding.recyclerviewMovie.setVisibility(View.INVISIBLE);
        mainBinding.tvErrorMessageDisplay.setVisibility(View.VISIBLE);
    }
}
