package com.example.mymeditationapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


public class ViewMeditationSessionsActivity extends FooterActivity {

    private RecyclerView rvSessions;
    private Button btnAdd;
    private Button btnRemove;
    private TextView tvSelect;
    private boolean selecting = false;
    private static final String TAG = "ViewMeditationSessionsActivity";
    private RelativeLayout parent;
    private TextView tvDay;
    public static boolean addedToDb = false;
    private SwipeRefreshLayout swipeRefreshRv;
    private ImageButton btnForward, btnBackward;
    private Date[] date;
    private String defaultText;
    private MeditationSession addedSession;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_meditation_sessions);

        parent = findViewById(R.id.rlViewMeditationSessions);

        Intent intent = getIntent();
        date = new Date[]{(Date)intent.getSerializableExtra("date")};

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.ENGLISH);

        Calendar calendar = Calendar.getInstance();

        tvDay = findViewById(R.id.tvDay);

        tvDay.setText(dateFormat.format(date[0]));

        Log.d(TAG,MeditationSession.dueDateToString(date[0]));

        loadRvData(date[0]);

        btnForward = findViewById(R.id.btnForward);
        btnBackward = findViewById(R.id.btnBackward);

        btnForward.setOnClickListener(v -> {
            calendar.setTime(date[0]);
            calendar.add(Calendar.DAY_OF_YEAR, 1); //increments the day by one
            date[0] = calendar.getTime(); //set as the new date
            tvDay.setText(dateFormat.format(date[0])); //update text
            loadRvData(date[0]); //reload the recycler view
        });

        btnBackward.setOnClickListener(v -> {
            calendar.setTime(date[0]);
            calendar.add(Calendar.DAY_OF_YEAR, -1); //decrements the day by one
            date[0] = calendar.getTime(); //set as the new date
            tvDay.setText(dateFormat.format(date[0])); //update text
            loadRvData(date[0]); //reload the recycler view
        });

        swipeRefreshRv = findViewById(R.id.swipeRefreshRv);
        swipeRefreshRv.setOnRefreshListener(() -> {
            loadRvData(date[0]);
            swipeRefreshRv.setRefreshing(false);
        });

        tvSelect = findViewById(R.id.tvSelect);
        btnRemove = findViewById(R.id.btnRemove);
        defaultText = btnRemove.getText().toString();
        btnRemove.setOnClickListener(v -> {
            if(selecting){
                btnRemove.setText(defaultText);
                tvSelect.setVisibility(View.GONE);
                selecting=false;
            }else{
                btnRemove.setText("Cancel");
                tvSelect.setVisibility(View.VISIBLE);
                selecting=true;
            }
        });

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Date currentDate = new Date();
            if (date[0].getYear() > currentDate.getYear() ||
                    (date[0].getYear() == currentDate.getYear() && date[0].getMonth() > currentDate.getMonth()) ||
                    (date[0].getYear() == currentDate.getYear() && date[0].getMonth() == currentDate.getMonth() && date[0].getDate() >= currentDate.getDate())) { //If the date is after the current date
                Intent intent1 = new Intent(ViewMeditationSessionsActivity.this, AddSessionActivity.class);
                intent1.putExtra("date", date[0]);
                startActivity(intent1);

            } else {
                Toast.makeText(ViewMeditationSessionsActivity.this, "Can't set a session before today", Toast.LENGTH_SHORT).show();
                Log.d("Selected Date", "Year: " + date[0].getYear() + ", Month: " + date[0].getMonth() + ", Day: " + date[0].getDate());
                Log.d("Current Date", "Year: " + currentDate.getYear() + ", Month: " + currentDate.getMonth() + ", Day: " + currentDate.getDate());
            }





        });


    }


    public void loadRvData(Date date){
        Intent updateServiceIntent = new Intent(ViewMeditationSessionsActivity.this, UpdateService.class); //updates sessions before it queries the sessions again
        startService(updateServiceIntent);

        //Re-queries the recycler view once the service has finished
        UpdateServiceCallback callback = () -> {
            List<MeditationSession> sessions = new ArrayList<>();

            Cursor cursor = getContentResolver().query(
                    MeditationContract.MeditationSessionTable.CONTENT_URI,
                    null,
                    null,
                    new String[]{MeditationSession.dueDateToString(date)},
                    null);

            if (cursor.moveToFirst()) {
                do {
                    int sessionId = cursor.getInt(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_SESSION_ID));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_SESSION_TYPE));
                    String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_DUEDATE));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_DURATION));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_STATUS));

                    sessions.add(new MeditationSession(sessionId, type, MeditationSession.dueDateToDate(dueDate), duration, status)); //Add session to list
                } while (cursor.moveToNext());
                cursor.close();
            }else{
                Log.d(TAG,"No data to load");
            }

            rvSessions = findViewById(R.id.rvSessions);
            rvSessions.setLayoutManager(new LinearLayoutManager(this));
            SessionAdapter adapter = new SessionAdapter(sessions);
            rvSessions.setAdapter(adapter);
        };

        UpdateService.setCallback(callback);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        btnRemove.setText(defaultText);
        tvSelect.setVisibility(View.GONE);
        selecting=false;

        List<MeditationSession> sessions = new ArrayList<>();

        Cursor cursor = getContentResolver().query(
                MeditationContract.MeditationSessionTable.CONTENT_URI,
                null,
                null,
                new String[]{MeditationSession.dueDateToString(date[0])},
                null);

        if (!cursor.moveToFirst()) {
            return;
        }
        do {
            int sessionId = cursor.getInt(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_SESSION_ID));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_SESSION_TYPE));
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_DUEDATE));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_DURATION));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(MeditationContract.MeditationSessionTable.COLUMN_NAME_STATUS));


            Log.d(TAG, sessionId + "");

            sessions.add(new MeditationSession(sessionId, type, MeditationSession.dueDateToDate(dueDate), duration, status));
        } while (cursor.moveToNext());
        cursor.close();

        if(addedToDb){


            addedSession = sessions.get(sessions.size()-1);

            Snackbar.make(parent, "Added to Database", Snackbar.LENGTH_LONG)
                    .setAction("Add to Calendar", v -> {
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                            //request the permission
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, 2);
                        } else {
                            addToCalendar();
                        }
                    })
                    .show();

            SessionAdapter adapter = new SessionAdapter(sessions);
            rvSessions.setAdapter(adapter);
            addedToDb=false;
        }

    }

    public void addToCalendar(){
        //create an intent to open the calendar app
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT);
        calendarIntent.setType("vnd.android.cursor.item/event");
        //set the event details
        calendarIntent.putExtra(CalendarContract.Events.TITLE, "Meditation Session: " + addedSession.getType());
        calendarIntent.putExtra(CalendarContract.Events.DESCRIPTION,  "A meditation session scheduled using the Meditation App");
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, addedSession.getDueDate().getTime());
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, (addedSession.getDueDate().getTime() + (addedSession.getDuration()*60000)));

        Log.d(TAG,"start "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((addedSession.getDueDate().getTime()))));
        Log.d(TAG,"start "+addedSession.getDueDate().getTime());
        Log.d(TAG,"dur"+addedSession.getDuration());
        Log.d(TAG, "end" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((addedSession.getDueDate().getTime() + (addedSession.getDuration()*60000)))));
        Log.d(TAG, "end" + addedSession.getDueDate().getTime() + (addedSession.getDuration()*60000));
        Log.d(TAG, "end" + addedSession.getDuration()*60000);

        //Launch the calendar app with values
        startActivity(calendarIntent);
    }

    //If the permission is accepted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission granted
                addToCalendar();
            }
        }
    }


    public boolean isSelecting(){
        return selecting;
    }
}