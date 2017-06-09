package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.utilities.NetworkUtils;

import java.util.HashMap;
import java.util.List;

import static android.R.attr.id;

/**
 * Created by apoorva on 5/25/17.
 */

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersAdapterViewHolder> {

    private Context mContext;
    private List<String> trailerDetails = null;
    private HashMap<String, List<String>> movieExtraDetails = null;
    private final TrailersAdapterOnClickHandler mOnClickHandler;

    private final String LOG_TAG = TrailersAdapter.class.getSimpleName();

    public interface TrailersAdapterOnClickHandler {
        void onClick(String trailerKey);
    }

    public TrailersAdapter(TrailersAdapterOnClickHandler onClickHandler, Context context) {
        this.mContext = context;
        this.mOnClickHandler = onClickHandler;
    }

    @Override
    public TrailersAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_trailer_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttactToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttactToParentImmediately);

        return new TrailersAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailersAdapterViewHolder holder, int position) {
        trailerDetails =
                movieExtraDetails.get(mContext.getString(R.string.trailers_key));
        String[] trailerValues = trailerDetails.get(position).split(":");
        holder.trailerName.setText(trailerValues[0]);
    }

    @Override
    public int getItemCount() {
        if (movieExtraDetails != null) {
            trailerDetails = movieExtraDetails.get(mContext.getString(R.string.trailers_key));
        }
        if (movieExtraDetails == null || trailerDetails == null) {
            return 0;
        }
        Log.v(LOG_TAG, "Number of trailers: " + trailerDetails.size());
        return trailerDetails.size();
    }

    public class TrailersAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        ImageView playButton;
        TextView trailerName;
        ImageView shareButton;

        public TrailersAdapterViewHolder(View view) {
            super(view);
            playButton = (ImageView) view.findViewById(R.id.play_button);
            trailerName = (TextView) view.findViewById(R.id.tv_trailer_name);
            shareButton = (ImageView) view.findViewById(R.id.share_button);
            playButton.setOnClickListener(this);
            trailerName.setOnClickListener(this);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] trailerValues = trailerDetails.get(getAdapterPosition()).split(":");
                    String trailerKey = trailerValues[1];
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM,
                            Uri.parse(NetworkUtils.VIDEO_BASE_URL + trailerKey));
                    shareIntent.setType("video/*");
                    mContext.startActivity(Intent.createChooser(shareIntent, "Share using"));
                }
            });
        }

        @Override
        public void onClick(View v) {
            String[] trailerValues = trailerDetails.get(getAdapterPosition()).split(":");
            mOnClickHandler.onClick(trailerValues[1]);
        }
    }

    public void setTrailerDetails(HashMap<String, List<String>> movieExtras) {
        movieExtraDetails = movieExtras;
        notifyDataSetChanged();
    }
}
