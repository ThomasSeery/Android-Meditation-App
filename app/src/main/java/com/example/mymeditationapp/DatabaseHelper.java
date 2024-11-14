package com.example.mymeditationapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Creates the database
    public DatabaseHelper(@Nullable Context context) {
        super(context, "meditation.db", null, 1);
    }

    //Called first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE MeditationSession(" +
                "sessionId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "type VARCHAR(15)," +
                "dueDate DATE," +
                "duration INTEGER," +
                "status VARCHAR(15))";
        db.execSQL(sql);
    }

    //Called every other time to update the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }




}
