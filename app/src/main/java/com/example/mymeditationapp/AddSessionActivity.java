package com.example.mymeditationapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;

import com.google.android.material.snackbar.Snackbar;


public class AddSessionActivity extends FooterActivity {

    public static Context context;

    private Button btnSubmit;
    private Spinner spnSessionTypes;
    private EditText edtDuration;
    private TimePicker tpTime;
    private static final String TAG = "AddSessionActivity";
    private RelativeLayout parent;
    private TextView tvError;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_session);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("medi", "Meditation Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        parent = findViewById(R.id.rlAddSession);

        Intent intent = getIntent();
        Date date = (Date)intent.getSerializableExtra("date");

        spnSessionTypes = findViewById(R.id.spnSessionTypes);
        edtDuration = findViewById(R.id.edtDuration);
        btnSubmit = findViewById(R.id.btnSubmit);
        tpTime = findViewById(R.id.tpTime);

        tpTime.setIs24HourView(true);

        tvError = findViewById(R.id.tvError);

        btnSubmit.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, tpTime.getHour());
            calendar.set(Calendar.MINUTE, tpTime.getMinute());
            Date updatedDate = calendar.getTime();
            if(updatedDate.after(new Date(new Date().getTime() - (10 * 60 * 1000)))){ //if the date is after 10 minutes ago from now
                if(InputValidator.isValidDuration(edtDuration.getText().toString())){
                    tvError.setVisibility(View.GONE);
                    MeditationSession session = new MeditationSession((String)spnSessionTypes.getSelectedItem(),updatedDate,Integer.parseInt(edtDuration.getText().toString()),"awaiting");
                    ContentValues values = new ContentValues();
                    values.put("type", session.getType());
                    values.put("dueDate", session.getDueDateAndTimeToString());
                    values.put("duration", session.getDuration());
                    values.put("status", session.getStatus());


                    Uri uri = getContentResolver().insert(MeditationContract.MeditationSessionTable.CONTENT_URI,values); //database query to add the new session to database
                    long id = Long.parseLong(uri.getLastPathSegment());

                    if(id>0){
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE); //create the alarm
                        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
                        alarmIntent.putExtra("reminder","meditation");
                        alarmIntent.putExtra("dueDate",session.getDueDate());
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, session.getSessionId(), alarmIntent, PendingIntent.FLAG_IMMUTABLE); //set the notification for that time

                        long alarmTime = (updatedDate.getTime() - new Date().getTime());
                        Log.d("AlarmTime",updatedDate.toString());
                        Log.d("AlarmTime",new Date().toString());
                        Log.d("AlarmTime", "Alarm time set to: " + alarmTime);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        ViewMeditationSessionsActivity.addedToDb = true;
                        finish();

                    }else{
                        Log.d(TAG,"unsuccessfull");
                    }
                }else{
                    tvError.setVisibility(View.VISIBLE);
                }

            }else{
                Toast.makeText(AddSessionActivity.this, "Cant create a session behind current date", Toast.LENGTH_SHORT).show();
            }
        });


    }


}