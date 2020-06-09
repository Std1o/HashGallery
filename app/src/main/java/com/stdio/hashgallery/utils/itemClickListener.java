package com.stdio.hashgallery.utils;

import com.stdio.hashgallery.models.ImageModel;

import java.util.ArrayList;

/**
 * Author CodeBoy722
 */
public interface itemClickListener {

    /**
     * Called when a picture is clicked
     * @param holder The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     */
    void onPicClicked(PicHolder holder, int position, ArrayList<ImageModel> pics);
    void onPicClicked(String pictureFolderPath, String folderName);
}
