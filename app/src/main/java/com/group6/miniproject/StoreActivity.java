package com.group6.miniproject;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.view.Window;

public class StoreActivity extends AppCompatActivity {

    private TextView tvStorePoints;
    private Button btnSubmitOrder, btnBackToBetting, btnOpenRewardDialog;
    private LinearLayout orderItemsContainer;
    private TextView tvTotalCost;
    

    
    private int currentPoints = 1000;
    
    // Prices for items
    private static final int PHO_PRICE = 200;
    private static final int BUNBO_PRICE = 250;
    private static final int COCA_PRICE = 100;
    private static final int PEPSI_PRICE = 100;
    
    // Order tracking
    private Map<String, Integer> orderItems = new HashMap<>();
    private int totalCost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Force landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_store);
        
        // Initialize audio player
        initializeMediaPlayer();
        
        // Initialize views
        initViews();
        
        // Get current points from intent
        if (getIntent().hasExtra("currentPoints")) {
            currentPoints = getIntent().getIntExtra("currentPoints", currentPoints);
            updatePointsDisplay();
        }
        
        // Set up food and drink item click listeners
        setupItemListeners();
        
        // Make sure food list is visible by default
        if (orderItemsContainer != null) {
            orderItemsContainer.setVisibility(View.VISIBLE);
        }
        
        // Thêm xử lý nút Đổi thưởng
        btnOpenRewardDialog = findViewById(R.id.btnOpenRewardDialog);
        btnOpenRewardDialog.setOnClickListener(v -> showRewardDialog());
    }
    
    private void initViews() {
        tvStorePoints = findViewById(R.id.tvStorePoints);
        btnSubmitOrder = findViewById(R.id.btnSubmitOrder);
        btnBackToBetting = findViewById(R.id.btnBackToBetting);
        orderItemsContainer = findViewById(R.id.orderItemsContainer);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        // Gán sự kiện cho nút Đặt hàng
        btnSubmitOrder.setOnClickListener(v -> submitOrder());
        // Gán sự kiện cho nút quay lại đặt cược
        btnBackToBetting.setOnClickListener(v -> {
            Intent intent = new Intent(StoreActivity.this, BettingActivity.class);
            intent.putExtra("currentPoints", currentPoints);
            startActivity(intent);
            finish();
        });
    }
    
    private void setupItemListeners() {

    }
    
    private void addToOrder(String itemName, int price) {
        // Check if item already exists in order
        if (orderItems.containsKey(itemName)) {
            // Increment quantity
            int quantity = orderItems.get(itemName) + 1;
            orderItems.put(itemName, quantity);
        } else {
            // Add new item
            orderItems.put(itemName, 1);
        }
        // Update total cost
        totalCost += price;

    }
    
    private void updateOrderList() {
        // Clear current order list
        orderItemsContainer.removeAllViews();
        
        // Add each item to the order list
        for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();
            int itemPrice = getItemPrice(itemName);
            
            // Create order item view
            View orderItemView = LayoutInflater.from(this).inflate(R.layout.item_order, orderItemsContainer, false);
            
            // Set item details
            TextView tvItemName = orderItemView.findViewById(R.id.tvOrderItemName);
            TextView tvItemQuantity = orderItemView.findViewById(R.id.tvOrderItemQuantity);
            TextView tvItemPrice = orderItemView.findViewById(R.id.tvOrderItemPrice);
            Button btnRemove = orderItemView.findViewById(R.id.btnRemoveItem);
            
            tvItemName.setText(itemName);
            tvItemQuantity.setText("x" + quantity);
            tvItemPrice.setText(itemPrice * quantity + " điểm");
            
            // Set remove button listener
            btnRemove.setOnClickListener(v -> removeFromOrder(itemName, itemPrice));
            
            // Add to container
            orderItemsContainer.addView(orderItemView);
        }
    }
    
    private void removeFromOrder(String itemName, int price) {
        if (orderItems.containsKey(itemName)) {
            int quantity = orderItems.get(itemName);
            
            if (quantity > 1) {
                // Decrement quantity
                orderItems.put(itemName, quantity - 1);
            } else {
                // Remove item completely
                orderItems.remove(itemName);
            }
            
            // Update total cost
            totalCost -= price;
            
            // Update UI
            updateOrderList();
            updateTotalCost();
            
            Toast.makeText(this, "Đã xóa 1 " + itemName + " khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
        }
    }
    
    private int getItemPrice(String itemName) {
        switch (itemName) {
            case "Phở bò":
                return PHO_PRICE;
            case "Bún bò Huế":
                return BUNBO_PRICE;
            case "Coca Cola":
                return COCA_PRICE;
            case "Pepsi":
                return PEPSI_PRICE;
            default:
                return 0;
        }
    }
    
    private void updateTotalCost() {
        tvTotalCost.setText("Tổng: " + totalCost + " điểm");
    }
    
    private void submitOrder() {
        if (orderItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một món", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (totalCost > currentPoints) {
            Toast.makeText(this, "Không đủ điểm! Bạn cần thêm " + (totalCost - currentPoints) + " điểm.", 
                    Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận đặt hàng");
        
        StringBuilder message = new StringBuilder("Bạn có muốn đặt các món sau?\n\n");
        for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
            message.append(entry.getKey())
                   .append(" x")
                   .append(entry.getValue())
                   .append("\n");
        }
        message.append("\nTổng: ").append(totalCost).append(" điểm");
        
        builder.setMessage(message.toString());
        
        builder.setPositiveButton("Đồng ý", (dialog, which) -> {
            // Deduct points
            currentPoints -= totalCost;
            updatePointsDisplay();
            
            // Show success message
            Toast.makeText(StoreActivity.this, 
                    "Đặt hàng thành công! Bạn còn " + currentPoints + " điểm.", 
                    Toast.LENGTH_SHORT).show();
            
            // Clear order
            orderItems.clear();
            totalCost = 0;
            updateOrderList();
            updateTotalCost();
        });
        
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        
        builder.show();
    }
    
    private void updatePointsDisplay() {
        tvStorePoints.setText("Điểm: " + currentPoints);
    }

    private void showRewardDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reward_list);
        dialog.setCancelable(true);

        // Đặt width dialog gần bằng width màn hình (landscape-friendly)
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.9),
                             android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }

        LinearLayout dialogItemsContainer = dialog.findViewById(R.id.dialogItemsContainer);
        Button btnDialogCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnDialogConfirm = dialog.findViewById(R.id.btnDialogConfirm);

        // Danh sách món mẫu
        RewardItem[] items = new RewardItem[] {
            new RewardItem("Phở bò", R.drawable.pho_bo, PHO_PRICE),
            new RewardItem("Bún bò Huế", R.drawable.bun_bo_hue, BUNBO_PRICE),
            new RewardItem("Coca Cola", R.drawable.coca, COCA_PRICE),
            new RewardItem("Pepsi", R.drawable.pepsi, PEPSI_PRICE)
        };
        // Tạm lưu số lượng chọn
        Map<String, Integer> tempSelected = new HashMap<>();
        for (RewardItem item : items) tempSelected.put(item.name, 0);

        // Inflate từng item
        for (RewardItem item : items) {
            View itemView = getLayoutInflater().inflate(R.layout.item_reward, dialogItemsContainer, false);
            ImageView img = itemView.findViewById(R.id.imgItem);
            TextView tv = itemView.findViewById(R.id.tvItemName);
            img.setImageResource(item.drawableRes);
            tv.setText(item.name + "\n" + item.price + " điểm");
            // Click vào ảnh để tăng số lượng chọn
            img.setOnClickListener(v -> {
                int current = tempSelected.get(item.name);
                tempSelected.put(item.name, current + 1);
                Toast.makeText(this, "Đã chọn " + item.name + " x" + (current + 1), Toast.LENGTH_SHORT).show();
            });
            dialogItemsContainer.addView(itemView);
        }

        btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        btnDialogConfirm.setOnClickListener(v -> {
            // Thêm các món đã chọn vào order
            for (RewardItem item : items) {
                int qty = tempSelected.get(item.name);
                for (int i = 0; i < qty; i++) {
                    addToOrder(item.name, item.price);
                }
            }
            // Cập nhật giao diện ngay sau khi chọn
            updateOrderList();
            updateTotalCost();
            dialog.dismiss();
        });
        dialog.show();
    }

    // RewardItem class
    private static class RewardItem {
        String name;
        int drawableRes;
        int price;
        RewardItem(String name, int drawableRes, int price) {
            this.name = name;
            this.drawableRes = drawableRes;
            this.price = price;
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
     * Resumes the background music when activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        AudioManager.getInstance().resumeMusic();
    }
    
    // No need for onDestroy handling as the AudioManager is now shared
} 