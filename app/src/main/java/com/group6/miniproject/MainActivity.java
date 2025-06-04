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

public class MainActivity extends AppCompatActivity {

    private CheckBox checkBox1, checkBox2, checkBox3;
    private SeekBar seekBar1, seekBar2, seekBar3;
    private ImageView lineImageView;
    private TextView tvBetInfo;
    
    private int selectedCharacter = -1; // -1: none, 0: mouse, 1: puppy, 2: turtle
    private int betAmount = 0;
    private int currentPoints = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Force landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_round);
        
        // Get betting information from intent
        if (getIntent().hasExtra("selectedCharacter")) {
            selectedCharacter = getIntent().getIntExtra("selectedCharacter", -1);
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
        
        // Display bet information
        updateBetInfo();
        
        // Remove text from checkboxes
        checkBox1.setText("");
        checkBox2.setText("");
        checkBox3.setText("");
        
        // Preserve original thumb sizes and positions
        preserveThumbAppearance(seekBar1);
        preserveThumbAppearance(seekBar2);
        preserveThumbAppearance(seekBar3);
        
        // Set up checkbox and seekbar connections
        setupCheckboxAndSeekbar(checkBox1, seekBar1);
        setupCheckboxAndSeekbar(checkBox2, seekBar2);
        setupCheckboxAndSeekbar(checkBox3, seekBar3);
        
        // Make checkboxes visible (in case they're not)
        checkBox1.setVisibility(View.VISIBLE);
        checkBox2.setVisibility(View.VISIBLE);
        checkBox3.setVisibility(View.VISIBLE);
        
        // Auto-select the character based on betting selection
        if (selectedCharacter != -1) {
            CheckBox selectedCheckBox = null;
            switch (selectedCharacter) {
                case 0: // mouse
                    selectedCheckBox = checkBox1;
                    break;
                case 1: // puppy
                    selectedCheckBox = checkBox2;
                    break;
                case 2: // turtle
                    selectedCheckBox = checkBox3;
                    break;
            }
            
            if (selectedCheckBox != null) {
                selectedCheckBox.setChecked(true);
            }
        }
    }
    
    private void updateBetInfo() {
        if (tvBetInfo != null && betAmount > 0) {
            String characterName = "Không xác định";
            switch (selectedCharacter) {
                case 0:
                    characterName = "Chuột";
                    break;
                case 1:
                    characterName = "Chó con";
                    break;
                case 2:
                    characterName = "Rùa";
                    break;
            }
            
            tvBetInfo.setText("Đặt cược: " + betAmount + " điểm cho " + characterName);
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
                
                // Nếu là seekBar3 (rùa), thêm hiệu ứng đường kẻ chạy
                if (seekBar == seekBar3) {
                    animateLineEffect(seekBar);
                }
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
    
    // Phương thức tạo hiệu ứng đường kẻ chạy
    private void animateLineEffect(final SeekBar seekBar) {

        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(seekBar, "progress", 0, 70);
        progressAnimator.setDuration(3000); // 3 giây
        progressAnimator.start();
        
        // Nếu có hình ảnh đường kẻ, thêm hiệu ứng cho nó
        if (lineImageView != null) {
            // Tạo hiệu ứng alpha (mờ dần hiện rõ)
            ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0.3f, 1.0f);
            alphaAnimator.setDuration(3000);
            alphaAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                lineImageView.setAlpha(value);
            });
            alphaAnimator.start();
        }
    }
}