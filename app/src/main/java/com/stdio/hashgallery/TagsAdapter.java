package com.stdio.hashgallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import static androidx.core.view.ViewCompat.setTransitionName;

/**
 * Author CodeBoy722
 *
 * A RecyclerView Adapter class that's populates a RecyclerView with images from
 * a folder on the device external storage
 */
public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.PicHolder> {

    public class PicHolder extends RecyclerView.ViewHolder{

        public TextView tvTag;
        CardView cardView;

        PicHolder(@NonNull View itemView) {
            super(itemView);

            tvTag = itemView.findViewById(R.id.tvTag);
            cardView = itemView.findViewById(R.id.cv);
        }
    }

    private ArrayList<String> tagsList;
    private Context pictureContx;


    public TagsAdapter(ArrayList<String> tagsList, Context pictureContx) {
        this.tagsList = tagsList;
        this.pictureContx = pictureContx;
    }

    @NonNull
    @Override
    public PicHolder onCreateViewHolder(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View cell = inflater.inflate(R.layout.tags_item, container, false);
        return new PicHolder(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull final PicHolder holder, final int position) {
        holder.tvTag.setText(tagsList.get(position));
        holder.tvTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.searchView.setIconified(false);//open searchView
                MainActivity.searchView.setQuery(holder.tvTag.getText().toString(), false);
                ReviewImageActivity.activity.finish();
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.searchView.setIconified(false);//open searchView
                MainActivity.searchView.setQuery(holder.tvTag.getText().toString(), false);
                ReviewImageActivity.activity.finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return tagsList.size();
    }

}
