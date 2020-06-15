package com.stdio.hashgallery;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stdio.hashgallery.fragments.pictureBrowserFragment;
import com.stdio.hashgallery.models.ImageModel;

import java.util.ArrayList;
import java.util.List;

import mabbas007.tagsedittext.TagsEditText;

public class ReviewImageActivity extends AppCompatActivity {

    public static ImageModel imageModel;
    private ImageView image;
    private TextView tvTags;
    private FloatingActionButton addBtn;
    DBTags dbTags;
    public static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_image);

        System.out.println(imageModel.getImageUri());

        dbTags = new DBTags(this);
        database = dbTags.getWritableDatabase();

        addBtn = findViewById(R.id.fab);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTags();
            }
        });

        image = findViewById(R.id.image);
        tvTags = findViewById(R.id.tvTags);
        if (imageModel.getTags() != null) {
            tvTags.setText(imageModel.getTags());
        }

        tvTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.searchView.setIconified(false);//open searchView
                MainActivity.searchView.setQuery(imageModel.getTags(), false);
                finish();
            }
        });

        Glide.with(this)
                .load(imageModel.getPicturePath())
                .apply(new RequestOptions().fitCenter())
                .into(image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(addBtn.isShown()){
                    addBtn.hide();
                    tvTags.setVisibility(View.GONE);
                }else{
                    addBtn.show();
                    tvTags.setVisibility(View.VISIBLE);
                }

                /**
                 * uncomment the below condition and comment the one above to control recyclerView visibility automatically
                 * when image is clicked
                 */
                    /*if(viewVisibilityController == 0){
                     indicatorRecycler.setVisibility(View.VISIBLE);
                     visibiling();
                 }else{
                     viewVisibilitylooper++;
                 }*/

            }
        });
    }

    private void addTags() {
        View dialogView = getLayoutInflater().inflate(R.layout.add_dialog, null);
        final TagsEditText tagsEditText = dialogView.findViewById(R.id.tagsEditText);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder tagsEt = new StringBuilder();
                        List<String> tagsList = tagsEditText.getTags();
                        for (String currentTag : tagsList) {
                            tagsEt.append("#").append(currentTag.replace(" ", "_")).append(" ");
                        }
                        addToDB(imageModel.getImageUri(), tagsEt.toString());
                        Toast.makeText(ReviewImageActivity.this, tagsEt.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        dialog.setTitle("Добавить теги");
        dialog.setNegativeButton("Отмена", null);
        dialog.show();
    }

    private void addToDB(String uri, String tags) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBTags.KEY_URI, uri);
        contentValues.put(DBTags.KEY_TAGS, tags);
        database.insert(DBTags.TABLE_TAGS, null, contentValues);
    }
}
