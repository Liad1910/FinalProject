package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TitlesAdapter extends RecyclerView.Adapter<TitlesAdapter.VH> {

    private final Context context;
    private final List<TitleCard> items;

    public TitlesAdapter(Context context, List<TitleCard> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_title_poster, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TitleCard item = items.get(position);

        // פוסטר מתוך drawable לפי posterResName
        int resId = 0;
        if (item.posterResName != null) {
            resId = context.getResources().getIdentifier(
                    item.posterResName, "drawable", context.getPackageName()
            );
        }

        if (resId == 0) {
            // fallback לפי סוג
            String fallback = ("series".equals(item.type)) ? "poster_default_series" : "poster_default_movie";
            resId = context.getResources().getIdentifier(fallback, "drawable", context.getPackageName());
        }

        if (resId != 0) h.imgPoster.setImageResource(resId);

        // ✅ לחיצה -> פתיחת עמוד לפי titleId ב-Firestore
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, MovieContentActivity.class);
            i.putExtra(MovieContentActivity.EXTRA_TITLE_ID, item.id);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        VH(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
        }
    }
}
