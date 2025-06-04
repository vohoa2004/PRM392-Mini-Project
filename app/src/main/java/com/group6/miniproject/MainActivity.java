package com.group6.miniproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.LayerDrawable;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private CheckBox checkBox1, checkBox2, checkBox3;
    private SeekBar seekBar1, seekBar2, seekBar3;
    private ImageView lineImageView;
    private TextView tvBetInfo;
    private TextView tvTimer;
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
    
    // Timer variables
    private long startTime = 0L;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    
    // Audio control
    private AudioManager audioManager;
    
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
        
        // Get audio manager
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        
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
        
        // Initialize timer text view
        tvTimer = findViewById(R.id.tvTimer);
        
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
        
        // Initialize timer runnable
        setupTimer();
    }
    
    private void setupTimer() {
        // Initialize timer display
        tvTimer.setText("00:00");
        
        // Create timer runnable
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
                updatedTime = timeSwapBuff + timeInMilliseconds;
                
                int secs = (int) (updatedTime / 1000);
                int mins = secs / 60;
                secs = secs % 60;
                
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
                
                timerHandler.postDelayed(this, 100);
            }
        };
    }
    
    private void startTimer() {
        if (startTime == 0L) {
            startTime = SystemClock.uptimeMillis();
        } else {
            // Resume timer
            startTime = SystemClock.uptimeMillis() - timeInMilliseconds + timeSwapBuff;
        }
        
        timerHandler.postDelayed(timerRunnable, 0);
    }
    
    private void pauseTimer() {
        timeSwapBuff += timeInMilliseconds;
        timerHandler.removeCallbacks(timerRunnable);
    }
    
    private void resetTimer() {
        startTime = 0L;
        timeSwapBuff = 0L;
        timeInMilliseconds = 0L;
        updatedTime = 0L;
        tvTimer.setText("00:00");
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
        
        // Create a custom layout for the dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);
        
        // Get references to the views in the dialog
        View volumeOption = dialogView.findViewById(R.id.option_volume);
        View cancelOption = dialogView.findViewById(R.id.option_cancel);
        
        // Set up click listeners for options
        volumeOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVolumeSettingsDialog();
            }
        });
        
        cancelOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmCancelBet();
            }
        });
        
        // Add cancel button
        builder.setNegativeButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        // Show the dialog
        builder.show();
    }
    
    private void showVolumeSettingsDialog() {
        // Create dialog for volume settings
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Âm lượng");
        
        // Create a custom layout for the volume dialog
        View volumeView = LayoutInflater.from(this).inflate(R.layout.dialog_volume, null);
        builder.setView(volumeView);
        
        // Get reference to the volume seekbar
        SeekBar volumeSeekBar = volumeView.findViewById(R.id.volume_seekbar);
        
        // Set up the seekbar with current volume level
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);
        
        // Set up seekbar listener to adjust volume
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });
        
        // Add close button
        builder.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        // Show the dialog
        builder.show();
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
        
        // Stop timer
        timerHandler.removeCallbacks(timerRunnable);
        
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
        
        // Start timer
        startTimer();
        
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
        
        // Pause timer
        pauseTimer();
        
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
        updatePointsAndShowResult(playerWon);
    }
    
    private void updatePointsAndShowResult(boolean playerWon) {
        int pointsChange;
        String resultMessage;
        
        // Update points based on win/loss
        if (playerWon) {
            pointsChange = betAmount;
            currentPoints += pointsChange;
            resultMessage = "Chúc mừng! Bạn đã thắng";
        } else {
            pointsChange = -betAmount;
            currentPoints += pointsChange;
            resultMessage = "Tiếc quá! Bạn đã thua";
        }
        
        // Create and show the results dialog
        showResultsDialog(resultMessage, pointsChange);
    }
    
    private void showResultsDialog(String resultMessage, int pointsChange) {
        // Create dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Set dialog title and icon
        if (pointsChange > 0) {
            builder.setTitle("Thắng cuộc!");
        } else {
            builder.setTitle("Thua cuộc!");
        }
        
        // Create custom layout for the dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_race_result, null);
        builder.setView(dialogView);
        
        // Get references to the views in the dialog
        TextView tvResultMessage = dialogView.findViewById(R.id.tv_result_message);
        TextView tvPointsChange = dialogView.findViewById(R.id.tv_points_change);
        TextView tvCurrentPoints = dialogView.findViewById(R.id.tv_current_points);
        
        // Set the text for the views
        tvResultMessage.setText(resultMessage);
        
        String pointsChangeText = (pointsChange > 0 ? "+" : "") + pointsChange + " điểm";
        tvPointsChange.setText(pointsChangeText);
        
        String currentPointsText = "Số điểm hiện tại: " + currentPoints;
        tvCurrentPoints.setText(currentPointsText);
        
        // Set text color based on win/loss
        if (pointsChange > 0) {
            tvPointsChange.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvPointsChange.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // Add continue button
        builder.setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                
                // Check if player is out of points
                if (currentPoints <= 0) {
                    showGameOverDialog();
                }
            }
        });
        
        // Add return to betting button
        builder.setNegativeButton("Đặt cược lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                returnToBettingScreen();
            }
        });
        
        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dismissing by tapping outside
        dialog.show();
    }
    
    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hết điểm!");
        builder.setMessage("Bạn đã hết điểm. Bạn có muốn bắt đầu lại với 1000 điểm không?");
        
        // Add restart button
        builder.setPositiveButton("Bắt đầu lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Reset points and return to betting screen
                currentPoints = 1000;
                returnToBettingScreen();
            }
        });
        
        // Add quit button
        builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Close the app
            }
        });
        
        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dismissing by tapping outside
        dialog.show();
    }
    
    private void resetRace() {
        // Reset race state
        isRaceRunning = false;
        btnPlayPause.setImageResource(R.drawable.play);
        
        // Reset timer
        resetTimer();
        
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
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}