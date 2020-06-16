package com.stdio.hashgallery.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stdio.hashgallery.DBTags;
import com.stdio.hashgallery.ImageDisplay;
import com.stdio.hashgallery.MainActivity;
import com.stdio.hashgallery.R;
import com.stdio.hashgallery.utils.MarginDecoration;
import com.stdio.hashgallery.utils.imageIndicatorListener;
import com.stdio.hashgallery.models.ImageModel;
import com.stdio.hashgallery.utils.picture_Adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mabbas007.tagsedittext.TagsEditText;

import static androidx.core.view.ViewCompat.setTransitionName;


/**
 * Author: CodeBoy722
 *
 * this fragment handles the browsing of all images in an ArrayList of pictureFacer passed in the constructor
 * the images are loaded in a ViewPager an a RecyclerView is used as a pager indicator for
 * each image in the ViewPager
 */
public class pictureBrowserFragment extends Fragment implements imageIndicatorListener {

    private  ArrayList<ImageModel> allImages = new ArrayList<>();
    private int position;
    private Context animeContx;
    private String tags;
    private ImageView image;
    private TextView tvTags;
    private ViewPager imagePager;
    private ImagesPagerAdapter pagingImages;
    private int previousSelected = -1;
    private FloatingActionButton addBtn;
    DBTags dbTags;
    public static SQLiteDatabase database;
    public static boolean activateSearch = false;
    RecyclerView imageRecycler;
    ArrayList<String> tagsList = new ArrayList<>();
    TagsAdapter adapter;

    public pictureBrowserFragment(){

    }

    public pictureBrowserFragment(ArrayList<ImageModel> allImages, int imagePosition, Context anim) {
        this.allImages = allImages;
        this.position = imagePosition;
        this.animeContx = anim;
        this.tags = allImages.get(position).getTags();
    }

    public static pictureBrowserFragment newInstance(ArrayList<ImageModel> allImages, int imagePosition, Context anim) {
        pictureBrowserFragment fragment = new pictureBrowserFragment(allImages,imagePosition,anim);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.picture_browser, container, false);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbTags = new DBTags(getContext());
        database = dbTags.getWritableDatabase();

        if (allImages.get(position).getTags() != null) {
            setTags(position, allImages);
        }
        imageRecycler = view.findViewById(R.id.recycler);
        adapter = new TagsAdapter(tagsList,getContext());
        imageRecycler.setAdapter(adapter);
        imagePager = view.findViewById(R.id.imagePager);
        pagingImages = new ImagesPagerAdapter();
        imagePager.setAdapter(pagingImages);
        imagePager.setOffscreenPageLimit(3);
        imagePager.setCurrentItem(position);//displaying the image at the current position passed by the ImageDisplay Activity

        addBtn = view.findViewById(R.id.fab);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTags();
            }
        });

        //adjusting the recyclerView indicator to the current position of the viewPager, also highlights the image in recyclerView with respect to the
        //viewPager's position
        allImages.get(position).setSelected(true);
        previousSelected = position;


        /**
         * this listener controls the visibility of the recyclerView
         * indication and it current position in respect to the image ViewPager
         */
        imagePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float positionOffset, int positionOffsetPixels) {
                position = i;
                setTags(position, allImages);
                adapter = new TagsAdapter(tagsList,getContext());
                imageRecycler.setAdapter(adapter);
            }

            @Override
            public void onPageSelected(int position) {

                if(previousSelected != -1){
                    allImages.get(previousSelected).setSelected(false);
                    previousSelected = position;
                    allImages.get(position).setSelected(true);
                }else{
                    previousSelected = position;
                    allImages.get(position).setSelected(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setTags(int position, ArrayList<ImageModel> list) {
        tagsList = new ArrayList<>();
        if (list.get(position).getTags() != null) {
            tagsList.addAll(Arrays.asList(list.get(position).getTags().split(" ")));
        }
    }

    private TextView getTvTag(String text) {
        final TextView tv1 = new TextView(getContext());
        tv1.setText(text);
        tv1.setTextSize(16);
        tv1.setTextColor(Color.parseColor("#ff0000ff"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(7,0,7,0);
        tv1.setLayoutParams(params);
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.setDatabaseList();
                MainActivity.searchView.setIconified(false);//open searchView
                MainActivity.searchView.setQuery(tv1.getText().toString(), false);
                activateSearch = true;
                getFragmentManager().popBackStack();
                ImageDisplay.activity.finish();
            }
        });
        return tv1;
    }

    private void addTags() {
        View dialogView = getLayoutInflater().inflate(R.layout.add_dialog, null);
        final TagsEditText tagsEditText = dialogView.findViewById(R.id.tagsEditText);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder tagsEt = new StringBuilder();
                        List<String> tagsListg = tagsEditText.getTags();
                        for (String currentTag : tagsListg) {
                            tagsEt.append("#").append(currentTag.replace(" ", "_")).append(" ");
                        }
                        if (allImages.get(position).getTags() == null) {
                            addToDB(allImages.get(position).getImageUri(), allImages.get(position).getPicturePath(), tagsEt.toString());
                        }
                        else {
                            ImageModel imageModel = allImages.get(position);
                            updateDB(imageModel.getTags() + tagsEt.toString(), imageModel.getId());
                        }
                        allImages.clear();
                        allImages.addAll(getAllImagesByFolder());
                        setTags(position, allImages);
                        adapter = new TagsAdapter(tagsList,getContext());
                        pagingImages.notifyDataSetChanged();
                        imageRecycler.setAdapter(adapter);
                        pagingImages.notifyDataSetChanged();
                        Toast.makeText(getContext(), tagsEt.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        dialog.setTitle("Добавить теги");
        dialog.setNegativeButton("Отмена", null);
        dialog.show();
    }

    private void updateDB(String tags, int id) {
        database.execSQL("UPDATE tags SET tags = '" + tags + "' WHERE _id='"
                + id + "';");
    }

    private void addToDB(String uri, String path, String tags) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBTags.KEY_URI, uri);
        contentValues.put(DBTags.KEY_PATH, path);
        contentValues.put(DBTags.KEY_TAGS, tags);
        database.insert(DBTags.TABLE_TAGS, null, contentValues);
    }

    public ArrayList<ImageModel> getAllImagesByFolder(){
        ArrayList<ImageModel> images = new ArrayList<>();
        Uri allVideosuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = getActivity().getContentResolver().query( allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[] {"%"+ImageDisplay.pathh+"%"}, null);
        try {
            cursor.moveToFirst();
            do{
                ImageModel pic = new ImageModel();

                String picPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String uri = Uri.parse(picPath).toString();

                pic.setPicturName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                pic.setPicturePath(picPath);
                pic.setImageUri(uri);
                pic.setPictureSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                pic = getNormalModelByUri(pic);

                images.add(pic);
            }while(cursor.moveToNext());
            cursor.close();
            ArrayList<ImageModel> reSelection = new ArrayList<>();
            for(int i = images.size()-1;i > -1;i--){
                reSelection.add(images.get(i));
            }
            images = reSelection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    private ImageModel getNormalModelByUri(ImageModel imageModel) {
        DBTags dbTags = new DBTags(getContext());
        SQLiteDatabase database = dbTags.getWritableDatabase();
        Cursor cursor = database.query(DBTags.TABLE_TAGS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int uriIndex = cursor.getColumnIndex(DBTags.KEY_URI);
            int tagsIndex = cursor.getColumnIndex(DBTags.KEY_TAGS);
            int idIndex = cursor.getColumnIndex(DBTags.KEY_ID);
            do {
                if (cursor.getString(uriIndex).equals(imageModel.getImageUri())) {
                    imageModel.setTags(cursor.getString(tagsIndex));
                    imageModel.setId(cursor.getInt(idIndex));
                    return imageModel;
                }
            } while (cursor.moveToNext());
        } else {
            cursor.close();
        }
        return imageModel;
    }


    /**
     * this method of the imageIndicatorListerner interface helps in communication between the fragment and the recyclerView Adapter
     * each time an iten in the adapter is clicked the position of that item is communicated in the fragment and the position of the
     * viewPager is adjusted as follows
     * @param ImagePosition The position of an image item in the RecyclerView Adapter
     */
    @Override
    public void onImageIndicatorClicked(int ImagePosition) {

        //the below lines of code highlights the currently select image in  the indicatorRecycler with respect to the viewPager position
        if(previousSelected != -1){
            allImages.get(previousSelected).setSelected(false);
            previousSelected = ImagePosition;
        }else{
            previousSelected = ImagePosition;
        }

        imagePager.setCurrentItem(ImagePosition);
    }

    /**
     * the imageViewPager's adapter
     */
    private class ImagesPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return allImages.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup containerCollection, int position) {
            LayoutInflater layoutinflater = (LayoutInflater) containerCollection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutinflater.inflate(R.layout.picture_browser_pager,null);
               image = view.findViewById(R.id.image);

            setTransitionName(image, String.valueOf(position)+"picture");

            ImageModel pic = allImages.get(position);
            Glide.with(animeContx)
                    .load(pic.getPicturePath())
                    .apply(new RequestOptions().fitCenter())
                    .into(image);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(addBtn.isShown()){
                        addBtn.hide();
                        imageRecycler.setVisibility(View.GONE);
                    }else{
                        addBtn.show();
                        imageRecycler.setVisibility(View.VISIBLE);
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



            ((ViewPager) containerCollection).addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup containerCollection, int position, Object view) {
            ((ViewPager) containerCollection).removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == ((View) object);
        }
    }
}
