package com.example.mymeditationapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FooterActivity extends AppCompatActivity {

    protected Button btnAbout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.footer);

        btnAbout = findViewById(R.id.btnAbout);
    }

    public void onAboutButtonClick(View view) {
        Intent intent = new Intent(FooterActivity.this,WebViewActivity.class);
        startActivity(intent);
    }

}
