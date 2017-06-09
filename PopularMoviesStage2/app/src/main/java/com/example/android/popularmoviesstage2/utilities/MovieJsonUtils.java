package com.example.android.popularmoviesstage2.utilities;

import android.content.Context;
import android.util.Log;

import com.example.android.popularmoviesstage2.Movie;
import com.example.android.popularmoviesstage2.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by apoorva on 4/5/17.
 * Class which converts JSON strings to an ArryList of Movie Objects.
 */

public final class MovieJsonUtils {
    private final static String LOG_TAG = MovieJsonUtils.class.getSimpleName();

    private final static String JSON_PAGE = "page";
    private final static String JSON_RESULTS = "results";
    private final static String JSON_TOTAL_PAGES = "total_pages";
    private final static String JSON_POSTER_PATH = "poster_path";
    private final static String JSON_MOVIE_ID = "id";
    private final static String JSON_MOVIE_TITLE = "title";
    private final static String JSON_MOVIE_PLOT = "overview";
    private final static String JSON_MOVIE_USER_RATING = "vote_average";
    private final static String JSON_MOVIE_RELEASE_DATE = "release_date";
    private final static String JSON_VIDEO_SITE = "site";
    private final static String JSON_VIDEO_TYPE = "type";
    private final static String JSON_VIDEO_NAME = "name";
    private final static String JSON_VIDEO_KEY = "key";
    private final static String JSON_REVIEW_AUTHOR = "author";
    private final static String JSON_REVIEW_CONTENT = "content";

    public static List<Movie> getMovieDetails(String requestUrl)
            throws JSONException {

        List<Movie> movieResultsArrayList = new ArrayList<Movie>();

        //Getting response from page 1 of the API to get the total number of pages.
        String pageOneResponse = getDataFromMovieDb(requestUrl, "1");

        JSONObject movieResponseObject = new JSONObject(pageOneResponse);
        int totalPages = movieResponseObject.getInt(JSON_TOTAL_PAGES);

        //Currently assuming that only the top 20 (page 1) top_rated and popular movies are being displayed.

        JSONArray resultsArray = movieResponseObject.getJSONArray(JSON_RESULTS);
        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject resultObject = resultsArray.getJSONObject(i);

            int movieId = resultObject.getInt(JSON_MOVIE_ID);
            String movieTitle = resultObject.getString(JSON_MOVIE_TITLE);
            String posterPath = resultObject.getString(JSON_POSTER_PATH);
            String moviePlot = resultObject.getString(JSON_MOVIE_PLOT);
            Float movieUserRating =
                    Float.valueOf((float) resultObject.getDouble(JSON_MOVIE_USER_RATING));
            String movieReleaseDate = resultObject.getString(JSON_MOVIE_RELEASE_DATE);

            Movie newMovie = new Movie(movieId, movieTitle, posterPath, moviePlot, movieUserRating,
                    movieReleaseDate);
            movieResultsArrayList.add(newMovie);

        }
        return movieResultsArrayList;
    }

    public static HashMap<String, List<String>> getMovieExtras(String urlStringTrailers,
                                                               String urlStringReviews,
                                                               Context context)
            throws JSONException {
        HashMap<String, List<String>> movieExtrasMap = new HashMap<String, List<String>>();
        List<String> movieTrailers = new ArrayList<String>();
        List<String> movieReviews = new ArrayList<String>();

        //Assuming that only page 1 details are displayed.

        //Getting the movie trailers from the API
        String movieTrailersJsonResponseString = getDataFromMovieDb(urlStringTrailers, "1");

        JSONObject videoResponseObject = new JSONObject(movieTrailersJsonResponseString);
        JSONArray videoResultsArray = videoResponseObject.getJSONArray(JSON_RESULTS);
        for (int i = 0; i < videoResultsArray.length(); i++) {
            JSONObject resultObject = videoResultsArray.getJSONObject(i);

            if (showTrailer(resultObject)) {
                String trailerName = resultObject.getString(JSON_VIDEO_NAME);
                String trailerKey = resultObject.getString(JSON_VIDEO_KEY);

                Log.v(LOG_TAG, "Trailer details from JSON: " + trailerName + "-" + trailerKey);
                movieTrailers.add(trailerName + ":" + trailerKey);
            }
        }
        if (!movieTrailers.isEmpty()) {
            movieExtrasMap.put(context.getString(R.string.trailers_key), movieTrailers);
        }

        //Getting the movie reviews from the API
        String movieReviewsJsonResponseString = getDataFromMovieDb(urlStringReviews, "1");

        JSONObject reviewResponseObject = new JSONObject(movieReviewsJsonResponseString);
        JSONArray reviewResultsArray = reviewResponseObject.getJSONArray(JSON_RESULTS);
        for (int i = 0; i < reviewResultsArray.length(); i++) {
            JSONObject resultObject = reviewResultsArray.getJSONObject(i);

            String reviewAuthor = resultObject.getString(JSON_REVIEW_AUTHOR);
            String reviewContent = resultObject.getString(JSON_REVIEW_CONTENT);

            movieReviews.add(reviewAuthor.trim() + ":" + reviewContent.trim());

        }
        if (!movieReviews.isEmpty()) {
            movieExtrasMap.put(context.getString(R.string.reviews_key), movieReviews);
        }

        return movieExtrasMap;
    }

    private static String getDataFromMovieDb(String requestUrlString, String pageNumber)
            throws JSONException {

        URL requestUrl = NetworkUtils.buildUrl(requestUrlString, pageNumber);
        String responseStr = null;
        try {
            responseStr = NetworkUtils.getResponseFromUrl(requestUrl);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOEXception while querying the API.", e);
        }

        return responseStr;
    }

    private static boolean showTrailer(JSONObject trailerResultObject)
            throws JSONException {
        if (trailerResultObject.getString(JSON_VIDEO_SITE).equals("YouTube")) {
            String videoType = trailerResultObject.getString(JSON_VIDEO_TYPE);
            if (videoType.equals("Trailer") || videoType.equals("Teaser")) {
                return true;
            }
        }
        return false;
    }

}
