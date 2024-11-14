
package com.example.mymeditationapp;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MeditationContentProvider extends ContentProvider {

    public static final String TAG = "MeditationContentProvider";
    private static final int SESSION_LIST = 100;
    private static final int SESSION_ID = 101;
    private static final int SESSION_LIST_CUSTOM = 102;
    private static final int SESSION_ID_CUSTOM = 103;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static DatabaseHelper dbHelper;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(MeditationContract.CONTENT_AUTHORITY,MeditationContract.PATH_SESSION,SESSION_LIST);
        matcher.addURI(MeditationContract.CONTENT_AUTHORITY,MeditationContract.PATH_SESSION + "/#", SESSION_ID);
        matcher.addURI(MeditationContract.CONTENT_AUTHORITY,MeditationContract.PATH_CUSTOM,SESSION_LIST_CUSTOM);
        matcher.addURI(MeditationContract.CONTENT_AUTHORITY,MeditationContract.PATH_CUSTOM + "/#", SESSION_ID_CUSTOM);
        return matcher;
    }

    public MeditationContentProvider() {}


    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (uriMatcher.match(uri)) {
            case SESSION_LIST:
                Log.d(TAG, "testing" + uri);
                cursor = db.query(
                        MeditationContract.MeditationSessionTable.TABLE_NAME,
                        new String[]{MeditationContract.MeditationSessionTable.COLUMN_NAME_SESSION_ID, MeditationContract.MeditationSessionTable.COLUMN_NAME_SESSION_TYPE, MeditationContract.MeditationSessionTable.COLUMN_NAME_DUEDATE, MeditationContract.MeditationSessionTable.COLUMN_NAME_DURATION, MeditationContract.MeditationSessionTable.COLUMN_NAME_STATUS},
                        "DATE("+MeditationContract.MeditationSessionTable.COLUMN_NAME_DUEDATE + ") = ?",
                        selectionArgs,
                        null,
                        null,
                        "dueDate ASC"
                );
                Log.d(TAG, "Table name: " + MeditationContract.MeditationSessionTable.TABLE_NAME);
                Log.d(TAG, "Projection: " + Arrays.toString(new String[]{MeditationContract.MeditationSessionTable.COLUMN_NAME_SESSION_ID, MeditationContract.MeditationSessionTable.COLUMN_NAME_SESSION_TYPE, MeditationContract.MeditationSessionTable.COLUMN_NAME_DUEDATE, MeditationContract.MeditationSessionTable.COLUMN_NAME_DURATION, MeditationContract.MeditationSessionTable.COLUMN_NAME_STATUS}));
                Log.d(TAG, "Selection: " + MeditationContract.MeditationSessionTable.COLUMN_NAME_DUEDATE + " = ?");
                Log.d(TAG, "Selection args: " + Arrays.toString(selectionArgs));

                return cursor;
            case SESSION_LIST_CUSTOM:
                try {
                    cursor = db.query(
                            MeditationContract.MeditationSessionTable.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                    return cursor;
                } catch (Exception e) {
                    Log.d(TAG, "Error querying database: " + e.getMessage());
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        return null;
    }




    public int calculateStreak(Uri uri) {
        ArrayList<MeditationSession> sessions = new ArrayList<>();

        String[] projection = {MeditationContract.MeditationSessionTable.COLUMN_NAME_DUEDATE, MeditationContract.MeditationSessionTable.COLUMN_NAME_STATUS};
        String sortOrder = "dueDate DESC";


        Cursor cursor = query(uri, projection, null, null, sortOrder);

        // Iterate over the cursor and create MeditationSession objects
        if (cursor.moveToFirst()) {
            do {
                // Extract the data from the cursor
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_DUEDATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_STATUS));

                // Create a new MeditationSession object and add it to the list
                MeditationSession session = new MeditationSession(MeditationSession.dueDateToDate(dueDate), status);
                sessions.add(session);
            } while (cursor.moveToNext());
        }

        boolean addedDay = false; //checks if a "complete" session has been added on that day
        int streak = 0;
        if (sessions.size() > 1) {
            Date prevDate = sessions.get(0).getDueDate();
            if (sessions.get(0).getStatus().equals("incomplete")) {
                Log.d(TAG, "Streak: " + streak + " (IF Statement: sessions.get(0).getStatus().equals(\"incomplete\"))");
                return 0;
            }
            if (sessions.get(0).getStatus().equals("complete")) {
                addedDay=true;
                streak++;
            }
            for (int i = 1; i < sessions.size(); i++) {
                Date currentDate = sessions.get(i).getDueDate();
                boolean isSameDay = currentDate.getDate() == prevDate.getDate()
                        && currentDate.getMonth() == prevDate.getMonth()
                        && currentDate.getYear() == prevDate.getYear();
                if (!isSameDay) { //If it's not the same date
                    addedDay = false;
                    Log.d(TAG, "Streak: " + streak + " (IF Statement: !isSameDay, Index: " + i + ")");
                }
                String status = sessions.get(i).getStatus();
                if (status.equals("incomplete")) { //If Session is incomplete, then stop there
                    Log.d(TAG, "Streak: " + streak + " (IF Statement: status.equals(\"incomplete\"), Index: " + i + ")");
                    return streak;
                }
                if (status.equals("complete")) {
                    if (!isSameDay) { //If its not the same day as before
                        if (TimeUnit.DAYS.convert(Math.abs(prevDate.getTime() - currentDate.getTime()), TimeUnit.MILLISECONDS) <= 7) {
                            Log.d(TAG, "Streak: " + streak + " (IF <=7 Days>), Index: " + i + ")");
                            addedDay=true; //Youve added a completion session for that day, so dont include anymore until its a day after this
                            streak++;
                        } else {
                            Log.d(TAG, "Streak: " + streak + " (IF Statement: !isSameDay, Index: " + i + ")");
                            return streak;
                        }
                    } else {
                        if (!addedDay) {
                            streak++;
                            addedDay = true; //Youve added a completion session for that day, so dont include anymore until its a day after this
                            Log.d(TAG, "Streak: " + streak + " (IF Statement: addedDay, Index: " + i + ")");
                        }
                    }
                }
                prevDate = sessions.get(i).getDueDate();
            }

            Log.d(TAG, "Streak: " + streak + " (IF Statement: End of loop)");
            return (TimeUnit.DAYS.convert(Math.abs(new Date().getTime() - prevDate.getTime()), TimeUnit.MILLISECONDS) < 7) ? streak : 0; // Streak is only valid if the previous session was less than 7 days ago
        }
        Log.d(TAG, "Streak: " + streak + " (IF Statement: sessions.size() <= 1)");
        return 0;


    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SESSION_LIST:
                return MeditationContract.MeditationSessionTable.CONTENT_TYPE_DIR;
            case SESSION_ID:
                return MeditationContract.MeditationSessionTable.CONTENT_TYPE_ITEM;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int matchCode = uriMatcher.match(uri);
        Uri retUri = null;
        long _id;
        switch(matchCode){
            case SESSION_LIST:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                _id = db.insert(MeditationContract.MeditationSessionTable.TABLE_NAME, null, values);
                if (_id > 0) {
                    retUri = MeditationContract.MeditationSessionTable.buildMeditationSessionUriWithId(_id);
                }
                else {
                    throw new SQLException("failed to insert");
                }

                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        Log.i(TAG, "insert success");
        return retUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rows = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case SESSION_LIST:
                rows = db.delete(MeditationContract.MeditationSessionTable.TABLE_NAME, MeditationContract.MeditationSessionTable.COLUMN_NAME_SESSION_ID + "=?", selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rows;
    }


    @Override
    public int update(@NonNull Uri uri, @NonNull ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows;
        switch (uriMatcher.match(uri)) {
            case SESSION_LIST:
                Log.d(TAG,"updating "+uri);
                Date currentDate = new Date(new Date().getTime() - 10 * 60 * 1000);
                values.put("status", "incomplete");
                rows = db.update(MeditationContract.MeditationSessionTable.TABLE_NAME, values, "status = 'awaiting' AND dueDate < ?", new String[]{new SimpleDateFormat("yyyy-MM-dd HH:mm").format(currentDate)});
                break;
            case SESSION_ID:
                rows = db.update("MeditationSession", values, "sessionId = ?", selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

}

