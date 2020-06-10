package com.stdio.hashgallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.stdio.hashgallery.fragments.pictureBrowserFragment;
import com.stdio.hashgallery.utils.MarginDecoration;
import com.stdio.hashgallery.utils.PicHolder;
import com.stdio.hashgallery.models.ImageFolderModel;
import com.stdio.hashgallery.utils.itemClickListener;
import com.stdio.hashgallery.models.ImageModel;
import com.stdio.hashgallery.utils.pictureFolderAdapter;
import com.stdio.hashgallery.utils.picture_Adapter;

import java.util.ArrayList;

/**
 *
 * The main Activity start and loads all folders containing images in a RecyclerView
 * this folders are gotten from the MediaStore by the Method getPicturePaths()
 */
public class MainActivity extends AppCompatActivity implements itemClickListener {

    RecyclerView folderRecycler;
    TextView empty;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 134;
    ArrayList<ImageModel> allpictures = new ArrayList<>();
    public static SearchView searchView;

    /**
     * Request the user for permission to access media files and read images on the device
     * this will be useful as from api 21 and above, if this check is not done the Activity will crash
     *
     * Setting up the RecyclerView and getting all folders that contain pictures from the device
     * the getPicturePaths() returns an ArrayList of imageFolder objects that is then used to
     * create a RecyclerView Adapter that is set to the RecyclerView
     *
     * @param savedInstanceState saving the activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        else {
            init();
        }
    }

    private void init() {
        empty =findViewById(R.id.empty);

        folderRecycler = findViewById(R.id.folderRecycler);
        folderRecycler.addItemDecoration(new MarginDecoration(this));
        folderRecycler.hasFixedSize();
        initFolders();
        searchView = findViewById(R.id.searchView);
        setOnQueryTextListener();
    }

    private void initFolders() {
        ArrayList<ImageFolderModel> folds = getPicturePaths();

        if(folds.isEmpty()){
            empty.setVisibility(View.VISIBLE);
        }else{
            RecyclerView.Adapter folderAdapter = new pictureFolderAdapter(folds,MainActivity.this,this);
            folderRecycler.setAdapter(folderAdapter);
        }
    }

    private void setOnQueryTextListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(allpictures.isEmpty()){
                    allpictures = getAllImagesByFolder();
                }
                String query = s.replace("#", "");
                if (getFilteredList(query).size() == 0 || query.isEmpty()) {
                    initFolders();
                }
                else {
                    folderRecycler.setAdapter(new picture_Adapter(getFilteredList(query),MainActivity.this,MainActivity.this));
                }
                return false;
            }
        });
    }

    private ArrayList<ImageModel> getFilteredList(String query) {
        ArrayList<ImageModel> filteredList = new ArrayList<>();
        for (ImageModel imageModel : allpictures) {
            if (imageModel.getTags().contains(query)) {
                filteredList.add(imageModel);
            }
        }
        return filteredList;
    }

    @Override
    public void onPicClicked(PicHolder holder, int position, ArrayList<ImageModel> pics) {
        ReviewImageActivity.imageModel = pics.get(position);
        startActivity(new Intent(this, ReviewImageActivity.class));
    }

    public ArrayList<ImageModel> getAllImagesByFolder(){
        ArrayList<ImageModel> images = new ArrayList<>();
        Uri allVideosuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = this.getContentResolver().query( allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[] {"%"+"%"}, null);
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
                pic.setTags(getTagsByUri(uri));

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

    private String getTagsByUri(String uri) {
        DBTags dbTags = new DBTags(this);
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

    /**
     * @return
     * gets all folders with pictures on the device and loads each of them in a custom object imageFolder
     * the returns an ArrayList of these custom objects
     */
    private ArrayList<ImageFolderModel> getPicturePaths(){
        ArrayList<ImageFolderModel> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do{
                ImageFolderModel folds = new ImageFolderModel();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder+"/"));
                folderpaths = folderpaths+folder+"/";
                if (!picPaths.contains(folderpaths)) {
                    picPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.addpics();
                    picFolders.add(folds);
                }else{
                    for(int i = 0;i<picFolders.size();i++){
                        if(picFolders.get(i).getPath().equals(folderpaths)){
                            picFolders.get(i).setFirstPic(datapath);
                            picFolders.get(i).addpics();
                        }
                    }
                }
            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i = 0;i < picFolders.size();i++){
            Log.d("picture folders",picFolders.get(i).getFolderName()+" and path = "+picFolders.get(i).getPath()+" "+picFolders.get(i).getNumberOfPics());
        }

        //reverse order ArrayList
       /* ArrayList<imageFolder> reverseFolders = new ArrayList<>();

        for(int i = picFolders.size()-1;i > reverseFolders.size()-1;i--){
            reverseFolders.add(picFolders.get(i));
        }*/

        return picFolders;
    }
    /**
     * Each time an item in the RecyclerView is clicked this method from the implementation of the transitListerner
     * in this activity is executed, this is possible because this class is passed as a parameter in the creation
     * of the RecyclerView's Adapter, see the adapter class to understand better what is happening here
     * @param pictureFolderPath a String corresponding to a folder path on the device external storage
     */
    @Override
    public void onPicClicked(String pictureFolderPath,String folderName) {
        Intent move = new Intent(MainActivity.this,ImageDisplay.class);
        move.putExtra("folderPath",pictureFolderPath);
        move.putExtra("folderName",folderName);

        //move.putExtra("recyclerItemSize",getCardsOptimalWidth(4));
        startActivity(move);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(MainActivity.this, "Permission is Required", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
