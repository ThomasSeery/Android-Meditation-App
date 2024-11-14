package com.example.mymeditationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;

public class BreathingSessionActivity extends AppCompatActivity {

    private ImageView imgCircle;
    private ImageButton btnStop;
    private int minRadius = 0, maxRadius = 200;
    private TextView tvInOut;
    private boolean isFirstFrame = true;
    private CountDownTimer timer;
    private TextView tvTimer;
    private ValueAnimator repeatAnimator;
    private int id;
    private static final String TAG = "BreathingSessionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_session);

        Intent intent = getIntent();
        int in = intent.getIntExtra("in", 5) * 1000;
        int out = intent.getIntExtra("out", 5) * 1000;
        int duration = intent.getIntExtra("duration", 5);
        long durationMillis = duration * 60000L;
        id = intent.getIntExtra("id",-1);

        tvTimer = findViewById(R.id.tvTimer);
        tvInOut = findViewById(R.id.tvInOut);
        imgCircle = findViewById(R.id.imgCircle);
        btnStop = findViewById(R.id.btnStop);

        ValueAnimator inflateAnimator = ValueAnimator.ofInt(minRadius, maxRadius);
        //create inflation animation for each frame
        inflateAnimator.addUpdateListener(valueAnimator -> {
            if(isFirstFrame){
                tvInOut.setText("Breathe in");
                isFirstFrame = false;
            }
            int radius = (int) valueAnimator.getAnimatedValue();
            imgCircle.getLayoutParams().width = radius;
            imgCircle.getLayoutParams().height = radius;
            imgCircle.requestLayout();
        });
        inflateAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isFirstFrame = true; //reset the flag for the next animation
            }
        });
        inflateAnimator.setDuration(in); //how long the inflate animation lasts
        inflateAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); //sets the rate of change of the animation

        ValueAnimator deflateAnimator = ValueAnimator.ofInt(maxRadius, minRadius); //initializes the animation that goes from max radius to min radius i.e. "deflate"
        //create the deflation animation for each frame
        deflateAnimator.addUpdateListener(valueAnimator -> {
            if(isFirstFrame){
                tvInOut.setText("Breathe out");
                isFirstFrame = false;
            }
            int radius = (int) valueAnimator.getAnimatedValue();
            imgCircle.getLayoutParams().width = radius;
            imgCircle.getLayoutParams().height = radius;
            imgCircle.requestLayout();
        });
        deflateAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isFirstFrame = true; //reset the flag for the next animation
            }
        });
        deflateAnimator.setDuration(out);
        deflateAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(inflateAnimator, deflateAnimator); //creates an animator set to run the inflate animation, followed by the deflate animation right after

        //create a reapeat animator to repeat the animator set a set number of times
        repeatAnimator = ValueAnimator.ofFloat(0f, 1f); //creates a repeat animation from 0 to 100% (represented by float)
        repeatAnimator.setDuration(durationMillis);
        repeatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        repeatAnimator.addUpdateListener(valueAnimator -> {
            float fraction = valueAnimator.getAnimatedFraction();
            if (fraction == 1f) { //if animation has ran for required duration
                animatorSet.end(); //stop the animation
            } else {
                animatorSet.start(); //otherwise, keep continuing the sequence
            }
        });

        timer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) millisUntilFinished / 60000;
                int seconds = (int) (millisUntilFinished % 60000) / 1000;
                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                tvTimer.setText(timeLeftFormatted);
            }

            @Override
            public void onFinish() {
                animatorSet.end();
                addSession(duration);
                finish();finish();
            }
        };

        timer.start();
        repeatAnimator.start(); //start the animation

        btnStop.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    public void addSession(int duration){
        MeditationSession session = new MeditationSession("Breathing Exercise",new Date(),duration,"complete");
        ContentValues values = new ContentValues();
        if(id==-1) {
            values.put("type", session.getType());
            values.put("dueDate", session.getDueDateAndTimeToString());
            values.put("duration", session.getDuration());
            values.put("status", session.getStatus());

            getContentResolver().insert(MeditationContract.MeditationSessionTable.CONTENT_URI,values);
            Intent intent = new Intent(this, SessionTypeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else{
            values.put("status", "complete");
            int rows = getContentResolver().update(ContentUris.withAppendedId(MeditationContract.MeditationSessionTable.CONTENT_URI, session.getSessionId()), values, null, new String[]{String.valueOf(id)});
            Log.d(TAG,"rows"+rows);
            Intent intent = new Intent(this, ViewMeditationSessionsActivity.class);
            intent.putExtra("date", session.getDueDate());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        repeatAnimator.end();
        if(timer!=null){
            timer.cancel();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
