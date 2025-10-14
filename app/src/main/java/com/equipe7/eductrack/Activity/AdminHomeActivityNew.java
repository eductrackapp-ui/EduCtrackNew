package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.adapters.RecentUserAdapter;
import com.equipe7.eductrack.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminHomeActivityNew extends AppCompatActivity {

    // UI Elements
    private TextView tvGreeting, tvAdminName;
    private TextView tvTotalUsers, tvOnlineUsers;
    private TextView tvTeachersCount, tvParentsCount, tvStudentsCount;
    private ImageView btnSettings;
    private CardView btnManageUsers, btnViewAnalytics;
    private TextView btnViewAll;
    private RecyclerView rvRecentUsers;
    private LinearLayout btnHome, btnUsers, btnReports;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    // Data
    private List<UserModel> allUsers = new ArrayList<>();
    private List<UserModel> recentUsers = new ArrayList<>();
    private RecentUserAdapter recentUserAdapter;
    
    // Counts
    private int totalUsers = 0;
    private int teachersCount = 0;
    private int parentsCount = 0;
    private int studentsCount = 0;
    private int onlineCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_home_activity_professional);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        
        // Setup click listeners
        setupClickListeners();
        
        // Load admin data
        loadAdminProfile();
        
        // Load all users data from Firebase
        loadUsersFromFirebase();
    }

    private void initializeViews() {
        // Header
        tvGreeting = findViewById(R.id.tvGreeting);
        tvAdminName = findViewById(R.id.tvAdminName);
        btnSettings = findViewById(R.id.btnSettings);
        
        // Stats
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvOnlineUsers = findViewById(R.id.tvOnlineUsers);
        tvTeachersCount = findViewById(R.id.tvTeachersCount);
        tvParentsCount = findViewById(R.id.tvParentsCount);
        tvStudentsCount = findViewById(R.id.tvStudentsCount);
        
        // Action buttons
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnViewAnalytics = findViewById(R.id.btnViewAnalytics);
        btnViewAll = findViewById(R.id.btnViewAll);
        
        // Recent users RecyclerView
        rvRecentUsers = findViewById(R.id.rvRecentUsers);
        rvRecentUsers.setLayoutManager(new LinearLayoutManager(this));
        recentUserAdapter = new RecentUserAdapter(recentUsers, this);
        rvRecentUsers.setAdapter(recentUserAdapter);
        
        // Bottom navigation
        btnHome = findViewById(R.id.btnHome);
        btnUsers = findViewById(R.id.btnUsers);
        btnReports = findViewById(R.id.btnReports);
        
        // Set greeting based on time
        setGreeting();
    }

    private void setGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        if (hour < 12) {
            tvGreeting.setText("Good Morning");
        } else if (hour < 18) {
            tvGreeting.setText("Good Afternoon");
        } else {
            tvGreeting.setText("Good Evening");
        }
    }

    private void setupClickListeners() {
        // Settings
        btnSettings.setOnClickListener(v -> openSettings());
        
        // Manage Users
        btnManageUsers.setOnClickListener(v -> openUserManagement());
        
        // Analytics
        btnViewAnalytics.setOnClickListener(v -> openAnalytics());
        
        // View all recent users
        btnViewAll.setOnClickListener(v -> openUserManagement());
        
        // Bottom navigation
        btnHome.setOnClickListener(v -> {
            // Already on home
            Toast.makeText(this, "You're on Home", Toast.LENGTH_SHORT).show();
        });
        
        btnUsers.setOnClickListener(v -> openUserManagement());
        
        btnReports.setOnClickListener(v -> openAnalytics());
    }

    private void loadAdminProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
            // Load admin profile from Firestore
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvAdminName.setText(name);
                        } else {
                            tvAdminName.setText("Admin");
                        }
                    } else {
                        tvAdminName.setText("Admin");
                    }
                })
                .addOnFailureListener(e -> {
                    tvAdminName.setText("Admin");
                });
        } else {
            tvAdminName.setText("Admin");
        }
    }

    private void loadUsersFromFirebase() {
        // Show loading
        tvTotalUsers.setText("...");
        tvTeachersCount.setText("...");
        tvParentsCount.setText("...");
        tvStudentsCount.setText("...");
        
        db.collection("users")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                allUsers.clear();
                recentUsers.clear();
                
                // Reset counts
                totalUsers = 0;
                teachersCount = 0;
                parentsCount = 0;
                studentsCount = 0;
                onlineCount = 0;
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    UserModel user = new UserModel();
                    user.setName(document.getString("name"));
                    user.setEmail(document.getString("email"));
                    user.setRole(document.getString("role"));
                    user.setSubject(document.getString("subject"));
                    user.setAssignedClass(document.getString("assignedClass"));
                    user.setCode(document.getString("code"));
                    
                    allUsers.add(user);
                    totalUsers++;
                    
                    // Count by role
                    String role = user.getRole();
                    if (role != null) {
                        switch (role.toLowerCase()) {
                            case "teacher":
                                teachersCount++;
                                break;
                            case "parent":
                                parentsCount++;
                                break;
                            case "student":
                                studentsCount++;
                                break;
                        }
                    }
                    
                    // Add to recent users (last 5)
                    if (recentUsers.size() < 5) {
                        recentUsers.add(user);
                    }
                }
                
                // Simulate online users (you can implement real presence tracking)
                onlineCount = (int) (totalUsers * 0.3); // 30% online for demo
                
                // Update UI
                updateUI();
                
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load users: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                // Set to 0 on failure
                updateUI();
            });
    }

    private void updateUI() {
        tvTotalUsers.setText(String.valueOf(totalUsers));
        tvOnlineUsers.setText(String.valueOf(onlineCount));
        tvTeachersCount.setText(String.valueOf(teachersCount));
        tvParentsCount.setText(String.valueOf(parentsCount));
        tvStudentsCount.setText(String.valueOf(studentsCount));
        
        // Update RecyclerView
        recentUserAdapter.updateData(recentUsers);
    }

    private void openUserManagement() {
        Intent intent = new Intent(this, AdminUserManagementActivityEnhanced.class);
        startActivity(intent);
    }

    private void openAnalytics() {
        Intent intent = new Intent(this, AdminAnalyticsActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(this, AdminSettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity
        loadUsersFromFirebase();
    }
}
