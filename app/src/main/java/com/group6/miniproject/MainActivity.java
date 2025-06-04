package com.group6.miniproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private CheckBox checkBox1, checkBox2, checkBox3;
    private SeekBar seekBar1, seekBar2, seekBar3;
    private ImageView lineImageView;
    private TextView tvBetInfo;
    private ImageButton btnPlayPause;
    private ImageButton btnSettings;
    
    private boolean[] selectedAnimals;
    private int betAmount = 0;
    private int currentPoints = 0;
    private List<String> selectedAnimalNames = new ArrayList<>();
    private List<Integer> selectedAnimalIndices = new ArrayList<>();
    private Random random = new Random();
    
    // Race control variables
    private boolean isRaceRunning = false;
    private Handler handler = new Handler();
    private Runnable raceRunnable;
    
    // Map animal indices to drawable resources
    private int[] animalDrawables = {
        R.drawable.bee,      // 0: Bee
        R.drawable.puppy,    // 1: Puppy
        R.drawable.dolphin,  // 2: Dolphin
        R.drawable.frog,     // 3: Frog
        R.drawable.seal,     // 4: Seal
        R.drawable.snail,    // 5: Snail
        R.drawable.turtle,   // 6: Turtle
        R.drawable.mouse,    // 7: Mouse
        R.drawable.squirl    // 8: Squirrel
    };
    
    // Map animal indices to names
    private String[] animalNames = {
        "Ong",      // 0: Bee
        "Chó con",  // 1: Puppy
        "Cá heo",   // 2: Dolphin
        "Ếch",      // 3: Frog
        "Hải cẩu",  // 4: Seal
        "Ốc sên",   // 5: Snail
        "Rùa",      // 6: Turtle
        "Chuột",    // 7: Mouse
        "Sóc"       // 8: Squirrel
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Force landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_round);
        
        // Get betting information from intent
        if (getIntent().hasExtra("selectedAnimals")) {
            selectedAnimals = getIntent().getBooleanArrayExtra("selectedAnimals");
            betAmount = getIntent().getIntExtra("betAmount", 0);
            currentPoints = getIntent().getIntExtra("currentPoints", 0);
        }
        
        // Initialize checkboxes
        checkBox1 = findViewById(R.id.checkBox);
        checkBox2 = findViewById(R.id.checkBox2);
        checkBox3 = findViewById(R.id.checkBox3);
        
        // Initialize seekbars
        seekBar1 = findViewById(R.id.seekBar);
        seekBar2 = findViewById(R.id.seekBar2);
        seekBar3 = findViewById(R.id.seekBar3);
        
        // Initialize line image
        lineImageView = findViewById(R.id.imageView2);
        
        // Initialize bet info text view
        tvBetInfo = findViewById(R.id.tvBetInfo);
        
        // Initialize play/pause button
        btnPlayPause = findViewById(R.id.btnPlayPause);
        
        // Initialize settings button
        btnSettings = findViewById(R.id.btnSettings);
        
        // Get selected animal names and indices
        getSelectedAnimalInfo();
        
        // Remove text from checkboxes
        checkBox1.setText("");
        checkBox2.setText("");
        checkBox3.setText("");
        
        // Preserve original thumb sizes and positions
        preserveThumbAppearance(seekBar1);
        preserveThumbAppearance(seekBar2);
        preserveThumbAppearance(seekBar3);
        
        // Set up race animals and UI
        setupRaceAnimals();
        
        // Display bet information
        updateBetInfo();
        
        // Set up play/pause button
        setupPlayPauseButton();
        
        // Set up settings button
        setupSettingsButton();
    }
    
    private void setupSettingsButton() {
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsDialog();
            }
        });
    }
    
    private void showSettingsDialog() {
        // Pause the race if it's running
        if (isRaceRunning) {
            pauseRace();
        }
        
        // Create dialog with options
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cài đặt");
        
        // Add options
        String[] options = {"Âm lượng", "Hủy đặt cược và quay lại"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Volume settings
                        showVolumeSettingsDialog();
                        break;
                    case 1: // Cancel bet and return to betting screen
                        confirmCancelBet();
                        break;
                }
            }
        });
        
        // Add cancel button
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        // Show the dialog
        builder.show();
    }
    
    private void showVolumeSettingsDialog() {
        // This will be implemented in the future
        // For now, just show a message
        Toast.makeText(this, "Tính năng âm lượng sẽ được phát triển sau", Toast.LENGTH_SHORT).show();
    }
    
    private void confirmCancelBet() {
        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận");
        builder.setMessage("Bạn có chắc muốn hủy đặt cược hiện tại và quay lại màn hình đặt cược?");
        
        // Add confirm button
        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return to betting screen
                returnToBettingScreen();
            }
        });
        
        // Add cancel button
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        // Show the dialog
        builder.show();
    }
    
    private void returnToBettingScreen() {
        // Stop any running animations
        if (raceRunnable != null) {
            handler.removeCallbacks(raceRunnable);
        }
        
        // Start BettingActivity
        Intent intent = new Intent(MainActivity.this, BettingActivity.class);
        intent.putExtra("currentPoints", currentPoints);
        startActivity(intent);
        finish(); // Close current activity
    }
    
    private void setupPlayPauseButton() {
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRaceRunning) {
                    // Race is running, pause it
                    pauseRace();
                } else {
                    // Race is not running, start it
                    startRace();
                }
            }
        });
    }
    
    private void startRace() {
        // Change button to pause
        btnPlayPause.setImageResource(R.drawable.pause);
        isRaceRunning = true;
        
        // Disable checkboxes during race
        checkBox1.setEnabled(false);
        checkBox2.setEnabled(false);
        checkBox3.setEnabled(false);
        
        // Create and start race animation
        raceRunnable = new Runnable() {
            @Override
            public void run() {
                // Move animals if their checkboxes were checked
                moveAnimal(seekBar1);
                moveAnimal(seekBar2);
                moveAnimal(seekBar3);
                
                // Check if race is finished
                if (isRaceFinished()) {
                    determineWinner();
                    resetRace();
                } else if (isRaceRunning) {
                    // Continue race if it's still running
                    handler.postDelayed(this, 100); // Update every 100ms
                }
            }
        };
        
        // Start the race animation
        handler.post(raceRunnable);
        
        // Show toast message
        Toast.makeText(this, "Cuộc đua bắt đầu!", Toast.LENGTH_SHORT).show();
    }
    
    private void pauseRace() {
        // Change button to play
        btnPlayPause.setImageResource(R.drawable.play);
        isRaceRunning = false;
        
        // Remove callbacks to stop animation
        if (raceRunnable != null) {
            handler.removeCallbacks(raceRunnable);
        }
        
        // Show toast message
        Toast.makeText(this, "Cuộc đua tạm dừng!", Toast.LENGTH_SHORT).show();
    }
    
    private void moveAnimal(SeekBar seekBar) {
        // Get current progress
        int currentProgress = seekBar.getProgress();
        
        // Calculate random movement (1-5 points)
        int movement = random.nextInt(5) + 1;
        
        // Calculate new progress
        int newProgress = Math.min(currentProgress + movement, seekBar.getMax());
        
        // Set new progress
        seekBar.setProgress(newProgress);
    }
    
    private boolean isRaceFinished() {
        // Check if any animal has reached the finish line
        return seekBar1.getProgress() >= seekBar1.getMax() ||
               seekBar2.getProgress() >= seekBar2.getMax() ||
               seekBar3.getProgress() >= seekBar3.getMax();
    }
    
    private void determineWinner() {
        // Find which animal reached the finish line first
        int winner = -1;
        int maxProgress = -1;
        
        if (seekBar1.getProgress() > maxProgress) {
            maxProgress = seekBar1.getProgress();
            winner = 0;
        }
        
        if (seekBar2.getProgress() > maxProgress) {
            maxProgress = seekBar2.getProgress();
            winner = 1;
        }
        
        if (seekBar3.getProgress() > maxProgress) {
            maxProgress = seekBar3.getProgress();
            winner = 2;
        }
        
        // Check if player won
        boolean playerWon = false;
        
        if (winner == 0 && checkBox1.isChecked()) {
            playerWon = true;
        } else if (winner == 1 && checkBox2.isChecked()) {
            playerWon = true;
        } else if (winner == 2 && checkBox3.isChecked()) {
            playerWon = true;
        }
        
        // Update points and show result
        if (playerWon) {
            currentPoints += betAmount;
            Toast.makeText(this, "Chúc mừng! Bạn đã thắng " + betAmount + " điểm!", Toast.LENGTH_LONG).show();
        } else {
            currentPoints -= betAmount;
            Toast.makeText(this, "Tiếc quá! Bạn đã thua " + betAmount + " điểm!", Toast.LENGTH_LONG).show();
        }
    }
    
    private void resetRace() {
        // Reset race state
        isRaceRunning = false;
        btnPlayPause.setImageResource(R.drawable.play);
        
        // Reset seekbars
        seekBar1.setProgress(0);
        seekBar2.setProgress(0);
        seekBar3.setProgress(0);
        
        // Re-enable checkboxes for selected animals
        if (!selectedAnimalIndices.isEmpty()) {
            // Check which seekbar has the selected animal and enable its checkbox
            if (checkBox1.isChecked()) {
                checkBox1.setEnabled(true);
            } else if (checkBox2.isChecked()) {
                checkBox2.setEnabled(true);
            } else if (checkBox3.isChecked()) {
                checkBox3.setEnabled(true);
            }
        }
    }
    
    private void getSelectedAnimalInfo() {
        selectedAnimalNames.clear();
        selectedAnimalIndices.clear();
        
        if (selectedAnimals != null) {
            for (int i = 0; i < selectedAnimals.length; i++) {
                if (selectedAnimals[i]) {
                    selectedAnimalNames.add(animalNames[i]);
                    selectedAnimalIndices.add(i);
                }
            }
        }
    }
    
    private void setupRaceAnimals() {
        // Create a list of all possible animal indices (0-8)
        List<Integer> allAnimalIndices = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            allAnimalIndices.add(i);
        }
        
        // Remove selected animals from the pool to avoid duplicates
        allAnimalIndices.removeAll(selectedAnimalIndices);
        
        // Create a list to hold the final 3 animals for the race
        List<Integer> raceAnimalIndices = new ArrayList<>();
        
        // First, add selected animal (if any)
        if (!selectedAnimalIndices.isEmpty()) {
            // If multiple animals were selected, choose one randomly
            int randomSelectedIndex = random.nextInt(selectedAnimalIndices.size());
            int selectedAnimalIndex = selectedAnimalIndices.get(randomSelectedIndex);
            raceAnimalIndices.add(selectedAnimalIndex);
        }
        
        // Fill the remaining slots with random animals
        while (raceAnimalIndices.size() < 3 && !allAnimalIndices.isEmpty()) {
            int randomIndex = random.nextInt(allAnimalIndices.size());
            raceAnimalIndices.add(allAnimalIndices.get(randomIndex));
            allAnimalIndices.remove(randomIndex);
        }
        
        // Ensure we have exactly 3 animals
        while (raceAnimalIndices.size() < 3) {
            // In case we don't have enough animals, just add random indices
            raceAnimalIndices.add(random.nextInt(9));
        }
        
        // Set all checkboxes to unchecked and disabled by default
        checkBox1.setChecked(false);
        checkBox2.setChecked(false);
        checkBox3.setChecked(false);
        checkBox1.setEnabled(false);
        checkBox2.setEnabled(false);
        checkBox3.setEnabled(false);
        
        // Set the thumbs for the three seekbars based on selected animals
        setSeekBarThumb(seekBar1, raceAnimalIndices.get(0));
        setSeekBarThumb(seekBar2, raceAnimalIndices.get(1));
        setSeekBarThumb(seekBar3, raceAnimalIndices.get(2));
        
        // If we have a selected animal, check its checkbox and enable it
        if (!selectedAnimalIndices.isEmpty()) {
            int selectedAnimalIndex = raceAnimalIndices.get(0);
            
            // Check which seekbar has the selected animal
            if (selectedAnimalIndices.contains(selectedAnimalIndex)) {
                checkBox1.setChecked(true);
                checkBox1.setEnabled(true);
                setupCheckboxAndSeekbar(checkBox1, seekBar1);
            } else if (selectedAnimalIndices.contains(raceAnimalIndices.get(1))) {
                checkBox2.setChecked(true);
                checkBox2.setEnabled(true);
                setupCheckboxAndSeekbar(checkBox2, seekBar2);
            } else if (selectedAnimalIndices.contains(raceAnimalIndices.get(2))) {
                checkBox3.setChecked(true);
                checkBox3.setEnabled(true);
                setupCheckboxAndSeekbar(checkBox3, seekBar3);
            }
        }
    }
    
    private void setSeekBarThumb(SeekBar seekBar, int animalIndex) {
        // Set the thumb drawable to the corresponding animal
        if (animalIndex >= 0 && animalIndex < animalDrawables.length) {
            seekBar.setThumb(getResources().getDrawable(animalDrawables[animalIndex], getTheme()));
        }
    }
    
    private void updateBetInfo() {
        if (tvBetInfo != null && betAmount > 0 && !selectedAnimalNames.isEmpty()) {
            StringBuilder animalText = new StringBuilder();
            for (int i = 0; i < selectedAnimalNames.size(); i++) {
                if (i > 0) {
                    if (i == selectedAnimalNames.size() - 1) {
                        animalText.append(" và ");
                    } else {
                        animalText.append(", ");
                    }
                }
                animalText.append(selectedAnimalNames.get(i));
            }
            
            String betText = String.format("Cược: %d điểm - %s", betAmount, animalText.toString());
            tvBetInfo.setText(betText);
        }
    }
    
    private void preserveThumbAppearance(SeekBar seekBar) {
        // Ensure the thumb is always visible and doesn't change size
        seekBar.setPadding(seekBar.getPaddingLeft(), seekBar.getPaddingTop(), 
                seekBar.getPaddingRight(), seekBar.getPaddingBottom());
    }
    
    private void setupCheckboxAndSeekbar(CheckBox checkBox, final SeekBar seekBar) {
        // When checkbox is checked/unchecked, enable/disable seekbar
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            seekBar.setEnabled(isChecked);
            
            if (isChecked) {
                // Just set progress without modifying the thumb
                seekBar.setProgress(0);
            } else {
                // Just reset progress without modifying the thumb
                seekBar.setProgress(0);
            }
        });
        
        // Initialize seekbar state based on checkbox
        seekBar.setEnabled(checkBox.isChecked());
        
        // When seekbar is moved, ensure checkbox stays checked
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // If user is moving the seekbar, make sure checkbox is checked
                if (fromUser && !checkBox.isChecked()) {
                    checkBox.setChecked(true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Make sure checkbox is checked when user starts using seekbar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: Add any behavior when user stops moving seekbar
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure to remove any callbacks to prevent memory leaks
        if (raceRunnable != null) {
            handler.removeCallbacks(raceRunnable);
        }
    }
}