package com.example.mymeditationapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.ShareActionProvider;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends FooterActivity {

    private Button btnViewCalendar;
    private Button btnStart;
    private TextView tvStreak;
    private ShareActionProvider shareActionProvider;

    private static final String TAG = "MainActivity";
    private int streak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate called");

        createNotification();

        btnViewCalendar = findViewById(R.id.btnViewCalendar);
        btnStart = findViewById(R.id.btnStart);
        tvStreak = findViewById(R.id.tvStreak);

        btnViewCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewCalendarActivity.class);
            startActivity(intent);
        });

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SessionTypeActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG,"onResume() run");

        Intent updateServiceIntent = new Intent(this, UpdateService.class);
        startService(updateServiceIntent);

        MeditationContentProvider provider = new MeditationContentProvider();
        streak = provider.calculateStreak(MeditationContract.MeditationSessionTable.CONTENT_URI_CUSTOM);

        tvStreak.setText("Your current meditation streak is " + streak);
    }

    private void createNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("reminder", "daily"); //make notification a daily one
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, alarmIntent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12); //set to go off at 12:00 each day
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareIntent();

        return super.onCreateOptionsMenu(menu);
    }

    private void setShareIntent() {
        if (shareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "I just achieved a streak of "+streak+" in Tommy's Meditation App!");

            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
