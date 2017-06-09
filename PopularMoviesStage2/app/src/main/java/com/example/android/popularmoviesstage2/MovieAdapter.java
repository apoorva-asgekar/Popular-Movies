package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmoviesstage2.utilities.ImageUtils;
import com.example.android.popularmoviesstage2.utilities.MovieJsonUtils;
import com.example.android.popularmoviesstage2.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by apoorva on 4/3/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private final static String LOG_TAG = MovieAdapter.class.getSimpleName();

    private List<Movie> mListOfMovies;
    private final MovieAdapterOnClickHandler mOnClickHandler;
    private Context mContext;

    /**
     * On Click Handler interface to handle user clicks on the recylcer view.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    public MovieAdapter(MovieAdapterOnClickHandler clickHandler
            , Context context) {
        this.mOnClickHandler = clickHandler;
        this.mContext = context;
    }

    @Override
    public MovieAdapter.MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_grid_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttactToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttactToParentImmediately);

        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapter.MovieAdapterViewHolder holder, int position) {
        String moviePosterLink = mListOfMovies.get(position).getPosterLink();
        if (moviePosterLink.contains(ImageUtils.IMAGE_DIRECTORY)) {
            ContextWrapper cw = new ContextWrapper(mContext);
            File directory = cw.getDir(ImageUtils.IMAGE_DIRECTORY, Context.MODE_PRIVATE);
            File myImageFile =
                    new File(directory, String.valueOf(mListOfMovies.get(position).getMovieId()));
            Picasso.with(mContext).load(myImageFile).into(holder.mMoviePoster);
        } else {
            String completePosterLink = NetworkUtils.getCompletePosterLink(moviePosterLink);
            Picasso.with(mContext)
                    .load(completePosterLink)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(holder.mMoviePoster);
        }
    }

    @Override
    public int getItemCount() {
        if (mListOfMovies == null) {
            return 0;
        }
        return mListOfMovies.size();
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        ImageView mMoviePoster;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mMoviePoster = (ImageView) view.findViewById(R.id.iv_movie_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Movie currentMovie = mListOfMovies.get(adapterPosition);
            mOnClickHandler.onClick(currentMovie);
        }
    }

    /**
     * This method is used to set the weather movies list on the MovieAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param listOfMovies The new list of movies to be displayed.
     */
    public void setMovieData(List<Movie> listOfMovies) {
        mListOfMovies = listOfMovies;
        notifyDataSetChanged();
    }
}
