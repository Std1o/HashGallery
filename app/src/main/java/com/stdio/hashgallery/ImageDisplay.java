package com.stdio.hashgallery;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.transition.Fade;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stdio.hashgallery.fragments.pictureBrowserFragment;
import com.stdio.hashgallery.utils.MarginDecoration;
import com.stdio.hashgallery.utils.PicHolder;
import com.stdio.hashgallery.utils.itemClickListener;
import com.stdio.hashgallery.models.ImageModel;
import com.stdio.hashgallery.utils.picture_Adapter;

import java.util.ArrayList;

/**
 * Author CodeBoy722
 *
 * This Activity get a path to a folder that contains images from the MainActivity Intent and displays
 * all the images in the folder inside a RecyclerView
 */

public class ImageDisplay extends AppCompatActivity implements itemClickListener {

    RecyclerView imageRecycler;
    ArrayList<ImageModel> allpictures;
    ProgressBar load;
    String foldePath;
    TextView folderName;
    ImageView ivArrowBack;
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);
        activity = this;

        folderName = findViewById(R.id.foldername);
        ivArrowBack = findViewById(R.id.ivArrowBack);
        folderName.setText(getIntent().getStringExtra("folderName"));

        foldePath =  getIntent().getStringExtra("folderPath");
        allpictures = new ArrayList<>();
        imageRecycler = findViewById(R.id.recycler);
        imageRecycler.addItemDecoration(new MarginDecoration(this));
        imageRecycler.hasFixedSize();
        load = findViewById(R.id.loader);


        if(allpictures.isEmpty()){
            load.setVisibility(View.VISIBLE);
            allpictures = getAllImagesByFolder(foldePath);
            imageRecycler.setAdapter(new picture_Adapter(allpictures,ImageDisplay.this,this));
            load.setVisibility(View.GONE);
        }else{

        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivArrowBack:
                onBackPressed();
                break;
        }
    }

    /**
     *
     * @param holder The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     * @param pics An ArrayList of all the items in the Adapter
     */
    @Override
    public void onPicClicked(PicHolder holder, int position, ArrayList<ImageModel> pics) {
        pictureBrowserFragment browser = pictureBrowserFragment.newInstance(pics,position,ImageDisplay.this);

        // Note that we need the API version check here because the actual transition classes (e.g. Fade)
        // are not in the support library and are only available in API 21+. The methods we are calling on the Fragment
        // ARE available in the support library (though they don't do anything on API < 21)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //browser.setEnterTransition(new Slide());
            //browser.setExitTransition(new Slide()); uncomment this to use slide transition and comment the two lines below
            browser.setEnterTransition(new Fade());
            browser.setExitTransition(new Fade());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .addSharedElement(holder.picture, position+"picture")
                .add(R.id.displayContainer, browser)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onPicClicked(String pictureFolderPath,String folderName) {

    }

    /**
     * This Method gets all the images in the folder paths passed as a String to the method and returns
     * and ArrayList of pictureFacer a custom object that holds data of a given image
     * @param path a String corresponding to a folder path on the device external storage
     */
    public ArrayList<ImageModel> getAllImagesByFolder(String path){
        ArrayList<ImageModel> images = new ArrayList<>();
        Uri allVideosuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = ImageDisplay.this.getContentResolver().query( allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[] {"%"+path+"%"}, null);
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
        DBTags dbTags = new DBTags(this);
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


}
