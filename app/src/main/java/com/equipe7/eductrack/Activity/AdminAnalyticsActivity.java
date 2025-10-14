package com.equipe7.eductrack.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.equipe7.eductrack.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private TextView tvTotalUsers, tvActiveToday, tvTeachersCount, tvParentsCount, tvStudentsCount;
    private TextView tvDbStatus, tvLastSync;
    private ImageView btnBack;
    
    private FirebaseFirestore db;
    
    private int totalUsers = 0;
    private int teachersCount = 0;
    private int parentsCount = 0;
    private int studentsCount = 0;
    private int activeToday = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();
        
        // Setup click listeners
        btnBack.setOnClickListener(v -> finish());
        
        // Load analytics data
        loadAnalyticsData();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvActiveToday = findViewById(R.id.tvActiveToday);
        tvTeachersCount = findViewById(R.id.tvTeachersCount);
        tvParentsCount = findViewById(R.id.tvParentsCount);
        tvStudentsCount = findViewById(R.id.tvStudentsCount);
        tvDbStatus = findViewById(R.id.tvDbStatus);
        tvLastSync = findViewById(R.id.tvLastSync);
    }

    private void loadAnalyticsData() {
        // Show loading state
        tvTotalUsers.setText("...");
        tvTeachersCount.setText("...");
        tvParentsCount.setText("...");
        tvStudentsCount.setText("...");
        tvActiveToday.setText("...");
        
        // Load users from Firebase
        db.collection("users")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Reset counts
                totalUsers = 0;
                teachersCount = 0;
                parentsCount = 0;
                studentsCount = 0;
                activeToday = 0;
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    totalUsers++;
                    
                    // Count by role
                    String role = document.getString("role");
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
                }
                
                // Simulate active users (30% of total for demo)
                activeToday = (int) (totalUsers * 0.3);
                
                // Update UI
                updateUI();
                
                // Update database status
                tvDbStatus.setText("● Connected");
                tvDbStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                
                // Update last sync time
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                tvLastSync.setText("Last sync: " + sdf.format(new Date()));
                
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load analytics: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                
                // Update database status
                tvDbStatus.setText("● Disconnected");
                tvDbStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                
                // Set counts to 0
                updateUI();
            });
    }

    private void updateUI() {
        tvTotalUsers.setText(String.valueOf(totalUsers));
        tvTeachersCount.setText(String.valueOf(teachersCount));
        tvParentsCount.setText(String.valueOf(parentsCount));
        tvStudentsCount.setText(String.valueOf(studentsCount));
        tvActiveToday.setText(String.valueOf(activeToday));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity
        loadAnalyticsData();
    }
}
