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
        
        // Initialize audio player
        initializeMediaPlayer();

        btnStart = findViewById(R.id.btnStart);

        // Set OnClickListener for the button
        btnStart.setOnClickListener(v -> {
            // Start the BettingActivity instead of MainActivity
            Intent intent = new Intent(InstructionActivity.this, BettingActivity.class);
            startActivity(intent);
        });
    }
    
    /**
     * Initializes the background music
     */
    private void initializeMediaPlayer() {
        if (!AudioManager.getInstance().isPlayingMusic(AudioManager.BACKGROUND_MUSIC)) {
            AudioManager.getInstance().playMusic(this, AudioManager.BACKGROUND_MUSIC, true, true);
        }
    }
    
    /**
     * Pauses the background music when activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Chỉ tạm dừng nhạc khi ứng dụng thực sự bị đóng hoặc chuyển sang nền
        if (isFinishing()) {
            AudioManager.getInstance().pauseMusic();
        }
    }
    
    /**
     * Resumes the background music when activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        AudioManager.getInstance().resumeMusic();
    }
}
