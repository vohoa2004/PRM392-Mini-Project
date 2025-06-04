package com.group6.miniproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.LayerDrawable;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.CompoundButton;

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
    private TextView tvTimer;
    private ImageButton btnSettings, btnPlayPause;
    private boolean isPlaying = false;
    
    // Settings variables
    private int volumeLevel = 80; // Default volume level (0-100)
    private boolean vibrationEnabled = true;
    private int difficultyLevel = 1; // 0: Easy, 1: Normal, 2: Hard
    
    private boolean[] selectedAnimals;
    private int betAmount = 0;
    private int currentPoints = 0;
    private List<String> selectedAnimalNames = new ArrayList<>();
    private List<Integer> selectedAnimalIndices = new ArrayList<>();
    private Random random = new Random();
    
    // Race animation variables
    private ObjectAnimator animator1, animator2, animator3;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;
    private boolean raceFinished = false;
    private int winningAnimalPosition = -1; // 0 for first, 1 for second, 2 for third
    
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
    
    // Timer runnable for updating the timer display
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying && !raceFinished) {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                
                tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
                
                // Schedule the next update in 100ms
                timerHandler.postDelayed(this, 100);
            }
        }
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
        
        // Initialize bet info text view and timer
        tvBetInfo = findViewById(R.id.tvBetInfo);
        tvTimer = findViewById(R.id.tvTimer);
        
        // Initialize buttons
        btnSettings = findViewById(R.id.btnSettings);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        
        // Set up button click listeners
        setupButtonListeners();
        
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
        
        // Initialize timer
        tvTimer.setText("00:00");
    }
    
    private void setupButtonListeners() {
        // Settings button click listener
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show settings dialog
                showSettingsDialog();
            }
        });
        
        // Play/Pause button click listener
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlaying = !isPlaying;
                
                // Update button image based on state
                if (isPlaying) {
                    btnPlayPause.setImageResource(R.drawable.pause);
                    startRace();
                } else {
                    btnPlayPause.setImageResource(R.drawable.play);
                    pauseRace();
                }
            }
        });
    }
    
    private void showSettingsDialog() {
        // Create dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cài đặt");
        
        // Inflate custom layout for the dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);
        
        // Get references to the views in the dialog
        final View layoutCancelBetting = dialogView.findViewById(R.id.layout_cancel_betting);
        final SeekBar seekBarVolume = dialogView.findViewById(R.id.seekbar_volume);
        final TextView tvVolumeValue = dialogView.findViewById(R.id.tv_volume_value);
        final Switch switchVibration = dialogView.findViewById(R.id.switch_vibration);
        final RadioGroup radioGroupDifficulty = dialogView.findViewById(R.id.radio_group_difficulty);
        final RadioButton radioEasy = dialogView.findViewById(R.id.radio_easy);
        final RadioButton radioNormal = dialogView.findViewById(R.id.radio_normal);
        final RadioButton radioHard = dialogView.findViewById(R.id.radio_hard);
        Button btnAbout = dialogView.findViewById(R.id.btn_about);
        
        // Set initial values based on current settings
        seekBarVolume.setProgress(volumeLevel);
        tvVolumeValue.setText(volumeLevel + "%");
        switchVibration.setChecked(vibrationEnabled);
        
        // Set the correct radio button based on difficulty level
        switch (difficultyLevel) {
            case 0:
                radioEasy.setChecked(true);
                break;
            case 1:
                radioNormal.setChecked(true);
                break;
            case 2:
                radioHard.setChecked(true);
                break;
        }
        
        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        
        // Set click listener for cancel betting option
        layoutCancelBetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog before canceling bet
                showCancelBettingConfirmationDialog();
                // Dismiss the settings dialog
                dialog.dismiss();
            }
        });
        
        // Set listeners for the settings controls
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeLevel = progress;
                tvVolumeValue.setText(progress + "%");
                applyVolumeSettings();
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
        
        switchVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vibrationEnabled = isChecked;
                // Apply vibration setting
                applyVibrationSetting();
            }
        });
        
        radioGroupDifficulty.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_easy) {
                    difficultyLevel = 0;
                } else if (checkedId == R.id.radio_normal) {
                    difficultyLevel = 1;
                } else if (checkedId == R.id.radio_hard) {
                    difficultyLevel = 2;
                }
                // Apply difficulty setting
                applyDifficultySetting();
            }
        });
        
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
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
        dialog.show();
    }
    
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông tin");
        builder.setMessage("Ứng dụng đua thú\nPhiên bản 1.0\n\nNhóm 6 - PRM392\n\n© 2024 Đại học FPT");
        builder.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    
    private void applyVolumeSettings() {
        // Implement volume setting logic
        Toast.makeText(this, "Âm lượng: " + volumeLevel + "%", Toast.LENGTH_SHORT).show();
        // Here you would actually adjust the app's volume
    }
    
    private void applyVibrationSetting() {
        // Implement vibration setting logic
        Toast.makeText(this, "Rung: " + (vibrationEnabled ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
    }
    
    private void applyDifficultySetting() {
        // Implement difficulty setting logic
        String difficultyText;
        switch (difficultyLevel) {
            case 0:
                difficultyText = "Dễ";
                break;
            case 1:
                difficultyText = "Thường";
                break;
            case 2:
                difficultyText = "Khó";
                break;
            default:
                difficultyText = "Thường";
        }
        Toast.makeText(this, "Độ khó: " + difficultyText, Toast.LENGTH_SHORT).show();
    }
    
    private void startRace() {
        if (raceFinished) {
            // If race is already finished, reset everything for a new race
            resetRace();
        }
        
        // Start the timer
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
        
        // Animate the seekbars to simulate race
        animateRace();
    }
    
    private void pauseRace() {
        // Pause the timer
        timerHandler.removeCallbacks(timerRunnable);
        
        // Pause the animations
        if (animator1 != null && animator1.isRunning()) {
            animator1.pause();
        }
        if (animator2 != null && animator2.isRunning()) {
            animator2.pause();
        }
        if (animator3 != null && animator3.isRunning()) {
            animator3.pause();
        }
    }
    
    private void resetRace() {
        // Reset race state
        raceFinished = false;
        winningAnimalPosition = -1;
        
        // Reset seekbar progress
        seekBar1.setProgress(0);
        seekBar2.setProgress(0);
        seekBar3.setProgress(0);
        
        // Reset timer
        tvTimer.setText("00:00");
        
        // Cancel any running animations
        if (animator1 != null) {
            animator1.cancel();
        }
        if (animator2 != null) {
            animator2.cancel();
        }
        if (animator3 != null) {
            animator3.cancel();
        }
    }
    
    private void animateRace() {
        // Generate random durations for each animal (between 5-10 seconds)
        int duration1 = 5000 + random.nextInt(5000);
        int duration2 = 5000 + random.nextInt(5000);
        int duration3 = 5000 + random.nextInt(5000);
        
        // Create progress animations for each seekbar
        animator1 = ObjectAnimator.ofInt(seekBar1, "progress", 0, 100);
        animator1.setDuration(duration1);
        
        animator2 = ObjectAnimator.ofInt(seekBar2, "progress", 0, 100);
        animator2.setDuration(duration2);
        
        animator3 = ObjectAnimator.ofInt(seekBar3, "progress", 0, 100);
        animator3.setDuration(duration3);
        
        // Add listeners to detect which animal finishes first
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                checkRaceFinished(0);
            }
        });
        
        animator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                checkRaceFinished(1);
            }
        });
        
        animator3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                checkRaceFinished(2);
            }
        });
        
        // Start the animations
        animator1.start();
        animator2.start();
        animator3.start();
    }
    
    private synchronized void checkRaceFinished(int animalPosition) {
        if (!raceFinished) {
            // This is the first animal to finish
            raceFinished = true;
            winningAnimalPosition = animalPosition;
            
            // Stop the timer
            timerHandler.removeCallbacks(timerRunnable);
            
            // Stop the other animations
            if (animalPosition != 0 && animator1 != null) {
                animator1.cancel();
            }
            if (animalPosition != 1 && animator2 != null) {
                animator2.cancel();
            }
            if (animalPosition != 2 && animator3 != null) {
                animator3.cancel();
            }
            
            // Reset play button state
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.play);
            
            // Check if player won
            boolean playerWon = false;
            
            if (animalPosition == 0 && checkBox1.isChecked()) {
                playerWon = true;
            } else if (animalPosition == 1 && checkBox2.isChecked()) {
                playerWon = true;
            } else if (animalPosition == 2 && checkBox3.isChecked()) {
                playerWon = true;
            }
            
            // Show the results after a short delay
            final boolean finalPlayerWon = playerWon;
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    updatePointsAndShowResult(finalPlayerWon);
                }
            }, 1000);
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
    
    /**
     * Updates points based on race result and shows a dialog
     * @param playerWon true if player won, false otherwise
     */
    public void updatePointsAndShowResult(boolean playerWon) {
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
    
    /**
     * Shows a dialog with race results and points information
     * @param resultMessage The message to show (win/lose)
     * @param pointsChange The points gained or lost
     */
    private void showResultsDialog(String resultMessage, int pointsChange) {
        // Create dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Set dialog title based on result
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
    
    /**
     * Shows a game over dialog when player runs out of points
     */
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
    
    /**
     * Returns to the betting screen with current points
     */
    private void returnToBettingScreen() {
        // Start BettingActivity
        Intent intent = new Intent(MainActivity.this, BettingActivity.class);
        intent.putExtra("currentPoints", currentPoints);
        startActivity(intent);
        finish(); // Close current activity
    }
    
    private void showCancelBettingConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận hủy cược");
        builder.setMessage("Bạn có chắc chắn muốn hủy đặt cược hiện tại và quay lại màn hình đặt cược?");
        builder.setIcon(R.drawable.cancel);
        
        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return to betting screen
                returnToBettingScreen();
            }
        });
        
        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        builder.show();
    }
}