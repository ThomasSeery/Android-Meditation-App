package com.example.mymeditationapp;

import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Date;
import android.Manifest;

public class ViewCalendarActivity extends FooterActivity {

    private CalendarView cvCalendar;
    private ImageButton btnCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_calendar);

        cvCalendar = findViewById(R.id.cvCalendar);

        cvCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> { //If click on a new day
            Toast.makeText(ViewCalendarActivity.this, "Day: "+dayOfMonth, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ViewCalendarActivity.this,ViewMeditationSessionsActivity.class); //Go to View Meditation Sessions event
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            Date date = calendar.getTime();
            intent.putExtra("date", date); //pass the date value into the intent
            startActivity(intent);
        });

        btnCalendar = findViewById(R.id.btnCalendar);
        btnCalendar.setOnClickListener(view -> {
            //Opens up the calendar app
            long startMillis = System.currentTimeMillis();
            Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
            builder.appendPath("time");
            ContentUris.appendId(builder, startMillis);
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
            startActivity(intent);
        });

    }
}