package com.equipe7.eductrack.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.equipe7.eductrack.Adapter.CarouselAdapter;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.CarouselItem;
import com.equipe7.eductrack.models.UserModel;
import com.equipe7.eductrack.models.AdminAnalytics;
import com.equipe7.eductrack.models.UserManagement;
import com.equipe7.eductrack.Firebase.AdminManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity {

    // Header
    private ImageView navNotifications, navProfile;
    private TextView badgeNotifications;

    // Search
    private EditText etSearchAdmin;

    // Carousel
    private ViewPager2 viewPagerCarousel;
    private WormDotsIndicator carouselIndicators;
    private CarouselAdapter carouselAdapter;

    // Cards
    private LinearLayout btnTeachers, btnStudents, btnAnalytics;
    private CardView btnViewAllUsers, btnSystemHealth, btnReports;

    // Dashboard Analytics
    private TextView totalUsersCount, totalTeachersCount, totalParentsCount, totalStudentsCount;
    private TextView systemAverage, activeUsersCount, systemStatus;
    private RecyclerView recentUsersRecycler;

    // Bottom nav
    private LinearLayout btnDashboard, btnHome;

    // Firebase and Admin Management
    private FirebaseFirestore db;
    private AdminManager adminManager;
    private AdminAnalytics currentAnalytics;
    private List<UserManagement> recentUsers = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_home_activity_enhanced);

        // --------------------------
        // Initialisation des vues
        // --------------------------
        // Comment out old layout elements that don't exist in enhanced layout
        /*
        navNotifications = findViewById(R.id.navNotifications);
        navProfile = findViewById(R.id.navProfile);
        badgeNotifications = findViewById(R.id.badgeNotifications);

        etSearchAdmin = findViewById(R.id.etSearchAdmin);

        viewPagerCarousel = findViewById(R.id.viewPagerCarousel);
        carouselIndicators = findViewById(R.id.carouselIndicators);

        btnTeachers = findViewById(R.id.btnTeachers);
        btnStudents = findViewById(R.id.btnStudents);
        btnAnalytics = findViewById(R.id.btnAnalytics);

        btnDashboard = findViewById(R.id.btnDashboard);
        */
        btnHome = findViewById(R.id.btnHome);

        db = FirebaseFirestore.getInstance();
        adminManager = new AdminManager();

        // Show simple toast to confirm admin portal works
        Toast.makeText(this, "Welcome to Admin Dashboard", Toast.LENGTH_SHORT).show();

        // --------------------------
        // Header Click Listeners
        // --------------------------
        /*
        navNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, AdminNotification.class))
        );

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, AdminProfileActivity.class))
        );
        */

        // --------------------------
        // Bottom Navigation Clicks
        // --------------------------
        /*
        btnDashboard.setOnClickListener(v ->
                Toast.makeText(this, "Already on Dashboard", Toast.LENGTH_SHORT).show()
        );
        */

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }

        // --------------------------
        // Cards Click Listeners
        // --------------------------
        /*
        btnTeachers.setOnClickListener(v ->
                startActivity(new Intent(this, AdminCreateTeacherActivity.class))
        );

        btnStudents.setOnClickListener(v ->
                startActivity(new Intent(this, AdminStudentDashboardActivity.class))
        );

        btnAnalytics.setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsDashboard.class))
        );
        */

        // --------------------------
        // Search Filter (cherche par NOM uniquement)
        // --------------------------
        /*
        etSearchAdmin.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() > 2) { // éviter de lancer la recherche trop tôt
                    searchUserByName(query);
                }
            }
        });
        */

        // --------------------------
        // Carousel Setup
        // --------------------------
        // setupCombinedCarousel();
    }

    // --------------------------
    // Search User in Firestore by NAME
    // --------------------------
    private void searchUserByName(String query) {
        db.collection("users")
                .whereEqualTo("name", query) // recherche uniquement par le champ "name"
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            UserModel user = doc.toObject(UserModel.class);

                            if (user != null) {
                                Intent intent = new Intent(AdminHomeActivity.this, UserProfileActivity.class);
                                intent.putExtra("userName", user.getName());
                                intent.putExtra("userEmail", user.getEmail());
                                intent.putExtra("userRole", user.getRole());
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(AdminHomeActivity.this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // --------------------------
    // Carousel Setup Method
    // --------------------------
    private void setupCombinedCarousel() {
        List<CarouselItem> combinedItems = new ArrayList<>();

        combinedItems.add(new CarouselItem(R.drawable.bebe_eleve, "Welcome", "Learning Together"));
        combinedItems.add(new CarouselItem(R.drawable.mission_image, "Our Mission", "Educate & Inspire"));
        combinedItems.add(new CarouselItem(R.drawable.eleve, "Students", "Motivation & Success"));
        combinedItems.add(new CarouselItem(R.drawable.eleve_png, "Education", "For Everyone"));
        combinedItems.add(new CarouselItem(R.drawable.groupe_eleve_deux, "Teamwork", "Together is Better"));
        combinedItems.add(new CarouselItem(R.drawable.c_eleve, "Trust", "Support & Respect"));
        combinedItems.add(new CarouselItem(R.drawable.charte, "Charter", "Values & Discipline"));
        combinedItems.add(new CarouselItem(R.drawable.c_ejeux, "Educational Games", "Learning Through Play"));

        carouselAdapter = new CarouselAdapter(combinedItems);
        viewPagerCarousel.setAdapter(carouselAdapter);
        carouselIndicators.setViewPager2(viewPagerCarousel);

        autoSlideCarousel(viewPagerCarousel, combinedItems.size());
    }

    // --------------------------
    // Auto Slide Method
    // --------------------------
    private void autoSlideCarousel(ViewPager2 carousel, int itemCount) {
        final int delay = 5000; // 5s
        carousel.postDelayed(new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if (itemCount > 0) {
                    index = (index + 1) % itemCount;
                    carousel.setCurrentItem(index, true);
                    carousel.postDelayed(this, delay);
                }
            }
        }, delay);
    }

    // --------------------------
    // ADMIN DASHBOARD METHODS
    // --------------------------
    
    private void initializeViews() {
        // Initialize enhanced admin dashboard views
        try {
            totalUsersCount = findViewById(R.id.totalUsersCount);
            totalTeachersCount = findViewById(R.id.totalTeachersCount);
            totalParentsCount = findViewById(R.id.totalParentsCount);
            totalStudentsCount = findViewById(R.id.totalStudentsCount);
            activeUsersCount = findViewById(R.id.activeUsersCount);
            systemStatus = findViewById(R.id.systemStatus);
            
            btnViewAllUsers = findViewById(R.id.btnViewAllUsers);
            btnSystemHealth = findViewById(R.id.btnSystemHealth);
            btnReports = findViewById(R.id.btnReports);
            
            recentUsersRecycler = findViewById(R.id.recentUsersRecycler);
            if (recentUsersRecycler != null) {
                recentUsersRecycler.setLayoutManager(new LinearLayoutManager(this));
            }
        } catch (Exception e) {
            // Views don't exist in enhanced layout
        }
    }
    
    private void setupClickListeners() {
        // Enhanced admin action buttons
        if (btnViewAllUsers != null) {
            btnViewAllUsers.setOnClickListener(v -> openUserManagement());
        }
        
        if (btnSystemHealth != null) {
            btnSystemHealth.setOnClickListener(v -> openSystemHealth());
        }
        
        if (btnReports != null) {
            btnReports.setOnClickListener(v -> openReportsCenter());
        }
    }
    
    private void loadAdminAnalytics() {
        try {
            adminManager.generateSystemAnalytics(new AdminManager.AdminCallback<AdminAnalytics>() {
                @Override
                public void onSuccess(AdminAnalytics analytics) {
                    currentAnalytics = analytics;
                    updateAnalyticsDisplay(analytics);
                }
                
                @Override
                public void onFailure(String error) {
                    // Silent failure - enhanced layout doesn't have all views
                }
            });
        } catch (Exception e) {
            // Silent failure
        }
    }
    
    private void updateAnalyticsDisplay(AdminAnalytics analytics) {
        if (totalUsersCount != null) totalUsersCount.setText(String.valueOf(analytics.getTotalUsers()));
        if (totalTeachersCount != null) totalTeachersCount.setText(String.valueOf(analytics.getTotalTeachers()));
        if (totalParentsCount != null) totalParentsCount.setText(String.valueOf(analytics.getTotalParents()));
        if (totalStudentsCount != null) totalStudentsCount.setText(String.valueOf(analytics.getTotalStudents()));
        if (systemAverage != null) systemAverage.setText(analytics.getSystemWideAverageDisplay());
        if (activeUsersCount != null) activeUsersCount.setText(String.valueOf(analytics.getActiveUsers()));
        if (systemStatus != null) {
            systemStatus.setText(analytics.getSystemStatus().toUpperCase());
            systemStatus.setTextColor(android.graphics.Color.parseColor(analytics.getSystemStatusColor()));
        }
    }
    
    private void createSampleAnalytics() {
        AdminAnalytics sampleAnalytics = new AdminAnalytics("current");
        sampleAnalytics.setTotalUsers(156);
        sampleAnalytics.setTotalTeachers(24);
        sampleAnalytics.setTotalParents(89);
        sampleAnalytics.setTotalStudents(42);
        sampleAnalytics.setTotalAdmins(1);
        sampleAnalytics.setActiveUsers(134);
        sampleAnalytics.setSystemWideAverage(78.5);
        sampleAnalytics.setSystemStatus("healthy");
        
        currentAnalytics = sampleAnalytics;
        updateAnalyticsDisplay(sampleAnalytics);
    }
    
    private void loadRecentUsers() {
        try {
            adminManager.getAllUsers(new AdminManager.AdminCallback<List<UserManagement>>() {
                @Override
                public void onSuccess(List<UserManagement> users) {
                    recentUsers.clear();
                    // Take first 5 users as recent
                    for (int i = 0; i < Math.min(5, users.size()); i++) {
                        recentUsers.add(users.get(i));
                    }
                    // Update RecyclerView adapter here if needed
                }
                
                @Override
                public void onFailure(String error) {
                    // Silent failure
                }
            });
        } catch (Exception e) {
            // Silent failure
        }
    }
    
    private void openUserManagement() {
        Intent intent = new Intent(this, AdminUserManagementActivity.class);
        startActivity(intent);
    }
    
    private void openSystemHealth() {
        // Placeholder - will be implemented later
        Toast.makeText(this, "System Health - Coming Soon", Toast.LENGTH_SHORT).show();
    }
    
    private void openReportsCenter() {
        // Placeholder - will be implemented later
        Toast.makeText(this, "Reports Center - Coming Soon", Toast.LENGTH_SHORT).show();
    }
    
    // Enhanced search with admin capabilities
    private void searchUserByNameEnhanced(String query) {
        adminManager.searchUsers(query, new AdminManager.AdminCallback<List<UserManagement>>() {
            @Override
            public void onSuccess(List<UserManagement> users) {
                if (!users.isEmpty()) {
                    // Open user management with search results
                    Intent intent = new Intent(AdminHomeActivity.this, AdminUserManagementActivity.class);
                    intent.putExtra("searchQuery", query);
                    startActivity(intent);
                } else {
                    Toast.makeText(AdminHomeActivity.this, "No users found matching: " + query, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminHomeActivity.this, "Search failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
