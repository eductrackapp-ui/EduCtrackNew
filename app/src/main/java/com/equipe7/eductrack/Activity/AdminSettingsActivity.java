package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import com.equipe7.eductrack.Auth.UnifiedAuthActivity;
import com.equipe7.eductrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class AdminSettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView cardProfile, cardChangePassword, cardNotifications, cardClearCache;
    private SwitchCompat switchNotifications;
    private MaterialButton btnLogout;
    
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        
        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        cardProfile = findViewById(R.id.cardProfile);
        cardChangePassword = findViewById(R.id.cardChangePassword);
        cardNotifications = findViewById(R.id.cardNotifications);
        cardClearCache = findViewById(R.id.cardClearCache);
        switchNotifications = findViewById(R.id.switchNotifications);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());
        
        // Profile
        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });
        
        // Change Password
        cardChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });
        
        // Notifications switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, 
                isChecked ? "Notifications enabled" : "Notifications disabled", 
                Toast.LENGTH_SHORT).show();
        });
        
        // Clear Cache
        cardClearCache.setOnClickListener(v -> {
            showClearCacheDialog();
        });
        
        // Logout
        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void showChangePasswordDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setMessage("A password reset link will be sent to your email address.")
            .setPositiveButton("Send Link", (dialog, which) -> {
                if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                    mAuth.sendPasswordResetEmail(mAuth.getCurrentUser().getEmail())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to send email: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Clear Cache")
            .setMessage("This will clear all cached data. Are you sure?")
            .setPositiveButton("Clear", (dialog, which) -> {
                // Clear cache
                try {
                    deleteCache(getCacheDir());
                    Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to clear cache", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                // Logout
                mAuth.signOut();
                
                // Navigate to login
                Intent intent = new Intent(this, UnifiedAuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteCache(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    deleteCache(new java.io.File(dir, child));
                }
            }
        }
        if (dir != null) {
            dir.delete();
        }
    }
}
