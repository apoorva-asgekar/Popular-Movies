package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by apoorva on 5/25/17.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    private Context mContext;
    private List<String> reviewDetails = null;
    private HashMap<String, List<String>> movieExtraDetails = null;

    private final String LOG_TAG = TrailersAdapter.class.getSimpleName();

    public ReviewsAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_review_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttactToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttactToParentImmediately);

        return new ReviewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewsAdapterViewHolder holder, int position) {
        reviewDetails =
                movieExtraDetails.get(mContext.getString(R.string.reviews_key));
        String[] trailerValues = reviewDetails.get(position).split(":");
        holder.reviewAuthor.setText(trailerValues[0]);
        holder.reviewContent.setText(trailerValues[1]);
    }

    @Override
    public int getItemCount() {
        if (movieExtraDetails != null) {
            reviewDetails = movieExtraDetails.get(mContext.getString(R.string.reviews_key));
        }
        if (movieExtraDetails == null || reviewDetails == null) {
            Log.v(LOG_TAG, "reviewDetails is null");
            return 0;
        }
        Log.v(LOG_TAG, "Number of reviews: " + reviewDetails.size());
        return reviewDetails.size();
    }

    public class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView reviewAuthor;
        TextView reviewContent;

        public ReviewsAdapterViewHolder(View view) {
            super(view);
            reviewAuthor = (TextView) view.findViewById(R.id.tv_review_author);
            reviewContent = (TextView) view.findViewById(R.id.tv_review_content);
        }

    }

    public void setReviewDetails(HashMap<String, List<String>> movieExtras) {
        movieExtraDetails = movieExtras;
        notifyDataSetChanged();
    }
}
