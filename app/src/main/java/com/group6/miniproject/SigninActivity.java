package com.group6.miniproject;

import android.content.Intent;
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

public class SigninActivity extends AppCompatActivity {
    private TextInputEditText etUsername, etPassword;
    private Button btnSignIn;
    private static ArrayList<User> accounts = new ArrayList<>();

    static {
        accounts.add(new User("player1", "player123"));
        accounts.add(new User("player2", "player123"));
        accounts.add(new User("1", "1"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Force landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);
        
        // Initialize audio player
        initializeMediaPlayer();

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Set click listeners
        btnSignIn.setOnClickListener(v -> handleSignIn());
        findViewById(R.id.tvSignUp).setOnClickListener(v -> {
            startActivity(new Intent(SigninActivity.this, SignupActivity.class));
        });
    }

    private void handleSignIn() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        for (User account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                moveToInstructionActivity();
                return;
            }
        }

        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
    }

    public static ArrayList<User> getAccounts() {
        return accounts;
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
    
    /**
     * Navigate to the Instruction activity
     */
    private void moveToInstructionActivity() {
        Intent intent = new Intent(SigninActivity.this, InstructionActivity.class);
        startActivity(intent);
    }
}