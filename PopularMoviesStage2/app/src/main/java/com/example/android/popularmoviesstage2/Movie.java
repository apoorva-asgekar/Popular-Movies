package com.example.android.popularmoviesstage2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by apoorva on 4/3/17.
 * Movie object to hold all data related to a movie fetched from the API.
 * This class implements the parcelable interface so that its objects can be transferred via Intents.
 */
//TODOCOMPLETED - Implement Parcelable interface
public class Movie implements Parcelable {
    private int mMovieId;
    private String mTitle;
    private String mPosterLink;
    private String mPlot;
    private Float mUserRating;
    private String mReleaseDate;

    public Movie(int movieId, String title, String posterLink, String plot, Float userRating
            , String releaseDate) {
        this.mMovieId = movieId;
        this.mTitle = title;
        this.mPosterLink = posterLink;
        this.mPlot = plot;
        this.mUserRating = userRating;
        this.mReleaseDate = releaseDate;
    }

    /**
     * Constructor to use when re-constructing object
     * from a parcel
     *
     * @param in a parcel from which to read this object
     */
    public Movie(Parcel in) {
        mMovieId = in.readInt();
        mTitle = in.readString();
        mPosterLink = in.readString();
        mPlot = in.readString();
        mUserRating = in.readFloat();
        mReleaseDate = in.readString();
    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {

        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public int getMovieId() {
        return mMovieId;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getPosterLink() {
        return this.mPosterLink;
    }

    public String getPlot() {
        return this.mPlot;
    }

    public Float getUserRating() {
        return this.mUserRating;
    }

    public String getReleaseDate() {
        return this.mReleaseDate;
    }

    public boolean hasMovieDetails() {
        if (this.mPlot == null
                && this.mUserRating == null
                && this.mReleaseDate == null) {
            return false;
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mMovieId);
        parcel.writeString(mTitle);
        parcel.writeString(mPosterLink);
        parcel.writeString(mPlot);
        parcel.writeFloat(mUserRating);
        parcel.writeString(mReleaseDate);
    }
}
