package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class MoviesGridAdapter extends RecyclerView.Adapter<MoviesGridAdapter.VH> {

    private final Context context;
    private final ArrayList<MovieItem> items;

    public MoviesGridAdapter(Context context, ArrayList<MovieItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_movie_card_light, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MovieItem m = items.get(position);

        h.tvTitle.setText(m.title != null ? m.title : "Movie");

        if (m.posterUrl != null && !m.posterUrl.isEmpty()) {
            Glide.with(context)
                    .load(m.posterUrl)
                    .placeholder(R.drawable.poster_default_movie)
                    .into(h.ivPoster);
        } else if (m.posterResId != 0) {
            h.ivPoster.setImageResource(m.posterResId);
        } else {
            h.ivPoster.setImageResource(R.drawable.poster_default_movie);
        }

        h.card.setOnClickListener(v -> {
            Intent i = new Intent(context, MovieContentActivity.class);

            if (m.isUserTitle) {
                i.putExtra(MovieContentActivity.EXTRA_TITLE_ID, m.id);
            } else {
                i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, m.id);
                i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, m.title);
                i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL, m.trailerUrl);
                i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, m.posterResId);
            }

            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView ivPoster;
        TextView tvTitle;

        VH(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardMovie);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
