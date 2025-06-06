package com.group6.miniproject;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

public class BettingActivity extends AppCompatActivity {

    private EditText etBetAmount;
    private TextView tvCurrentPoints;
    private Button btnPlaceBet;
    private GridLayout gridAnimals;
    
    // Map to store animal selections (animalId -> isSelected)
    private Map<Integer, Boolean> selectedAnimals = new HashMap<>();
    private Map<Integer, Integer> animalResources = new HashMap<>();
    private Map<Integer, String> animalNames = new HashMap<>();
    private Map<Integer, View> animalViews = new HashMap<>();
    
    private static final int MAX_SELECTIONS = 3; // Maximum number of animals that can be selected
    private int currentPoints = 1000; // Default starting points only used if no points are passed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Force landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_betting);
        
        // Initialize views
        etBetAmount = findViewById(R.id.etBetAmount);
        tvCurrentPoints = findViewById(R.id.tvCurrentPoints);
        btnPlaceBet = findViewById(R.id.btnPlaceBet);
        gridAnimals = findViewById(R.id.gridAnimals);
        ImageButton btnStore = findViewById(R.id.btnStore);
        
        // Get current points from intent if available
        if (getIntent().hasExtra("currentPoints")) {
            currentPoints = getIntent().getIntExtra("currentPoints", currentPoints);
        }
        
        // Initialize audio player AFTER getting intent extras
        boolean fromGame = getIntent().getBooleanExtra("from_game", false);
        // If coming from game, audio will be initialized in onResume
        if (!fromGame) {
            initializeMediaPlayer();
        }
        
        // Set current points
        updatePointsDisplay();
        
        // Set up store button
        btnStore.setOnClickListener(v -> {
            // Navigate to store activity
            Intent intent = new Intent(BettingActivity.this, StoreActivity.class);
            intent.putExtra("currentPoints", currentPoints);
            startActivity(intent);
        });
    }
    

    
    /**
     * Updates the points display with the current value
     */
    private void updatePointsDisplay() {
        tvCurrentPoints.setText("Điểm: " + currentPoints);
        
        // Setup animal resources and names
        setupAnimalData();
        
        // Setup animal grid
        setupAnimalGrid();
        
        // Set up place bet button
        btnPlaceBet.setOnClickListener(v -> placeBet());
    }
    
    private void setupAnimalData() {
        // Add all animal drawable resources with their IDs
        animalResources.put(0, R.drawable.bee);
        animalResources.put(1, R.drawable.puppy);
        animalResources.put(2, R.drawable.dolphin);
        animalResources.put(3, R.drawable.frog);
        animalResources.put(4, R.drawable.seal);
        animalResources.put(5, R.drawable.snail);
        animalResources.put(6, R.drawable.turtle);
        animalResources.put(7, R.drawable.mouse);
        animalResources.put(8, R.drawable.squirl);
        
        // Add animal names
        animalNames.put(0, "Ong");
        animalNames.put(1, "Chó con");
        animalNames.put(2, "Cá heo");
        animalNames.put(3, "Ếch");
        animalNames.put(4, "Hải cẩu");
        animalNames.put(5, "Ốc sên");
        animalNames.put(6, "Rùa");
        animalNames.put(7, "Chuột");
        animalNames.put(8, "Sóc");
        
        // Initialize all animals as unselected
        for (int i = 0; i < animalResources.size(); i++) {
            selectedAnimals.put(i, false);
        }
    }
    
    private void setupAnimalGrid() {
        // Set column count for the grid
        gridAnimals.setColumnCount(3);
        gridAnimals.setUseDefaultMargins(true);
        
        // Create image buttons for each animal
        for (int i = 0; i < animalResources.size(); i++) {
            final int animalId = i;
            
            // Create container view for the animal
            View animalView = getLayoutInflater().inflate(R.layout.item_animal, gridAnimals, false);
            animalViews.put(animalId, animalView);
            
            // Get views from the inflated layout
            ImageView ivAnimal = animalView.findViewById(R.id.ivAnimal);
            TextView tvAnimalName = animalView.findViewById(R.id.tvAnimalName);
            
            // Set animal image and name
            ivAnimal.setImageResource(animalResources.get(animalId));
            tvAnimalName.setText(animalNames.get(animalId));
            
            // Set initial alpha
            ivAnimal.setAlpha(0.5f);
            
            // Set click listener for the entire view
            animalView.setOnClickListener(v -> {
                // Get current state
                boolean currentlySelected = selectedAnimals.get(animalId);
                
                // If trying to select and already at maximum, show message and return
                if (!currentlySelected && countSelectedAnimals() >= MAX_SELECTIONS) {
                    Toast.makeText(BettingActivity.this, 
                            "Chỉ được chọn tối đa " + MAX_SELECTIONS + " nhân vật", 
                            Toast.LENGTH_SHORT).show();
                    // Đảm bảo âm thanh vẫn tiếp tục phát sau khi hiển thị thông báo
                    AudioManager.getInstance().resumeMusic();
                    return;
                }
                
                // Toggle selection
                boolean isSelected = !currentlySelected;
                selectedAnimals.put(animalId, isSelected);
                
                // Update visual appearance
                ivAnimal.setAlpha(isSelected ? 1.0f : 0.5f);
                
                // Add or remove border based on selection
                ivAnimal.setBackground(isSelected ? 
                        ContextCompat.getDrawable(BettingActivity.this, R.drawable.selected_animal_background) : null);
                
                // Update the enabled state of all animal views based on selection count
                updateAnimalViewsState();
                
                // Extra check to ensure audio doesn't get stopped
                new Handler().postDelayed(() -> {
                    AudioManager.getInstance().resumeMusic();
                }, 500);
            });
            
            // Set parameters for grid layout
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.setGravity(android.view.Gravity.CENTER);
            animalView.setLayoutParams(param);
            
            // Add to grid
            gridAnimals.addView(animalView);
        }
    }
    
    private void placeBet() {
        // Check if at least one animal is selected
        boolean hasSelection = false;
        for (Boolean isSelected : selectedAnimals.values()) {
            if (isSelected) {
                hasSelection = true;
                break;
            }
        }
        
        if (!hasSelection) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một nhân vật", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String betAmountStr = etBetAmount.getText().toString().trim();
        if (betAmountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điểm cược", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int betAmount;
        try {
            betAmount = Integer.parseInt(betAmountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số điểm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (betAmount <= 0) {
            Toast.makeText(this, "Số điểm cược phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (betAmount > currentPoints) {
            Toast.makeText(this, "Không đủ điểm để đặt cược", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create boolean array of selected animals
        boolean[] selectedAnimalsArray = new boolean[animalResources.size()];
        for (int i = 0; i < animalResources.size(); i++) {
            selectedAnimalsArray[i] = selectedAnimals.get(i);
        }
        
        // Start the game with bet information
        Intent intent = new Intent(BettingActivity.this, MainActivity.class);
        intent.putExtra("selectedAnimals", selectedAnimalsArray);
        intent.putExtra("betAmount", betAmount);
        intent.putExtra("currentPoints", currentPoints);
        startActivity(intent);
        
        
    }
    
    /**
     * Counts the number of currently selected animals
     * @return Number of selected animals
     */
    private int countSelectedAnimals() {
        int count = 0;
        for (Boolean isSelected : selectedAnimals.values()) {
            if (isSelected) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Updates the enabled state of all animal views based on selection count
     */
    private void updateAnimalViewsState() {
        int selectedCount = countSelectedAnimals();
        
        // If at max selections, disable all unselected animals
        for (int i = 0; i < animalResources.size(); i++) {
            View animalView = animalViews.get(i);
            ImageView ivAnimal = animalView.findViewById(R.id.ivAnimal);
            boolean isSelected = selectedAnimals.get(i);
            
            // Only disable unselected items when at max selections
            if (selectedCount >= MAX_SELECTIONS && !isSelected) {
                animalView.setEnabled(false);
                ivAnimal.setAlpha(0.3f); // More faded to show disabled state
            } else {
                animalView.setEnabled(true);
                ivAnimal.setAlpha(isSelected ? 1.0f : 0.5f); // Normal alpha for selected/unselected
            }
        }
    }
    
    /**
     * Initializes the background music
     */
    private void initializeMediaPlayer() {
        AudioManager.getInstance().playMusic(this, AudioManager.BETTING_MUSIC, true, true);
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
     * Resumes the background music and updates points when activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        
        // Check for updated points in intent (may happen when returning from rounds)
        if (getIntent().hasExtra("currentPoints")) {
            currentPoints = getIntent().getIntExtra("currentPoints", currentPoints);
            updatePointsDisplay();
        }
        
        AudioManager.getInstance().resumeMusic();
    }
} 