package com.example.mymeditationapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MindfulnessFormActivity extends FooterActivity {

    private Button btnSubmit;
    private EditText edtDuration;
    private Spinner spnSounds;
    private LinearLayout llDuration;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mindfulness_form);

        SharedPreferences sp = getSharedPreferences("MeditationPreferences", Context.MODE_PRIVATE);
        int index = sp.getInt("soundIndex", 0);

        Intent recievedIntent = getIntent();
        int id = recievedIntent.getIntExtra("id", -1);

        spnSounds = findViewById(R.id.spnSounds);

        ArrayList<String> sounds = new ArrayList<>();
        Field[] fields = R.raw.class.getFields();
        //Iterate through the file names and add them
        for (int i = 0; i < fields.length; i++) {
            String fileName = fields[i].getName();
            sounds.add(fileName);
        }

        //Add to an adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sounds);
        //Add adapter to spinner
        spnSounds.setAdapter(adapter);
        spnSounds.setSelection(index);

        llDuration = findViewById(R.id.llDuration);
        btnSubmit = findViewById(R.id.btnSubmit);
        edtDuration = findViewById(R.id.edtDuration);

        //If duration is already set (booked session)
        int duration = recievedIntent.getIntExtra("duration", -1);
        if(duration!=-1){
            llDuration.setVisibility(View.GONE);
        }

        tvError = findViewById(R.id.tvError);

        btnSubmit.setOnClickListener(v -> {
            //Save the shared preference
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("soundIndex", spnSounds.getSelectedItemPosition());
            editor.apply();

            Intent intent = new Intent(MindfulnessFormActivity.this, MindfulnessSessionActivity.class);
            intent.putExtra("sound", spnSounds.getSelectedItem().toString());
            if((InputValidator.isValidDuration(edtDuration.getText().toString()) || duration>0)){ //if input is valid
                tvError.setVisibility(View.GONE);
                if(duration==-1){
                    intent.putExtra("duration", Integer.parseInt(edtDuration.getText().toString()));
                }else{
                    intent.putExtra("duration", duration);
                }
                intent.putExtra("id", id);
                startActivity(intent);
            }else{
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

}