package com.group6.miniproject;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
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
    
    private boolean[] selectedAnimals;
    private int betAmount = 0;
    private int currentPoints = 0;
    private List<String> selectedAnimalNames = new ArrayList<>();
    private List<Integer> selectedAnimalIndices = new ArrayList<>();
    private Random random = new Random();
    
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
}