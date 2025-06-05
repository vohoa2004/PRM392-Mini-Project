package com.group6.miniproject;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.group6.miniproject.models.User;

import java.util.ArrayList;

public class SignupActivity extends AppCompatActivity {
    private TextInputEditText etUsername, etPassword, etConfirmPassword;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Force landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        
        // Initialize audio player
        initializeMediaPlayer();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        // Set click listeners
        btnSignUp.setOnClickListener(v -> handleSignUp());
        findViewById(R.id.tvSignIn).setOnClickListener(v -> {
            finish();
        });
    }

    private void handleSignUp() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<User> accounts = SigninActivity.getAccounts();
        for (User account : accounts) {
            if (account.getUsername().equals(username)) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        accounts.add(new User(username, password));
        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
        
        finish();
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