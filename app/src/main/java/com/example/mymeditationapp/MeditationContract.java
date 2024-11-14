
package com.example.mymeditationapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public final class MeditationContract {

    //Uri for ContentProvider
    public static final String CONTENT_AUTHORITY = "com.example.mymeditationapp.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_SESSION = "session";
    public static final String PATH_CUSTOM = "custom_session";


    public static final class MeditationSessionTable implements BaseColumns {
        //Uri
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SESSION).build();
        public static final Uri CONTENT_URI_CUSTOM = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CUSTOM).build();
        public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_SESSION;
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_SESSION;
        public static final String CONTENT_TYPE_DIR_CUSTOM = "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_CUSTOM;
        public static final String CONTENT_TYPE_ITEM_CUSTOM = "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_CUSTOM;
        //table and column names
        public static final String TABLE_NAME = "MeditationSession";
        public static final String COLUMN_NAME_SESSION_ID = "sessionId";
        public static final String COLUMN_NAME_SESSION_TYPE = "type";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_DUEDATE = "dueDate";
        public static final String COLUMN_NAME_STATUS = "status";
        public static Uri buildMeditationSessionUriWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }
}

