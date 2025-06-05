package com.group6.miniproject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Round2Activity extends AppCompatActivity {
    private CheckBox checkBox1, checkBox2, checkBox3;
    private SeekBar seekBar1, seekBar2, seekBar3;
    private ImageView lineImageView;
    private TextView tvBetInfo;
    private TextView tvTimer;
    private TextView tvRound;
    private ImageButton btnSettings, btnPlayPause;
    private boolean isPlaying = false;
    

    
    private int volumeLevel = 80; // Default volume level (0-100)
    private boolean vibrationEnabled = true;
    private int difficultyLevel = 1; // 0: Easy, 1: Normal, 2: Hard
    
    private boolean[] selectedAnimals;
    private int betAmount = 0;
    private int currentPoints = 0;
    private List<String> selectedAnimalNames = new ArrayList<>();
    private List<Integer> selectedAnimalIndices = new ArrayList<>();
    private Random random = new Random();
    
    private ObjectAnimator animator1, animator2, animator3;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;
    private boolean raceFinished = false;
    private int winningAnimalPosition = -1; // 0 for first, 1 for second, 2 for third
    
    // Round 2 specific - higher difficulty
    private final float SPEED_MULTIPLIER = 1.5f; // Animals move faster in round 2
    
    // FIXED: Match the exact same order as in MainActivity
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
    
    private int[] raceAnimalIndices = new int[3];
    
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
        
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_round2);
        
        // Initialize audio player
        initializeMediaPlayer();
        
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
        tvRound = findViewById(R.id.tvRound);
        
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
        
        // Set round info
        tvRound.setText("Vòng 2");
        
        // Set up checkboxes for Round 2 - ensure they're checked for the selected animals
        setupCheckboxes();
    }
    
    /**
     * Set up checkboxes for Round 2 - ensure they're checked for the selected animals
     */
    private void setupCheckboxes() {
        System.out.println("========== SETTING UP CHECKBOXES FOR ROUND 2 ==========");
        
        // STEP 1: Get the bet animal index directly from the intent
        int betAnimalIndex = getIntent().getIntExtra("betAnimalIndex", -1);
        
        // Print all intent extras for debugging
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            System.out.println("Intent extras for checkbox setup:");
            for (String key : extras.keySet()) {
                System.out.println("Key: " + key + " = " + extras.get(key));
            }
        }
        
        // STEP 2: If we don't have a valid bet animal index from the intent, try to get it from selectedAnimalIndices
        if (betAnimalIndex < 0 && !selectedAnimalIndices.isEmpty()) {
            betAnimalIndex = selectedAnimalIndices.get(0);
            System.out.println("Using bet animal from selectedAnimalIndices: " + animalNames[betAnimalIndex]);
        }
        
        // STEP 3: If we still don't have a valid bet animal index, try to get it from selectedAnimals array
        if (betAnimalIndex < 0) {
            boolean[] selectedAnimalsArray = getIntent().getBooleanArrayExtra("selectedAnimals");
            if (selectedAnimalsArray != null) {
                for (int i = 0; i < selectedAnimalsArray.length && i < animalNames.length; i++) {
                    if (selectedAnimalsArray[i]) {
                        betAnimalIndex = i;
                        System.out.println("Found bet animal in selectedAnimals array: " + animalNames[i]);
                        break;
                    }
                }
            }
        }
        
        // STEP 4: If we have a valid bet animal index, check the appropriate checkbox
        if (betAnimalIndex >= 0 && betAnimalIndex < animalNames.length) {
            System.out.println("FINAL BET ANIMAL FOR CHECKBOX: " + animalNames[betAnimalIndex] + " (index: " + betAnimalIndex + ")");
            
            // CRITICAL FIX: First uncheck all checkboxes
            checkBox1.setChecked(false);
            checkBox2.setChecked(false);
            checkBox3.setChecked(false);
            
            // Check which lane has the betted animal and check that checkbox
            if (raceAnimalIndices[0] == betAnimalIndex) {
                System.out.println("Checking checkbox 1 for " + animalNames[betAnimalIndex]);
                checkBox1.setChecked(true);
            } else if (raceAnimalIndices[1] == betAnimalIndex) {
                System.out.println("Checking checkbox 2 for " + animalNames[betAnimalIndex]);
                checkBox2.setChecked(true);
            } else if (raceAnimalIndices[2] == betAnimalIndex) {
                System.out.println("Checking checkbox 3 for " + animalNames[betAnimalIndex]);
                checkBox3.setChecked(true);
            } else {
                System.out.println("WARNING: Bet animal " + animalNames[betAnimalIndex] + " not found in race animals!");
                System.out.println("Race animals: " + 
                                  animalNames[raceAnimalIndices[0]] + ", " +
                                  animalNames[raceAnimalIndices[1]] + ", " +
                                  animalNames[raceAnimalIndices[2]]);
            }
        } else {
            System.out.println("ERROR: No valid bet animal index found for checkbox selection!");
        }
        
        // Print the final checkbox state
        System.out.println("Final checkbox state:");
        System.out.println("Checkbox 1 (" + animalNames[raceAnimalIndices[0]] + "): " + checkBox1.isChecked());
        System.out.println("Checkbox 2 (" + animalNames[raceAnimalIndices[1]] + "): " + checkBox2.isChecked());
        System.out.println("Checkbox 3 (" + animalNames[raceAnimalIndices[2]] + "): " + checkBox3.isChecked());
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
        
        dialog.show();
    }
    
    private void applyVolumeSettings() {
        // Update the media player volume based on current setting
        updateMediaPlayerVolume();
        Toast.makeText(this, "Âm lượng: " + volumeLevel + "%", Toast.LENGTH_SHORT).show();
    }
    
    private void applyVibrationSetting() {
        // Apply vibration settings (implementation would depend on how vibration is managed)
    }
    
    private void applyDifficultySetting() {
        // Apply difficulty settings
        // This would affect race speed, animal behavior, etc.
    }
    
    private void startRace() {
        if (!raceFinished) {
            // Reset race state
            resetRace();
            
            // Change music to round music
            changeBackgroundMusic(AudioManager.ROUND_MUSIC, true);
            
            // Start timer
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            
            // Start race animation
            animateRace();
        }
    }
    
    private void pauseRace() {
        // Pause animations
        if (animator1 != null && animator1.isRunning()) {
            animator1.pause();
        }
        if (animator2 != null && animator2.isRunning()) {
            animator2.pause();
        }
        if (animator3 != null && animator3.isRunning()) {
            animator3.pause();
        }
        
        // Pause timer
        timerHandler.removeCallbacks(timerRunnable);
    }
    
    private void resetRace() {
        // Reset animations
        if (animator1 != null) {
            animator1.cancel();
        }
        if (animator2 != null) {
            animator2.cancel();
        }
        if (animator3 != null) {
            animator3.cancel();
        }
        
        // Reset SeekBars to start position
        seekBar1.setProgress(0);
        seekBar2.setProgress(0);
        seekBar3.setProgress(0);
        
        // Reset timer
        timerHandler.removeCallbacks(timerRunnable);
        tvTimer.setText("00:00");
        
        // Reset race state
        raceFinished = false;
        winningAnimalPosition = -1;
    }
    
    private void animateRace() {
        // Calculate finish position (max progress)
        final int maxProgress = seekBar1.getMax();
        
        // Create animators for each animal with varying durations based on difficulty
        // Round 2 has faster animations
        int baseDuration = 10000; // 10 seconds base duration
        int durationVariance = 3000; // 3 seconds variance
        
        // Apply round 2 speed multiplier
        baseDuration = (int)(baseDuration / SPEED_MULTIPLIER);
        
        // Create animator for first animal
        animator1 = ObjectAnimator.ofInt(seekBar1, "progress", 0, maxProgress);
        animator1.setDuration(baseDuration + random.nextInt(durationVariance));
        
        // Create animator for second animal
        animator2 = ObjectAnimator.ofInt(seekBar2, "progress", 0, maxProgress);
        animator2.setDuration(baseDuration + random.nextInt(durationVariance));
        
        // Create animator for third animal
        animator3 = ObjectAnimator.ofInt(seekBar3, "progress", 0, maxProgress);
        animator3.setDuration(baseDuration + random.nextInt(durationVariance));
        
        // Add listeners to detect when animals finish
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
        // If race is already finished, do nothing
        if (raceFinished) {
            return;
        }
        
        // Mark race as finished
        raceFinished = true;
        winningAnimalPosition = animalPosition;
        
        // Stop all animations
        if (animator1 != null && animator1.isRunning()) {
            animator1.end();
        }
        if (animator2 != null && animator2.isRunning()) {
            animator2.end();
        }
        if (animator3 != null && animator3.isRunning()) {
            animator3.end();
        }
        
        // Stop timer
        timerHandler.removeCallbacks(timerRunnable);
        
        // Update play/pause button
        btnPlayPause.setImageResource(R.drawable.play);
        isPlaying = false;
        
                // Check if player won
        final boolean playerWon = checkPlayerWon();
        
        // Change to ending music
        changeBackgroundMusic(AudioManager.ENDING_MUSIC, false);
        
        // Update points and show result dialog
        // Add a delay to show the result dialog after the race finishes
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                updatePointsAndShowResult(playerWon);
            }
        }, 500);
    }
    
    private void getSelectedAnimalInfo() {
        // First check if we have the selected animal indices array from Round 1
        if (getIntent().hasExtra("selectedAnimalIndicesArray")) {
            int[] selectedIndicesArray = getIntent().getIntArrayExtra("selectedAnimalIndicesArray");
            if (selectedIndicesArray != null && selectedIndicesArray.length > 0) {
                // Use the selected animal indices from Round 1
                for (int index : selectedIndicesArray) {
                    selectedAnimalIndices.add(index);
                    selectedAnimalNames.add(animalNames[index]);
                }
                return; // We have the selected animals, no need to check selectedAnimals
            }
        }
        
        // Fallback to using selectedAnimals if the indices array is not available
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
        // Nhận danh sách các nhân vật đã chọn từ intent
        int[] betAnimalIndices = getIntent().getIntArrayExtra("betAnimalIndices");
        List<Integer> finalRaceIndices = new ArrayList<>();
        List<Integer> allIndices = new ArrayList<>();
        for (int i = 0; i < animalDrawables.length; i++) allIndices.add(i);
        if (betAnimalIndices != null && betAnimalIndices.length > 0) {
            // Thêm các nhân vật đã chọn
            for (int idx : betAnimalIndices) {
                finalRaceIndices.add(idx);
                allIndices.remove((Integer) idx);
            }
            // Nếu chưa đủ 3, random bổ sung
            java.util.Collections.shuffle(allIndices, random);
            while (finalRaceIndices.size() < 3 && !allIndices.isEmpty()) {
                finalRaceIndices.add(allIndices.remove(0));
            }
        } else {
            // Nếu không có dữ liệu, random 3 nhân vật
            java.util.Collections.shuffle(allIndices, random);
            for (int i = 0; i < 3; i++) finalRaceIndices.add(allIndices.get(i));
        }
        // Gán vào raceAnimalIndices
        for (int i = 0; i < 3; i++) raceAnimalIndices[i] = finalRaceIndices.get(i);
        // Set thumb cho seekbar
        setSeekBarThumb(seekBar1, raceAnimalIndices[0]);
        setSeekBarThumb(seekBar2, raceAnimalIndices[1]);
        setSeekBarThumb(seekBar3, raceAnimalIndices[2]);
    }
    
    /**
     * Selects random animals for the race
     */
    private void selectRandomAnimals() {
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < animalDrawables.length; i++) {
            availableIndices.add(i);
        }
        
        // Shuffle the indices
        Collections.shuffle(availableIndices, random);
        
        // Select the first 3 animals
        for (int i = 0; i < 3; i++) {
            raceAnimalIndices[i] = availableIndices.get(i);
        }
    }
    
    private void setSeekBarThumb(SeekBar seekBar, int animalIndex) {
        seekBar.setThumb(getResources().getDrawable(animalDrawables[animalIndex], getTheme()));
    }
    
    private void updateBetInfo() {
        // Create bet info text
        StringBuilder betInfo = new StringBuilder("Cược: " + betAmount + " điểm");
        
        // Add selected animals
        if (!selectedAnimalNames.isEmpty()) {
            betInfo.append(" (");
            for (int i = 0; i < selectedAnimalNames.size(); i++) {
                betInfo.append(selectedAnimalNames.get(i));
                if (i < selectedAnimalNames.size() - 1) {
                    betInfo.append(", ");
                }
            }
            betInfo.append(")");
        }
        
        // Set the text
        tvBetInfo.setText(betInfo.toString());
    }
    
    private void preserveThumbAppearance(SeekBar seekBar) {
        // This method preserves the appearance of the seekbar thumb
        // by preventing it from being clipped or scaled
    }
    
    public void updatePointsAndShowResult(boolean playerWon) {
        String resultMessage;
        int pointsChange;
        
        // For Round 2:
        // - Win: Get 5x the Round 1 bet amount
        // - Lose: Lose the entire Round 1 bet amount
        
        if (playerWon) {
            // Win in Round 2: Get 5x the bet amount
            pointsChange = betAmount * 5;
            
            // Check if we're using the 5x bet multiplier from Round 1 option
            boolean isBet5x = getIntent().getBooleanExtra("bet5x", false);
            if (isBet5x) {
                // If player chose 5x bet option in Round 1, they already risked 5x
                // So we don't multiply again, they just get the 5x they bet
                pointsChange = betAmount * 5;
            }
            
            currentPoints += pointsChange;
            resultMessage = "Chúc mừng! Bạn đã thắng vòng 2";
        } else {
            // Lose in Round 2: Lose the entire bet amount
            pointsChange = -betAmount;
            
            // Check if we're using the 5x bet multiplier from Round 1 option
            boolean isBet5x = getIntent().getBooleanExtra("bet5x", false);
            if (isBet5x) {
                // If player chose 5x bet option in Round 1, they lose 5x the bet
                pointsChange = -betAmount;
            }
            
            currentPoints += pointsChange;
            resultMessage = "Tiếc quá! Bạn đã thua vòng 2";
        }
        
        // Create and show the results dialog
        showResultsDialog(resultMessage, pointsChange, playerWon);
    }
    
    private void showResultsDialog(String resultMessage, int pointsChange, boolean playerWon) {
        // Create dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Create custom layout for the dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_race_result, null);
        builder.setView(dialogView);
        
        // Get references to the views in the dialog
        TextView tvPointsChange = dialogView.findViewById(R.id.tv_points_change);
        TextView tvCurrentPoints = dialogView.findViewById(R.id.tv_current_points);
        
        // Get references to ranking views
        TextView tvRank1Name = dialogView.findViewById(R.id.tv_rank1_name);
        TextView tvRank2Name = dialogView.findViewById(R.id.tv_rank2_name);
        TextView tvRank3Name = dialogView.findViewById(R.id.tv_rank3_name);
        TextView tvRank1TimeValue = dialogView.findViewById(R.id.tv_rank1_time_value);
        TextView tvRank2TimeValue = dialogView.findViewById(R.id.tv_rank2_time_value);
        TextView tvRank3TimeValue = dialogView.findViewById(R.id.tv_rank3_time_value);
        ImageView ivRank1Animal = dialogView.findViewById(R.id.iv_rank1_animal);
        ImageView ivRank2Animal = dialogView.findViewById(R.id.iv_rank2_animal);
        ImageView ivRank3Animal = dialogView.findViewById(R.id.iv_rank3_animal);
        Button btnBetAgain = dialogView.findViewById(R.id.btn_bet_again);
        Button btnContinue = dialogView.findViewById(R.id.btn_continue);
        
        // Set the text for the views
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
        
        // Determine the finishing order based on our race
        // The first animal is the one that finished (winningAnimalPosition)
        // We'll randomly assign the other positions
        int[] finishOrder = new int[3]; // Stores the indices of seekbars in finish order
        finishOrder[0] = winningAnimalPosition;
        
        // Randomly assign 2nd and 3rd places from the remaining animals
        List<Integer> remainingPositions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (i != winningAnimalPosition) {
                remainingPositions.add(i);
            }
        }
        // Shuffle the remaining positions
        Collections.shuffle(remainingPositions, random);
        finishOrder[1] = remainingPositions.get(0);
        finishOrder[2] = remainingPositions.get(1);
        
        // Map the seekbar positions to animal indices
        int[] animalIndices = new int[3];
        animalIndices[0] = raceAnimalIndices[finishOrder[0]];
        animalIndices[1] = raceAnimalIndices[finishOrder[1]];
        animalIndices[2] = raceAnimalIndices[finishOrder[2]];
        
        // Set the animal names and images for each rank
        tvRank1Name.setText(animalNames[animalIndices[0]]);
        tvRank2Name.setText(animalNames[animalIndices[1]]);
        tvRank3Name.setText(animalNames[animalIndices[2]]);
        
        ivRank1Animal.setImageResource(animalDrawables[animalIndices[0]]);
        ivRank2Animal.setImageResource(animalDrawables[animalIndices[1]]);
        ivRank3Animal.setImageResource(animalDrawables[animalIndices[2]]);
        
        // Generate finish times (winner has shortest time)
        long raceTimeMillis = System.currentTimeMillis() - startTime;
        int winnerSeconds = (int)(raceTimeMillis / 1000);
        int winnerMillis = (int)((raceTimeMillis % 1000) / 10); // Convert to centiseconds (0-99)
        int secondPlaceSeconds = winnerSeconds + random.nextInt(3) + 1; // 1-3 seconds more
        int secondPlaceMillis = random.nextInt(100); // 0-99 centiseconds
        int thirdPlaceSeconds = secondPlaceSeconds + random.nextInt(3) + 1; // 1-3 seconds more than second
        int thirdPlaceMillis = random.nextInt(100); // 0-99 centiseconds
        
        tvRank1TimeValue.setText(String.format("%02d:%02d.%02d", winnerSeconds / 60, winnerSeconds % 60, winnerMillis));
        tvRank2TimeValue.setText(String.format("%02d:%02d.%02d", secondPlaceSeconds / 60, secondPlaceSeconds % 60, secondPlaceMillis));
        tvRank3TimeValue.setText(String.format("%02d:%02d.%02d", thirdPlaceSeconds / 60, thirdPlaceSeconds % 60, thirdPlaceMillis));
        
        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dismissing by tapping outside
        
        // Hide the Continue button since we only need "Back to Betting" in Round 2
        btnContinue.setVisibility(View.GONE);
        
        // Change text of Bet Again button to make it clearer
        btnBetAgain.setText("Quay lại đặt cược");
        
        // Set button click listeners
        btnBetAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                
                // Check if player is out of points before returning to betting screen
                if (currentPoints <= 0) {
                    showGameOverDialog();
                } else {
                    returnToBettingScreen();
                }
            }
        });
        
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
    
    private void returnToBettingScreen() {
        // Change back to background music before leaving
        changeBackgroundMusic(AudioManager.BACKGROUND_MUSIC, true);
        
        // Start BettingActivity
        Intent intent = new Intent(Round2Activity.this, BettingActivity.class);
        intent.putExtra("currentPoints", currentPoints);
        startActivity(intent);
        finish(); // Close current activity
    }
    
    /**
     * Check if the player won the race
     * @return true if player won, false otherwise
     */
    private boolean checkPlayerWon() {
        boolean playerWon = false;
        for (int i = 0; i < selectedAnimalIndices.size(); i++) {
            if (raceAnimalIndices[winningAnimalPosition] == selectedAnimalIndices.get(i)) {
                playerWon = true;
                break;
            }
        }
        return playerWon;
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
    
    /**
     * Initializes the MediaPlayer to play background music
     * Note: We don't force audio restart to preserve continuity between screens
     */
    private void initializeMediaPlayer() {
        // Just ensure the AudioManager exists, but don't restart music
        // This maintains continuity from previous activities
        
        // Set volume level
        AudioManager.getInstance().setVolume(volumeLevel);
    }
    
    /**
     * Changes the background music
     * @param resId Resource ID of the audio file
     * @param looping Whether the audio should loop
     */
    private void changeBackgroundMusic(int resId, boolean looping) {
        // Use the AudioManager singleton to change music
        // Pass false as the last parameter to avoid forcing restart when same music is playing
        AudioManager.getInstance().playMusic(this, resId, looping, false);
    }
    
    /**
     * Updates the media player volume based on the volume level setting
     */
    private void updateMediaPlayerVolume() {
        // Update volume using the AudioManager singleton
        AudioManager.getInstance().setVolume(volumeLevel);
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