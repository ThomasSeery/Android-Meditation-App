package com.example.mymeditationapp;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class UpdateService extends Service {
    private static final String TAG = "UpdateService";
    private static UpdateServiceCallback callback;

    public static void setCallback(UpdateServiceCallback callback) {
        UpdateService.callback = callback;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Call the update query here
        updateSession();
        //Stop the service after the query is complete
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateSession() {
        //Update the statuses of the sessions with db query
        int rowsUpdated = getContentResolver().update(MeditationContract.MeditationSessionTable.CONTENT_URI, new ContentValues(), null, null);

        if (rowsUpdated > 0) {
            Log.d(TAG, "Session updated successfully");
        } else {
            Log.d(TAG, "No updates needed");
        }
        if(callback != null){
            callback.onUpdateServiceFinished();
        }
    }
}

