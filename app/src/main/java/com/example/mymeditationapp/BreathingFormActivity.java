package com.example.mymeditationapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BreathingFormActivity extends FooterActivity {

    private Button btnSubmit;
    private EditText edtIn, edtOut, edtDuration;
    private LinearLayout llDuration;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_form);

        Intent recievedIntent = getIntent();

        //If Session is booked, these values won't be -1, otherwise, they will
        int id = recievedIntent.getIntExtra("id",-1);
        int duration = recievedIntent.getIntExtra("duration",-1);

        btnSubmit = findViewById(R.id.btnSubmit);
        edtIn = findViewById(R.id.edtIn);
        edtOut = findViewById(R.id.edtOut);
        edtDuration = findViewById(R.id.edtDuration);

        //If session is pre booked, don't include duration
        llDuration = findViewById(R.id.llDuration);
        if(duration!=-1){
            llDuration.setVisibility(View.GONE);
        }

        tvError = findViewById(R.id.tvError);

        btnSubmit.setOnClickListener(v -> {
            if((InputValidator.isPositiveInteger(edtIn.getText().toString()) && InputValidator.isPositiveInteger(edtOut.getText().toString()) && InputValidator.isValidDuration(edtDuration.getText().toString())
                    && InputValidator.inPlusOutLessThanOrEqualToDuration(edtIn.getText().toString(),edtOut.getText().toString(),edtDuration.getText().toString()))
                    || ((InputValidator.isPositiveInteger(edtIn.getText().toString()) && (InputValidator.isPositiveInteger(edtOut.getText().toString())) && duration>0 && (InputValidator.inPlusOutLessThanOrEqualToDuration(edtIn.getText().toString(),edtOut.getText().toString(),Integer.toString(duration)))))) { //if inputs are valid
                tvError.setVisibility(View.GONE); //remove error message
                Intent intent = new Intent(BreathingFormActivity.this,BreathingSessionActivity.class);
                intent.putExtra("in", Integer.parseInt(edtIn.getText().toString()));
                intent.putExtra("out", Integer.parseInt(edtOut.getText().toString()));
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