package com.stdio.hashgallery.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.stdio.hashgallery.ImageDisplay;
import com.stdio.hashgallery.MainActivity;
import com.stdio.hashgallery.R;

import java.util.ArrayList;

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
                ImageDisplay.activity.finish();
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.searchView.setIconified(false);//open searchView
                MainActivity.searchView.setQuery(holder.tvTag.getText().toString(), false);
                ImageDisplay.activity.finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return tagsList.size();
    }

}
