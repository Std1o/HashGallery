package com.stdio.hashgallery;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBTags extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "tagsDB";
    public static final String TABLE_TAGS = "tags";

    public static final String KEY_ID = "_id";
    public static final String KEY_URI = "uri";
    public static final String KEY_PATH = "path";
    public static final String KEY_TAGS = "tags";

    public DBTags(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_TAGS + "(" + KEY_ID
                + " integer primary key,"  + KEY_URI + " text," + KEY_PATH + " text," +  KEY_TAGS + " text" + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_TAGS);

        onCreate(db);

    }
}
