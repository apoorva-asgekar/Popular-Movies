package com.example.android.popularmoviesstage2;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesstage2.database.FavMoviesContract;
import com.example.android.popularmoviesstage2.database.FavMoviesContract.FavMovieEntry;
import com.example.android.popularmoviesstage2.databinding.ActivityDetailBinding;
import com.example.android.popularmoviesstage2.utilities.ImageUtils;
import com.example.android.popularmoviesstage2.utilities.MovieJsonUtils;
import com.example.android.popularmoviesstage2.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.id;

public class DetailActivity extends AppCompatActivity
        implements TrailersAdapter.TrailersAdapterOnClickHandler {

    private final static String LOG_TAG = DetailActivity.class.getSimpleName();

    private TrailersAdapter mTrailersAdapter;
    private ReviewsAdapter mReviewsAdapter;

    private final int MOVIE_TRAILER_REVIEWS_LOADER = 1984;
    private final int FAVOURITE_CURSOR_LOADER = 1994;
    Movie currentMovie = null;

    private ActivityDetailBinding mBinding;

    //Creating 2 loader callbacks. 1 for getting trailers and reviews from the network and the
    //other for getting favourite data from the database.
    private LoaderManager.LoaderCallbacks<HashMap<String, List<String>>> movieExtrasLoaderListener =
            new LoaderManager.LoaderCallbacks<HashMap<String, List<String>>>() {
                @Override
                public Loader<HashMap<String, List<String>>> onCreateLoader(int id, final Bundle args) {
                    return new AsyncTaskLoader<HashMap<String, List<String>>>(DetailActivity.this) {

                        HashMap<String, List<String>> movieExtras = null;

                        @Override
                        protected void onStartLoading() {
                            if (movieExtras != null) {
                                deliverResult(movieExtras);
                            } else {
                                mBinding.detailProgressBar.setVisibility(View.VISIBLE);
                                forceLoad();
                            }
                        }

                        @Override
                        public HashMap<String, List<String>> loadInBackground() {
                            HashMap<String, List<String>> movieExtraDetails = new HashMap<String, List<String>>();

                            String movieId = String.valueOf(args.getInt("movieId"));
                            String requestUrlTrailers =
                                    NetworkUtils.getRequestUrlForVideos(movieId, DetailActivity.this);
                            String requestUrlReviews =
                                    NetworkUtils.getRequestUrlForReviews(movieId, DetailActivity.this);
                            try {
                                movieExtras = MovieJsonUtils.getMovieExtras(requestUrlTrailers, requestUrlReviews,
                                        DetailActivity.this);
                            } catch (JSONException e) {
                                Log.e(LOG_TAG, "JSONException thrown by detail activity loader", e);
                            }
                            return movieExtras;
                        }

                        @Override
                        public void deliverResult(HashMap<String, List<String>> data) {
                            movieExtras = data;
                            super.deliverResult(data);
                        }
                    };
                }

                @Override
                public void onLoadFinished(Loader<HashMap<String, List<String>>> loader,
                                           HashMap<String, List<String>> data) {
                    mBinding.detailProgressBar.setVisibility(View.INVISIBLE);
                    mTrailersAdapter.setTrailerDetails(data);
                    mReviewsAdapter.setReviewDetails(data);

                    //By default hide trailer and review details. Only show them if corresponding data is available.
                    dontShowTrailers();
                    dontShowReviews();
                    showOtherDetails();

                    if (data.containsKey(getString(R.string.trailers_key))) {
                        showTrailers();
                    }
                    if (data.containsKey(getString(R.string.reviews_key))) {
                        Log.v(LOG_TAG, "Will show reviews");
                        showReviews();
                    }
                }

                @Override
                public void onLoaderReset(Loader<HashMap<String, List<String>>> loader) {

                }
            };

    private LoaderManager.LoaderCallbacks<Cursor> favMoviesLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    String[] projection = new String[]{
                            FavMovieEntry._ID
                    };
                    String selection = FavMovieEntry.COLUMN_MOVIE_API_ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(args.getInt("movieId"))};
                    return new CursorLoader(DetailActivity.this,
                            FavMovieEntry.CONTENT_URI,
                            projection,
                            selection,
                            selectionArgs,
                            null);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    Log.v(LOG_TAG, "Cursor is: " + DatabaseUtils.dumpCursorToString(data));
                    if (data.getCount() > 0) {
                        mBinding.posterDetails.checkboxFavourite.setChecked(true);
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Bundle intentBundle = getIntent().getBundleExtra("movieBundle");
        if (intentBundle != null) {
            currentMovie = intentBundle.getParcelable("MOVIE");
            Log.v(LOG_TAG, "Movie Id: " + currentMovie.getMovieId());
            Log.v(LOG_TAG, "Movie Title: " + currentMovie.getTitle());
            Log.v(LOG_TAG, "Movie Poster: " + currentMovie.getPosterLink());
            Log.v(LOG_TAG, "Movie Plot: " + currentMovie.getPlot());
            Log.v(LOG_TAG, "Movie User Rating: " + currentMovie.getUserRating());
            Log.v(LOG_TAG, "Movie Release Date: " + currentMovie.getReleaseDate());
        }

        //Set listener on the Favourite checkbox to save favourite movie info.
        mBinding.posterDetails.checkboxFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.posterDetails.checkboxFavourite.isChecked()) {
                    // Save the poster to local storage.
                    String posterUrl =
                            NetworkUtils.getCompletePosterLink(currentMovie.getPosterLink());
                    Picasso.with(DetailActivity.this)
                            .load(posterUrl)
                            .into(ImageUtils.picassoImageTarget(getApplicationContext()
                                    , String.valueOf(currentMovie.getMovieId())));

                    // Save all values to database
                    ContentValues values = new ContentValues();
                    values.put(FavMovieEntry.COLUMN_MOVIE_API_ID, currentMovie.getMovieId());
                    values.put(FavMovieEntry.COLUMN_MOVIE_TITLE, currentMovie.getTitle());
                    values.put(FavMovieEntry.COLUMN_MOVIE_RATING, currentMovie.getUserRating());
                    values.put(FavMovieEntry.COLUMN_MOVIE_RELEASE_DATE, currentMovie.getReleaseDate());
                    values.put(FavMovieEntry.COLUMN_MOVIE_OVERVIEW, currentMovie.getPlot());
                    values.put(FavMovieEntry.COLUMN_MOVIE_POSTER_PATH,
                            ImageUtils.IMAGE_DIRECTORY + "/" + String.valueOf(currentMovie.getMovieId()));
                    getContentResolver().insert(FavMovieEntry.CONTENT_URI, values);
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.favourite_saved), Toast.LENGTH_SHORT).show();
                } else {
                    // Deleting the image from local storage.
                    ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    File directory = cw.getDir(ImageUtils.IMAGE_DIRECTORY, Context.MODE_PRIVATE);
                    File myImageFile =
                            new File(directory, String.valueOf(currentMovie.getMovieId()));
                    if (myImageFile.delete()) {
                        Log.v(LOG_TAG, "Image on the disk deleted successfully!");
                    }

                    //Deleting the movie from the database.
                    String where = FavMovieEntry.COLUMN_MOVIE_API_ID + "=?";
                    String[] whereArgs = new String[]{String.valueOf(currentMovie.getMovieId())};
                    getContentResolver().delete(FavMovieEntry.CONTENT_URI, where, whereArgs);
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.favourite_deleted), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set Trailer data in Trailers RecyclerView
        mBinding.trailersReviews.recyclerviewTrailers.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.trailersReviews.recyclerviewTrailers.setLayoutManager(layoutManager);

        mTrailersAdapter = new TrailersAdapter(this, this);
        mBinding.trailersReviews.recyclerviewTrailers.setAdapter(mTrailersAdapter);

        //Set Reviews data in Reviews RecyclerView
        mBinding.trailersReviews.recyclerviewReviews.setHasFixedSize(true);
        LinearLayoutManager layoutManagerReviews = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.trailersReviews.recyclerviewReviews.setLayoutManager(layoutManagerReviews);

        mReviewsAdapter = new ReviewsAdapter(this);
        mBinding.trailersReviews.recyclerviewReviews.setAdapter(mReviewsAdapter);

        if (currentMovie != null) {
            Bundle loaderArgs = new Bundle();
            loaderArgs.putInt("movieId", currentMovie.getMovieId());
            if (NetworkUtils.isNetworkConnected(this)) {
                getLoaderManager()
                        .initLoader(MOVIE_TRAILER_REVIEWS_LOADER, loaderArgs, movieExtrasLoaderListener);
                getLoaderManager()
                        .initLoader(FAVOURITE_CURSOR_LOADER, loaderArgs, favMoviesLoaderListener)
                        .forceLoad();
            } else {
                getLoaderManager()
                        .initLoader(FAVOURITE_CURSOR_LOADER, loaderArgs, favMoviesLoaderListener)
                        .forceLoad();
                dontShowReviews();
                dontShowTrailers();
                showOtherDetails();
            }
        }
    }

    private void showOtherDetails() {
        String moviePosterLink = currentMovie.getPosterLink();
        Log.d(LOG_TAG, moviePosterLink);

        if (moviePosterLink.contains(ImageUtils.IMAGE_DIRECTORY)) {
            ContextWrapper cw = new ContextWrapper(this);
            File directory = cw.getDir(ImageUtils.IMAGE_DIRECTORY, Context.MODE_PRIVATE);
            File myImageFile =
                    new File(directory, String.valueOf(currentMovie.getMovieId()));
            Picasso.with(this).load(myImageFile).into(mBinding.posterDetails.ivPosterThumbnail);
        } else {
            String completePosterLink = NetworkUtils.getCompletePosterLink(moviePosterLink);
            Picasso.with(this)
                    .load(completePosterLink)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(mBinding.posterDetails.ivPosterThumbnail);
        }
        mBinding.posterDetails.tvTitle.setText(currentMovie.getTitle());
        mBinding.plotOverview.tvPlot.setText(currentMovie.getPlot());
        mBinding.posterDetails.tvUserRating.setText(String.valueOf(currentMovie.getUserRating()));
        mBinding.posterDetails.tvReleaseDate.setText(currentMovie.getReleaseDate());
    }

    private void dontShowTrailers() {
        mBinding.trailersReviews.tvTrailersHeading.setVisibility(View.INVISIBLE);
        mBinding.trailersReviews.recyclerviewTrailers.setVisibility(View.INVISIBLE);

    }

    private void dontShowReviews() {
        mBinding.trailersReviews.tvReviewsHeading.setVisibility(View.INVISIBLE);
        mBinding.trailersReviews.recyclerviewReviews.setVisibility(View.INVISIBLE);

    }

    private void showTrailers() {
        mBinding.trailersReviews.tvTrailersHeading.setVisibility(View.VISIBLE);
        mBinding.trailersReviews.recyclerviewTrailers.setVisibility(View.VISIBLE);
    }

    private void showReviews() {
        mBinding.trailersReviews.tvReviewsHeading.setVisibility(View.VISIBLE);
        mBinding.trailersReviews.recyclerviewReviews.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(String trailerKey) {
        Log.v(LOG_TAG, Uri.parse(NetworkUtils.VIDEO_BASE_URL + trailerKey).toString());
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(NetworkUtils.VIDEO_BASE_URL + trailerKey)));
    }
}
