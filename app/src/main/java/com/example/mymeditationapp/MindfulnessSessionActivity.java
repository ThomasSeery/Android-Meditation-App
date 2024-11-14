package com.example.mymeditationapp;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.Locale;

public class MindfulnessSessionActivity extends AppCompatActivity {

    private ImageButton btnPlay;
    private ImageButton btnStop;
    private ProgressBar pbTimer;
    private TextView tvTimer;
    private boolean timerRunning = true;
    private long timeLeft;

    private CountDownTimer cdtTimer;
    private MediaPlayer mp;
    private long duration;
    private long durationMillis;
    private int id;

    private boolean isBound = false;

    private final String TAG = "MindfulnessSessionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mindfulness_session);

        Intent recievedIntent = getIntent();
        //Get values
        duration = recievedIntent.getIntExtra("duration", 0);
        id = recievedIntent.getIntExtra("id", -1);
        durationMillis=duration*60000;
        Log.d("duration", "dur" +duration);
        String sound = recievedIntent.getStringExtra("sound");

        int resId = getResources().getIdentifier(sound, "raw", getPackageName());
        mp = MediaPlayer.create(MindfulnessSessionActivity.this, resId); //Get the name of the sound by the resourceId
        mp.setLooping(true); //Make it loop if it needs to

        btnPlay = findViewById(R.id.btnPlayPause);
        btnStop = findViewById(R.id.btnStop);
        pbTimer = findViewById(R.id.pbTimer);
        tvTimer = findViewById(R.id.tvTimer);

        pbTimer.setMax(100); //Sets max duration to 100%

        timeLeft = durationMillis;
        play();

        btnPlay.setOnClickListener(v -> {
            if (!timerRunning) {
                play();
            } else {
                pause();
            }
        });

        btnStop.setOnClickListener(view -> {
            onBackPressed();
        });

    }

    public void play() {
        Log.d(TAG, "play started");
        timerRunning = true;
        mp.start(); //Start the sound
        btnPlay.setImageResource(R.drawable.ic_pause);
        Log.d(TAG, "play "+timeLeft);
        cdtTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { //Runs at each interval
                timeLeft = millisUntilFinished; //Get timeLeft
                Log.d(TAG, "timeleft "+millisUntilFinished);
                updateText(); //Update the text using timeLeft
                pbTimer.setProgress((int) (((durationMillis - timeLeft) * 100) / durationMillis)); //Update the progress of the progress bar
                Log.d("progress", (int) (((durationMillis - timeLeft) * 100) / durationMillis)+"");
            }

            @Override
            public void onFinish() {
                sessionFinished();
            }
        };
        cdtTimer.start();
        Log.d(TAG, "play completed");
    }

    public void pause() {
        Log.d(TAG, "pause started");
        if (cdtTimer != null) {
            cdtTimer.cancel();
        }
        timerRunning = false;
        mp.pause();
        btnPlay.setImageResource(R.drawable.ic_play);
        Log.d(TAG, "pause completed");
    }

    public void updateText() {
        int minutes = (int) timeLeft / 60000;
        int seconds = (int) (timeLeft % 60000) / 1000;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(timeLeftFormatted);
        Log.d(TAG, "text updated to "+timeLeftFormatted);
    }

    public void sessionFinished(){
        //update database
        MeditationSession session = new MeditationSession(id,"mindfulness",new Date(),(int)duration,"complete");
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        boolean result;
        ContentValues values = new ContentValues();
        if(id==-1){ //If this was an instantaneous session
            values.put("type", session.getType());
            values.put("dueDate", session.getDueDateAndTimeToString());
            values.put("duration", session.getDuration());
            values.put("status", session.getStatus());
            Uri rows = getContentResolver().insert(MeditationContract.MeditationSessionTable.CONTENT_URI,values); //Insert the new session in the database
            Intent intent = new Intent(this, SessionTypeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }else{ //If this was a booked session
            values.put("status", "complete"); //change it to complete
            int rows = getContentResolver().update(ContentUris.withAppendedId(MeditationContract.MeditationSessionTable.CONTENT_URI, session.getSessionId()), values, null, new String[]{String.valueOf(session.getSessionId())});
            Intent intent = new Intent(this, ViewMeditationSessionsActivity.class);
            intent.putExtra("date", session.getDueDate());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
        mp.stop();
        pbTimer.setProgress(0);

        //back to prev activity
        Toast.makeText(this, "Meditation session complete!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if (cdtTimer != null) {
            cdtTimer.cancel();
        }
        timerRunning = false;
        mp.stop();
        mp.seekTo(0);
        timeLeft = durationMillis;
        pbTimer.setProgress(0);
        updateText();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
