package com.group6.miniproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class SigninActivity extends AppCompatActivity {
    private TextInputEditText etUsername, etPassword;
    private Button btnSignIn;
    private static ArrayList<User> accounts = new ArrayList<>();

    static {
        accounts.add(new User("player1", "player123"));
        accounts.add(new User("player2", "player123"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

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
                startActivity(new Intent(SigninActivity.this, MainActivity.class));
                finish();
                return;
            }
        }

        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
    }

    public static ArrayList<User> getAccounts() {
        return accounts;
    }
}