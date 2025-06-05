package com.group6.miniproject;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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
        
        // Initialize audio player
        initializeMediaPlayer();

        btnStart = findViewById(R.id.btnStart);

        //Set OnClickListener for the button
        btnStart.setOnClickListener(v -> {
            //Start the BettingActivity instead of MainActivity
            startActivity(new Intent(InstructionActivity.this, BettingActivity.class));
        });
    }
    
    /**
     * Initializes the background music
     */
    private void initializeMediaPlayer() {
        // Use the AudioManager singleton to play background music
        // We don't force restart to ensure continuity from previous activity
        AudioManager.getInstance().playMusic(this, AudioManager.BACKGROUND_MUSIC, true, false);
    }
    
    /**
     * Pauses the background music when activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        AudioManager.getInstance().pauseMusic();
    }
    
    /**
     * Resumes the background music when activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        AudioManager.getInstance().resumeMusic();
    }
    
    // No need for onDestroy handling as the AudioManager is now shared
}
