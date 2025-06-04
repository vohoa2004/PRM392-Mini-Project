package com.group6.miniproject;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


public class InstructionActivity extends AppCompatActivity {

    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Force landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instruction);

        btnStart = findViewById(R.id.btnStart);

        //Set OnClickListener for the button
        btnStart.setOnClickListener(v -> {
            //Start the BettingActivity instead of MainActivity
            startActivity(new Intent(InstructionActivity.this, BettingActivity.class));
        });
    }

}
