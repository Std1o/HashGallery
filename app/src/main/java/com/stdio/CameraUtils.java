package com.stdio;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;

import androidx.core.content.FileProvider;

import com.stdio.hashgallery.BuildConfig;
import com.stdio.hashgallery.MainActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraUtils {
    private static final String DEVICE_CAMERA_FOLDER_NAME = "Camera";
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    public static final int PHOTO_TAKEN_RESULT = 100;
    private static final String TAG = "CameraUtils";
    private static final String VK_CAMERA_FOLDER_NAME = "VK Camera";
    private static boolean hasCamera;
    private static boolean hasCameraInitialized = false;
    private static File mCurrentFile;
    private static String mCurrentPath;
    private static String mCurrentTitle;
    private static File mStorageDir;

    public static void addImageToGallery() {
        if (mCurrentPath != null && mCurrentTitle != null) {
            try {
                if (!isExternalStorageMounted(false)) {
                } else if (!isMediaScannerScanning()) {
                    ContentValues contentValues = new ContentValues();
                    ContentResolver contentResolver = MainActivity.context.getContentResolver();
                    long currentTimeMillis = System.currentTimeMillis();
                    contentValues.put("_data", mCurrentPath);
                    contentValues.put("title", mCurrentTitle);
                    contentValues.put("_display_name", mCurrentTitle);
                    contentValues.put("datetaken", Long.valueOf(currentTimeMillis));
                    contentValues.put("date_added", Long.valueOf(currentTimeMillis));
                    contentValues.put("date_modified", Long.valueOf(currentTimeMillis));
                    contentValues.put("mime_type", "image/jpeg");
                }
            } catch (Throwable th) {
            }
            closeOutput();
        }
    }

    private static File closeOutput() {
        mCurrentPath = null;
        return null;
    }

    public static void deleteImage() {
        if (mCurrentPath != null && mCurrentFile != null) {
            try {
                if (mCurrentFile.length() < 50) {
                    mCurrentFile.delete();
                }
            } catch (Throwable th) {
            }
        }
    }

    public static String getCameraDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + DEVICE_CAMERA_FOLDER_NAME;
    }

    public static File getCurrentPhotoFile() {
        return mCurrentFile;
    }

    public static String getCurrentPhotoPath() {
        return mCurrentPath;
    }

    public static boolean getDeviceHasCamera() {
        if (hasCameraInitialized) {
            return hasCamera;
        }
        PackageManager packageManager = MainActivity.context.getPackageManager();
        hasCameraInitialized = true;
        hasCamera = packageManager.hasSystemFeature("android.hardware.camera");
        return hasCamera;
    }

    private static File getOutput() throws IOException {
        File file = new File(getStorageDir() + "/" + JPEG_FILE_PREFIX + getTimestamp() + JPEG_FILE_SUFFIX);
        file.createNewFile();
        return file;
    }

    private static File getStorageDir() {
        if (mStorageDir == null) {
            mStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            if (mStorageDir != null) {
                mStorageDir = new File(mStorageDir.getAbsolutePath() + "/" + DEVICE_CAMERA_FOLDER_NAME);
            }
            if (mStorageDir != null && !mStorageDir.mkdirs() && !mStorageDir.exists()) {
                mStorageDir = null;
            }
        }
        return mStorageDir;
    }

    private static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    public static String getVKCameraDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + VK_CAMERA_FOLDER_NAME;
    }

    public static boolean isExternalStorageMounted(boolean z) {
        String externalStorageState = Environment.getExternalStorageState();
        return "mounted".equals(externalStorageState) || (z && "mounted_ro".equals(externalStorageState));
    }

    public static boolean isMediaScannerScanning() {
        Cursor query = MainActivity.context.getContentResolver().query(MediaStore.getMediaScannerUri(), new String[]{"volume"}, null, null, null);
        if (query == null) {
            return false;
        }
        boolean z = (query.getColumnCount() != 1 || !query.moveToFirst()) ? false : "external".equals(query.getString(0));
        query.close();
        return z;
    }

    public static void launchCamera(Activity activity) {
        if (getDeviceHasCamera() && "mounted".equals(Environment.getExternalStorageState()) && getStorageDir() != null) {
            activity.startActivityForResult(prepareIntent(), 100);
        }
    }

    public static void launchCamera(Fragment fragment) {
        if (getDeviceHasCamera() && "mounted".equals(Environment.getExternalStorageState()) && getStorageDir() != null) {
            fragment.startActivityForResult(prepareIntent(), 100);
        }
    }

    private static Intent prepareIntent() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        try {
            mCurrentFile = prepareOutput();
            MainActivity.uri = FileProvider.getUriForFile(MainActivity.context, BuildConfig.APPLICATION_ID + ".provider",mCurrentFile);
            intent.putExtra("output", MainActivity.uri);
        } catch (Throwable th) {
            mCurrentFile = closeOutput();
        }
        if (mCurrentFile == null || mCurrentPath == null) {
            return null;
        }
        return intent;
    }

    private static File prepareOutput() throws IOException {
        File output = getOutput();
        mCurrentPath = output.getAbsolutePath();
        mCurrentTitle = output.getName();
        return output;
    }
}
