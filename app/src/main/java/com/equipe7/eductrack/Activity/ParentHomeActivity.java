package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.equipe7.eductrack.R;
import com.equipe7.eductrack.TrackModule.HomeworkActivity;
import com.equipe7.eductrack.TrackModule.StudentExercisesActivity;
import com.equipe7.eductrack.Activity.LessonsActivity;
import com.equipe7.eductrack.TrackModule.ParentsReportsActivity;
import com.equipe7.eductrack.Activity.StudentListActivity;
import com.equipe7.eductrack.TrackModule.StudentExamResultsActivity;
import com.equipe7.eductrack.Activity.NotificationActivity;
import com.equipe7.eductrack.Activity.ProfileActivity;
import com.equipe7.eductrack.models.Child;
import com.equipe7.eductrack.models.PerformanceData;
import com.equipe7.eductrack.Adapter.SubjectPerformanceAdapter;
import com.equipe7.eductrack.Auth.UnifiedAuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParentHomeActivity extends AppCompatActivity {

    public static class SearchableItem {
        public String type;
        public String name;
        public String code;
        public String className;

        public SearchableItem() {} // Nécessaire pour Firebase

        public SearchableItem(String type, String name, String code, String className) {
            this.type = type;
            this.name = name;
            this.code = code;
            this.className = className;
        }
    }

    private List<SearchableItem> database = new ArrayList<>();
    private DatabaseReference dbRef;
    private FirebaseFirestore firestore;
    private TextView tvWelcome, tvParentName, childrenCount;
    private TextView childName, childClass, childGrade, overallGrade, performanceStatus;
    private TextView weeklyAverage, monthlyAverage;
    private ImageView navProfile, navNotifications;
    private RecyclerView childrenRecyclerView, subjectPerformanceRecycler;
    private SubjectPerformanceAdapter subjectAdapter;
    private com.equipe7.eductrack.adapters.ChildCardAdapter childCardAdapter;
    private androidx.cardview.widget.CardView noChildrenCard;
    private List<Child> childrenList = new ArrayList<>();
    private List<PerformanceData> performanceList = new ArrayList<>();
    private Child currentChild;
    private String parentName = "";
    private String actualChildName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_home_activity);

        firestore = FirebaseFirestore.getInstance();
        initializeViews();
        setupClickListeners();
        loadParentAndChildData();
        loadDatabaseFromFirebase();
        setupPersonalizedGreeting();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvParentName = findViewById(R.id.tvParentName);
        childrenCount = findViewById(R.id.childrenCount);
        weeklyAverage = findViewById(R.id.weeklyAverage);
        monthlyAverage = findViewById(R.id.monthlyAverage);
        navProfile = findViewById(R.id.navProfile);
        navNotifications = findViewById(R.id.navNotifications);
        noChildrenCard = findViewById(R.id.noChildrenCard);
        
        // Initialize old single child views (for compatibility with old methods)
        // These will be null since they don't exist in new layout, but that's OK
        childName = findViewById(R.id.childName);
        childClass = findViewById(R.id.childClass);
        childGrade = findViewById(R.id.childGrade);
        overallGrade = findViewById(R.id.overallGrade);
        performanceStatus = findViewById(R.id.performanceStatus);
        
        // Initialize RecyclerView for children cards
        childrenRecyclerView = findViewById(R.id.childrenRecyclerView);
        childCardAdapter = new com.equipe7.eductrack.adapters.ChildCardAdapter(this, childrenList);
        childrenRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        childrenRecyclerView.setAdapter(childCardAdapter);
        
        // Initialize RecyclerView for subject performance
        subjectPerformanceRecycler = findViewById(R.id.subjectPerformanceRecycler);
        subjectAdapter = new SubjectPerformanceAdapter(this);
        subjectPerformanceRecycler.setLayoutManager(new LinearLayoutManager(this));
        subjectPerformanceRecycler.setAdapter(subjectAdapter);
    }

    private void setupClickListeners() {
        // Bottom navigation with safe navigation
        findViewById(R.id.btnExams).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, StudentExamResultsActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Opening Exams...", Toast.LENGTH_SHORT).show();
            }
        });
        
        findViewById(R.id.btnHomework).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, HomeworkActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Opening Homework...", Toast.LENGTH_SHORT).show();
            }
        });
        
        findViewById(R.id.btnReport).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, ParentsReportsActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Opening Reports...", Toast.LENGTH_SHORT).show();
            }
        });
        
        findViewById(R.id.btnExercise).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, StudentExercisesActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Opening Exercises...", Toast.LENGTH_SHORT).show();
            }
        });
        
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            // Already on home, just show toast
            Toast.makeText(this, "You're on Home", Toast.LENGTH_SHORT).show();
        });

        // Quick action buttons
        findViewById(R.id.btnViewReports).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, ParentsReportsActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Opening Reports...", Toast.LENGTH_SHORT).show();
            }
        });
        
        findViewById(R.id.btnViewHomework).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, HomeworkActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Opening Homework...", Toast.LENGTH_SHORT).show();
            }
        });
        
        findViewById(R.id.btnAddChild).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, AddChildActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Error opening Add Child screen", Toast.LENGTH_SHORT).show();
            }
        });

        // Header icons
        navNotifications.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, NotificationActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Notifications - Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });
        
        navProfile.setOnClickListener(v -> openProfileManagement());

        // Search functionality
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                searchInFirebase(query);
                return true;
            }
            return false;
        });
    }

    private void setupPersonalizedGreeting() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        int hour = Integer.parseInt(sdf.format(new Date()));
        
        String greeting;
        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }
        
        // Will be updated when parent data loads
        tvWelcome.setText(greeting + "!");
    }

    private void loadParentAndChildData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            
            // Load from Firestore first (new system)
            firestore.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            parentName = documentSnapshot.getString("name");
                            if (parentName != null && !parentName.isEmpty()) {
                                updatePersonalizedGreeting();
                                tvParentName.setText(parentName);
                            }
                            
                            // Load child information from Firestore
                            loadChildDataFromFirestore(uid);
                        } else {
                            // Fallback to Realtime Database
                            loadFromRealtimeDatabase(uid);
                        }
                    })
                    .addOnFailureListener(e -> loadFromRealtimeDatabase(uid));
        }
    }

    private void loadFromRealtimeDatabase(String uid) {
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        
        // Load parent information
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parentName = snapshot.child("name").getValue(String.class);
                if (parentName != null && !parentName.isEmpty()) {
                    updatePersonalizedGreeting();
                    tvParentName.setText(parentName);
                } else {
                    tvParentName.setText("Parent");
                }
                
                // Load child information
                loadChildData(uid);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvParentName.setText("Parent");
            }
        });
    }

    private void updatePersonalizedGreeting() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        int hour = Integer.parseInt(sdf.format(new Date()));
        
        String greeting;
        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }
        
        if (parentName != null && !parentName.isEmpty()) {
            tvWelcome.setText(greeting + ", " + parentName + "!");
        } else {
            tvWelcome.setText(greeting + "!");
        }
    }

    private void loadChildDataFromFirestore(String parentUid) {
        firestore.collection("children")
                .whereEqualTo("parentId", parentUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childrenList.clear();
                    
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (var childDoc : queryDocumentSnapshots.getDocuments()) {
                            Child child = new Child();
                            child.setChildId(childDoc.getId());
                            child.setName(childDoc.getString("name"));
                            child.setClassName(childDoc.getString("className"));
                            child.setGrade(childDoc.getString("grade"));
                            child.setBranch(childDoc.getString("branch"));
                            child.setStudentCode(childDoc.getString("studentCode"));
                            child.setParentId(childDoc.getString("parentId"));
                            
                            Double overallAvg = childDoc.getDouble("overallAverage");
                            if (overallAvg != null) {
                                child.setOverallAverage(overallAvg);
                            }
                            
                            String status = childDoc.getString("status");
                            if (status != null) {
                                child.setStatus(status);
                            }
                            
                            childrenList.add(child);
                        }
                        
                        updateChildrenDisplay();
                        
                        // Load performance data for first child if available
                        if (!childrenList.isEmpty()) {
                            loadChildPerformanceData(childrenList.get(0).getChildId());
                        }
                    } else {
                        updateChildrenDisplay();
                    }
                })
                .addOnFailureListener(e -> {
                    childrenList.clear();
                    updateChildrenDisplay();
                });
    }

    private void loadChildData(String parentId) {
        DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("children");
        childRef.orderByChild("parentId").equalTo(parentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                currentChild = childSnapshot.getValue(Child.class);
                                if (currentChild != null) {
                                    displayChildInfo(currentChild);
                                    loadPerformanceData(currentChild.getChildId());
                                    break; // Take first child for now
                                }
                            }
                        } else {
                            // Create sample child data for demonstration
                            createSampleChildData(parentId);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void displayChildInfo(Child child) {
        // Safe null checks for old UI elements that may not exist in new layout
        if (childName != null) childName.setText(child.getName());
        if (childClass != null) childClass.setText(child.getClassName());
        if (childGrade != null) childGrade.setText(child.getGrade());
        if (overallGrade != null) overallGrade.setText(child.getGradeDisplay());
        
        // Set performance status with color
        if (performanceStatus != null) {
            performanceStatus.setText(child.getStatus().toUpperCase());
            performanceStatus.setBackgroundColor(Color.parseColor(child.getStatusColor()));
        }
    }

    private void loadPerformanceData(String childId) {
        DatabaseReference perfRef = FirebaseDatabase.getInstance().getReference("performance");
        perfRef.orderByChild("childId").equalTo(childId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        performanceList.clear();
                        double weeklyTotal = 0, monthlyTotal = 0;
                        int weeklyCount = 0, monthlyCount = 0;
                        
                        for (DataSnapshot perfSnapshot : snapshot.getChildren()) {
                            PerformanceData perf = perfSnapshot.getValue(PerformanceData.class);
                            if (perf != null) {
                                performanceList.add(perf);
                                
                                // Calculate weekly and monthly averages
                                double percentage = perf.getPercentage();
                                weeklyTotal += percentage;
                                weeklyCount++;
                                monthlyTotal += percentage;
                                monthlyCount++;
                            }
                        }
                        
                        // Update UI with calculated averages
                        if (weeklyCount > 0) {
                            weeklyAverage.setText(String.format("%.1f%%", weeklyTotal / weeklyCount));
                        }
                        if (monthlyCount > 0) {
                            monthlyAverage.setText(String.format("%.1f%%", monthlyTotal / monthlyCount));
                        }
                        
                        // Update subject performance adapter
                        if (currentChild != null) {
                            subjectAdapter.updateData(currentChild.getCurrentGrades(), performanceList);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void createSampleChildDataOld(String parentId) {
        // Create sample child for demonstration (old method - keep for compatibility)
        Child sampleChild = new Child("child_001", "Emma Johnson", "6A", "Grade 6");
        sampleChild.setParentId(parentId);
        sampleChild.setOverallAverage(85.5);
        
        // Create sample subjects
        List<String> subjects = new ArrayList<>();
        subjects.add("Mathematics");
        subjects.add("English");
        subjects.add("Science");
        subjects.add("History");
        sampleChild.setSubjects(subjects);
        
        // Create sample grades
        Map<String, Double> grades = new HashMap<>();
        grades.put("Mathematics", 88.0);
        grades.put("English", 85.0);
        grades.put("Science", 82.0);
        grades.put("History", 87.0);
        sampleChild.setCurrentGrades(grades);
        
        currentChild = sampleChild;
        displayChildInfo(sampleChild);
        
        // Set sample performance data
        weeklyAverage.setText("78.5%");
        monthlyAverage.setText("82.3%");
        
        // Create sample performance data for subjects
        createSamplePerformanceDataOld();
        
        // Update subject adapter with sample data
        subjectAdapter.updateData(sampleChild.getCurrentGrades(), performanceList);
    }
    
    private void createSamplePerformanceDataOld() {
        performanceList.clear();
        
        // Create sample performance data for each subject
        String[] subjects = {"Mathematics", "English", "Science", "History"};
        String[] types = {"homework", "quiz", "exam", "exercise"};
        
        for (String subject : subjects) {
            for (int i = 0; i < 5; i++) {
                PerformanceData perf = new PerformanceData("child_001", subject, 
                    75 + (Math.random() * 25), // Random grade between 75-100
                    types[(int)(Math.random() * types.length)]);
                
                perf.setTitle("Assignment " + (i + 1));
                perf.setWeek("Week " + ((i % 4) + 1));
                perf.setMonth("October");
                perf.setSemester("Fall 2024");
                
                performanceList.add(perf);
            }
        }
    }

    // Recherche universelle dans Firebase
    private void searchInFirebase(String query) {
        String lowerQuery = query.toLowerCase();
        List<SearchableItem> results = new ArrayList<>();
        for (SearchableItem item : database) {
            if ((item.name != null && item.name.toLowerCase().contains(lowerQuery)) ||
                    (item.code != null && item.code.toLowerCase().contains(lowerQuery)) ||
                    (item.className != null && item.className.toLowerCase().contains(lowerQuery))) {
                results.add(item);
            }
        }
        // Passe les résultats à l'activité de résultats
        Intent intent = new Intent(this, SearchResultActivity.class);
        intent.putExtra("search_query", query);
        SearchResultActivity.results = results; // exemple simple
        startActivity(intent);
    }

    // Charger les données de recherche depuis Firebase
    private void loadDatabaseFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("searchable_items");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                database.clear();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    SearchableItem item = itemSnap.getValue(SearchableItem.class);
                    if (item != null) database.add(item);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void openProfileManagement() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Profile Management");
        
        String[] options = {"View Profile", "Edit Profile", "Settings", "Logout"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // View Profile
                    startActivity(new Intent(this, ProfileActivity.class));
                    break;
                case 1: // Edit Profile
                    startActivity(new Intent(this, ProfileActivity.class));
                    break;
                case 2: // Settings
                    // TODO: Create settings activity
                    android.widget.Toast.makeText(this, "Settings - Coming Soon", android.widget.Toast.LENGTH_SHORT).show();
                    break;
                case 3: // Logout
                    showLogoutConfirmation();
                    break;
            }
        });
        
        builder.show();
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, UnifiedAuthActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void createSampleChildData(String parentUid) {
        // Create sample child data for demonstration
        Map<String, Object> childData = new HashMap<>();
        childData.put("name", "Alex Johnson");
        childData.put("class", "Grade 5A");
        childData.put("grade", "5th Grade");
        childData.put("parentId", parentUid);
        childData.put("overallGrade", "85%");
        childData.put("status", "Good");
        
        firestore.collection("children")
                .add(childData)
                .addOnSuccessListener(documentReference -> {
                    actualChildName = "Alex Johnson";
                    if (childName != null) childName.setText(actualChildName);
                    if (childClass != null) childClass.setText("Grade 5A");
                    if (childGrade != null) childGrade.setText("5th Grade");
                    
                    // Create sample performance data
                    createSamplePerformanceData(documentReference.getId());
                });
    }

    private void createSamplePerformanceData(String childId) {
        // Create sample performance data
        if (overallGrade != null) overallGrade.setText("85%");
        if (performanceStatus != null) {
            performanceStatus.setText("Good");
            performanceStatus.setTextColor(Color.parseColor("#4CAF50"));
        }
        if (weeklyAverage != null) weeklyAverage.setText("87%");
        if (monthlyAverage != null) monthlyAverage.setText("83%");
        
        // Create sample subject performance
        performanceList.clear();
        performanceList.add(new PerformanceData("child_001", "Mathematics", 88.0, "exam"));
        performanceList.add(new PerformanceData("child_001", "English", 85.0, "exam"));
        performanceList.add(new PerformanceData("child_001", "Science", 82.0, "exam"));
        performanceList.add(new PerformanceData("child_001", "History", 80.0, "exam"));
        performanceList.add(new PerformanceData("child_001", "Geography", 86.0, "exam"));
        
        // Create sample grades map for adapter
        Map<String, Double> sampleGrades = new HashMap<>();
        sampleGrades.put("Mathematics", 88.0);
        sampleGrades.put("English", 85.0);
        sampleGrades.put("Science", 82.0);
        sampleGrades.put("History", 80.0);
        sampleGrades.put("Geography", 86.0);
        
        subjectAdapter.updateData(sampleGrades, performanceList);
    }

    private void loadChildPerformanceData(String childId) {
        // Load real performance data from Firestore
        firestore.collection("performance")
                .whereEqualTo("childId", childId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        performanceList.clear();
                        double totalGrade = 0;
                        int subjectCount = 0;
                        
                        for (var doc : queryDocumentSnapshots.getDocuments()) {
                            String subject = doc.getString("subject");
                            Double grade = doc.getDouble("grade");
                            String status = doc.getString("status");
                            
                            if (subject != null && grade != null) {
                                performanceList.add(new PerformanceData(childId, subject, grade, status != null ? status : "exam"));
                                totalGrade += grade;
                                subjectCount++;
                            }
                        }
                        
                        if (subjectCount > 0) {
                            double average = totalGrade / subjectCount;
                            if (overallGrade != null) overallGrade.setText(String.format("%.0f%%", average));
                            
                            String status = average >= 90 ? "Excellent" : average >= 80 ? "Good" : "Needs Improvement";
                            if (performanceStatus != null) {
                                performanceStatus.setText(status);
                                
                                int color = average >= 90 ? Color.parseColor("#4CAF50") : 
                                           average >= 80 ? Color.parseColor("#FF9800") : 
                                           Color.parseColor("#F44336");
                                performanceStatus.setTextColor(color);
                            }
                        }
                        
                        // Create grades map for adapter
                        Map<String, Double> gradesMap = new HashMap<>();
                        for (PerformanceData perf : performanceList) {
                            gradesMap.put(perf.getSubject(), perf.getGrade());
                        }
                        
                        subjectAdapter.updateData(gradesMap, performanceList);
                    } else {
                        // No performance data found, create sample data
                        createSamplePerformanceData(childId);
                    }
                })
                .addOnFailureListener(e -> createSamplePerformanceData(childId));
    }

    private void updateChildrenDisplay() {
        int childrenCount = childrenList.size();
        
        // Update children count badge
        this.childrenCount.setText(String.valueOf(childrenCount));
        
        // Show/hide no children message
        if (childrenCount == 0) {
            noChildrenCard.setVisibility(android.view.View.VISIBLE);
            childrenRecyclerView.setVisibility(android.view.View.GONE);
        } else {
            noChildrenCard.setVisibility(android.view.View.GONE);
            childrenRecyclerView.setVisibility(android.view.View.VISIBLE);
            childCardAdapter.updateChildren(childrenList);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload children data when returning from AddChildActivity
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadChildDataFromFirestore(user.getUid());
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back button - show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();
                    finishAffinity(); // Close all activities
                })
                .setNegativeButton("No", null)
                .show();
    }
}