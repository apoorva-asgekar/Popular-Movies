package com.example.android.popularmoviesstage2.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.example.android.popularmoviesstage2.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by apoorva on 4/4/17.
 * Class used to connect to the MovieDb API and fetch data being displayed by the App.
 */

public final class NetworkUtils {
    private final static String LOG_TAG = NetworkUtils.class.getSimpleName();

    public final static String API_BASE_URL = "https://api.themoviedb.org/3/";
    public final static String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    public final static String VIDEO_BASE_URL = "https://www.youtube.com/watch?v=";
    public final static String IMAGE_FILE_SIZE = "w185/";

    public final static String API_POPULAR_ENDPOINT = "movie/popular";
    public final static String API_TOP_RATED_ENDPOINT = "movie/top_rated";
    public final static String API_DETAILS_PREFIX = "movie/";
    public final static String API_TRAILER_SUFFIX = "/videos";
    public final static String API_REVIEWS_SUFFIX = "/reviews";

    private final static String API_KEY = "";

    private final static String QUERY_PARAM_API_KEY = "api_key";
    private final static String QUERY_PARAM_PAGE = "page";

    /**
     * Returns a java URL object needed to make the network call to the MovieDb API.
     *
     * @param requestQuery - String form of the url
     * @return URL - java object of type URL
     */
    public static URL buildUrl(String requestQuery, String pageNumber) {
        Uri builtUri = Uri.parse(requestQuery).buildUpon()
                .appendQueryParameter(QUERY_PARAM_API_KEY, API_KEY)
                .appendQueryParameter(QUERY_PARAM_PAGE, pageNumber)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL Excpetion in buildUrl", e);
        }
        return url;
    }

    /**
     * Connects to the MovieDb API and gets the response to the Http request.
     *
     * @param url - URL for the API request
     * @return String which contains the API response
     * @throws IOException
     */
    public static String getResponseFromUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        String resultJson = null;

        try {
            if (url != null) {
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            if (urlConnection != null) {
                inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    Scanner scanner = new Scanner(inputStream);
                    scanner.useDelimiter("\\A");

                    if (scanner.hasNext()) {
                        resultJson = scanner.next();
                    }
                }
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return resultJson;
                } else {
                    Log.e(LOG_TAG, "Http request returned with an error code: " + resultJson);
                }
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    //Check if internet connection is currently available.
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static String getCompletePosterLink(String posterPath) {
        return IMAGE_BASE_URL + IMAGE_FILE_SIZE
                + posterPath;
    }

    public static String getRequestUrlWithPreference(String sortOrder, Context context) {
        String requestUrlString = API_BASE_URL;
        String prefPopular = context.getResources().getString(R.string.pref_sort_popular_value);
        String prefRatings = context.getResources().getString(R.string.pref_sort_ratings_value);
        if (sortOrder.equals(prefPopular)) {
            requestUrlString += API_POPULAR_ENDPOINT;
        } else if (sortOrder.equals(prefRatings)) {
            requestUrlString += API_TOP_RATED_ENDPOINT;
        } else {
            return null;
        }
        return requestUrlString;
    }

    public static String getRequestUrlForVideos(String movieId, Context context) {
        return API_BASE_URL + API_DETAILS_PREFIX + movieId + API_TRAILER_SUFFIX;
    }

    public static String getRequestUrlForReviews(String movieId, Context context) {
        return API_BASE_URL + API_DETAILS_PREFIX + movieId + API_REVIEWS_SUFFIX;
    }
}
