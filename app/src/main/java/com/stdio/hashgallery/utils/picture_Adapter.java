package com.stdio.hashgallery.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stdio.hashgallery.DBTags;
import com.stdio.hashgallery.R;
import com.stdio.hashgallery.models.ImageModel;

import java.util.ArrayList;
import static androidx.core.view.ViewCompat.setTransitionName;

/**
 * Author CodeBoy722
 *
 * A RecyclerView Adapter class that's populates a RecyclerView with images from
 * a folder on the device external storage
 */
public class picture_Adapter extends RecyclerView.Adapter<PicHolder> {

    private ArrayList<ImageModel> pictureList;
    private Context pictureContx;
    private final itemClickListener picListerner;

    /**
     *
     * @param pictureList ArrayList of pictureFacer objects
     * @param pictureContx The Activities Context
     * @param picListerner An interface for listening to clicks on the RecyclerView's items
     */
    public picture_Adapter(ArrayList<ImageModel> pictureList, Context pictureContx, itemClickListener picListerner) {
        this.pictureList = pictureList;
        this.pictureContx = pictureContx;
        this.picListerner = picListerner;
    }

    @NonNull
    @Override
    public PicHolder onCreateViewHolder(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View cell = inflater.inflate(R.layout.pic_holder_item, container, false);
        return new PicHolder(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull final PicHolder holder, final int position) {

        final ImageModel image = pictureList.get(position);

        Glide.with(pictureContx)
                .load(image.getPicturePath())
                .apply(new RequestOptions().centerCrop())
                .into(holder.picture);

        setTransitionName(holder.picture, String.valueOf(position) + "_image");

        holder.picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picListerner.onPicClicked(holder,position, pictureList);
            }
        });

    }

    private String getTagsByUri(String uri) {
        DBTags dbTags = new DBTags(pictureContx);
        SQLiteDatabase database = dbTags.getWritableDatabase();
        Cursor cursor = database.query(DBTags.TABLE_TAGS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int uriIndex = cursor.getColumnIndex(DBTags.KEY_URI);
            int tagsIndex = cursor.getColumnIndex(DBTags.KEY_TAGS);
            do {
                if (cursor.getString(uriIndex).equals(uri)) {
                    return cursor.getString(tagsIndex);
                }
            } while (cursor.moveToNext());
        } else {
            cursor.close();
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }
}
