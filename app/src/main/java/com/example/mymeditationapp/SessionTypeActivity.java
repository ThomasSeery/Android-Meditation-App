package com.example.mymeditationapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SessionTypeActivity extends FooterActivity {

    private Button btnMindfulness, btnBreathing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_type);

        btnMindfulness = findViewById(R.id.btnMindfulness);
        btnBreathing = findViewById(R.id.btnBreathing);

        //Go to different intent based on what button is pressed
        btnMindfulness.setOnClickListener(v -> {
            Intent intent = new Intent(SessionTypeActivity.this,MindfulnessFormActivity.class);
            startActivity(intent);
        });
        btnBreathing.setOnClickListener(v -> {
            Intent intent = new Intent(SessionTypeActivity.this, BreathingFormActivity.class);
            startActivity(intent);
        });
    }
}