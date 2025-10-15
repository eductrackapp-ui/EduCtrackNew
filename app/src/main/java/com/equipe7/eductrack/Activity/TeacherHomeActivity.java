package com.equipe7.eductrack.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.equipe7.eductrack.R;
import com.equipe7.eductrack.Adapter.CarouselAdapter;
import com.equipe7.eductrack.TrackModule.ActivityAddReportTeacher;
import com.equipe7.eductrack.TrackModule.ActivityAddScoresTeacher;
import com.equipe7.eductrack.TrackModule.AddCalculateScoresActivity;
import com.equipe7.eductrack.models.CarouselItem;
import com.equipe7.eductrack.models.Exercise;
import com.equipe7.eductrack.models.ClassAnalytics;
import com.equipe7.eductrack.models.GradeEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherHomeActivity extends AppCompatActivity {

    // Navigation (haut)
    private View navNotifications, navProfile, navMessages;
    private TextView badgeMessages, badgeNotifications, tvWelcome, tvTeacherName;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Dashboard Elements
    private TextView teacherClassName, teacherSubject, studentCount, classAverage;
    private TextView excellentCount, goodCount, needsAttentionCount;
    private CardView btnCreateExercise, btnGradeAssignments;
    private RecyclerView recentExercisesRecycler;

    // Carousel
    private ViewPager2 combinedCarousel;
    private WormDotsIndicator combinedIndicator;

    // Barre de recherche
    private EditText etSearch;

    // Boutons bas de navigation
    private LinearLayout btnHome, btnAddCalculate, btnAddScoresReports, btnAddScoresTeacher;

    // Data
    private List<Exercise> recentExercises = new ArrayList<>();
    private ClassAnalytics currentAnalytics;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_home_activity);

        // Firebase init
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupClickListeners();
        loadTeacherData();
        loadClassAnalytics();
        setupRecyclerViews();

        // Actions navigation haut
        navNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Actions barre de navigation inférieure
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnAddCalculate.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCalculateScoresActivity.class);
            startActivity(intent);
        });

        btnAddScoresReports.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityAddReportTeacher.class);
            startActivity(intent);
        });

        btnAddScoresTeacher.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityAddScoresTeacher.class);
            startActivity(intent);
        });

        // Setup fonctionnalités
        listenForNotifications();
        listenForMessages();
        animateWelcomeText();
        setupCombinedCarousel();

        // Barre de recherche
        etSearch.setOnEditorActionListener(this::onEditorAction);
    }

    // 🔔 Notifications
    private void listenForNotifications() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("notifications")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    int count = value.size();
                    if (count > 0) {
                        badgeNotifications.setText(String.valueOf(count));
                        badgeNotifications.setVisibility(View.VISIBLE);
                    } else {
                        badgeNotifications.setVisibility(View.GONE);
                    }
                });
    }

    // 📩 Messages
    private void listenForMessages() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("messages")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    int count = value.size();
                    if (count > 0) {
                        badgeMessages.setText(String.valueOf(count));
                        badgeMessages.setVisibility(View.VISIBLE);
                    } else {
                        badgeMessages.setVisibility(View.GONE);
                    }
                });
    }

    // ✨ Animation du texte de bienvenue
    private void animateWelcomeText() {
        tvWelcome.setVisibility(View.VISIBLE);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1500);
        tvWelcome.startAnimation(fadeIn);
    }

    // 🎠 Carousel
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

        CarouselAdapter adapter = new CarouselAdapter(combinedItems);
        combinedCarousel.setAdapter(adapter);
        combinedIndicator.setViewPager2(combinedCarousel);

        autoSlideCarousel(combinedCarousel, combinedItems.size());
    }

    // ⏳ Auto slide
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

    // 🔍 Recherche Firestore
    private boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String query = etSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            performSearch(query);
        }
        return true;
    }

    private void performSearch(String query) {
        Map<String, List<Map<String, Object>>> results = new HashMap<>();

        // Collections à rechercher
        String[] collections = {"admins", "teachers", "lessons", "students", "classes"};

        for (String col : collections) {
            db.collection(col)
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        List<Map<String, Object>> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            if (doc.getData().toString().toLowerCase().contains(query.toLowerCase())) {
                                list.add(doc.getData());
                            }
                        }
                        if (!list.isEmpty()) {
                            results.put(col, list);
                        }

                        // Quand toutes les recherches sont terminées → envoyer à SearchActivity
                        if (col.equals("classes")) {
                            Intent i = new Intent(this, SearchActivity.class);
                            i.putExtra("query", query);
                            i.putExtra("results", new HashMap<>(results));
                            startActivity(i);
                        }
                    });
        }
    }

    private void initializeViews() {
        // Navigation elements
        navNotifications = findViewById(R.id.navNotifications);
        navProfile = findViewById(R.id.navProfile);
        badgeNotifications = findViewById(R.id.badgeNotifications);
        badgeMessages = findViewById(R.id.badgeMessages);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTeacherName = findViewById(R.id.tvTeacherName);

        // Dashboard elements
        teacherClassName = findViewById(R.id.teacherClassName);
        teacherSubject = findViewById(R.id.teacherSubject);
        studentCount = findViewById(R.id.studentCount);
        classAverage = findViewById(R.id.classAverage);
        excellentCount = findViewById(R.id.excellentCount);
        goodCount = findViewById(R.id.goodCount);
        needsAttentionCount = findViewById(R.id.needsAttentionCount);

        // Action buttons
        btnCreateExercise = findViewById(R.id.btnCreateExercise);
        btnGradeAssignments = findViewById(R.id.btnGradeAssignments);

        // RecyclerView
        recentExercisesRecycler = findViewById(R.id.recentExercisesRecycler);

        // Carousel
        combinedCarousel = findViewById(R.id.dashboardCarousel);
        combinedIndicator = findViewById(R.id.dashboardIndicator);

        // Search
        etSearch = findViewById(R.id.etSearch);

        // Bottom navigation
        btnHome = findViewById(R.id.btnHome);
        btnAddCalculate = findViewById(R.id.btnAddCalculate);
        btnAddScoresReports = findViewById(R.id.btnAddScoresReports);
        btnAddScoresTeacher = findViewById(R.id.btnAddScoresTeacher);
    }

    private void setupClickListeners() {
        // Navigation
        navNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Quick actions
        btnCreateExercise.setOnClickListener(v -> createNewExercise());
        btnGradeAssignments.setOnClickListener(v -> openGradingInterface());

        // Bottom navigation
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnAddCalculate.setOnClickListener(v -> startActivity(new Intent(this, AddCalculateScoresActivity.class)));
        btnAddScoresReports.setOnClickListener(v -> startActivity(new Intent(this, ActivityAddReportTeacher.class)));
        btnAddScoresTeacher.setOnClickListener(v -> startActivity(new Intent(this, ActivityAddScoresTeacher.class)));

        // Search
        etSearch.setOnEditorActionListener(this::onEditorAction);

        // Setup other functionalities
        listenForNotifications();
        listenForMessages();
        animateWelcomeText();
        setupCombinedCarousel();
    }

    private void loadTeacherData() {
        if (mAuth.getCurrentUser() == null) return;
        
        String teacherId = mAuth.getCurrentUser().getUid();
        db.collection("teachers").document(teacherId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String subject = documentSnapshot.getString("subject");
                        String className = documentSnapshot.getString("assignedClass");
                        
                        if (name != null) tvTeacherName.setText(name);
                        if (subject != null) teacherSubject.setText(subject);
                        if (className != null) teacherClassName.setText(className);
                        
                        // Load class-specific data
                        loadClassStudents(className);
                    } else {
                        // Create sample teacher data
                        createSampleTeacherData(teacherId);
                    }
                })
                .addOnFailureListener(e -> createSampleTeacherData(teacherId));
    }

    private void createSampleTeacherData(String teacherId) {
        tvTeacherName.setText("Mr. Johnson");
        teacherSubject.setText("Mathematics");
        teacherClassName.setText("Class 6A");
        studentCount.setText("28 Students");
        classAverage.setText("78.5%");
        
        // Set sample analytics
        excellentCount.setText("12");
        goodCount.setText("10");
        needsAttentionCount.setText("6");
        
        // Create sample exercises
        createSampleExercises();
    }

    private void loadClassStudents(String className) {
        if (className == null) return;
        
        db.collection("students")
                .whereEqualTo("className", className)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalStudents = queryDocumentSnapshots.size();
                    studentCount.setText(totalStudents + " Students");
                    
                    // Calculate class performance
                    calculateClassPerformance(queryDocumentSnapshots.getDocuments());
                });
    }

    private void calculateClassPerformance(List<com.google.firebase.firestore.DocumentSnapshot> students) {
        // This would calculate real performance data
        // For now, using sample data
        classAverage.setText("78.5%");
        excellentCount.setText("12");
        goodCount.setText("10");
        needsAttentionCount.setText("6");
    }

    private void loadClassAnalytics() {
        // Load real-time class analytics from Firebase
        if (mAuth.getCurrentUser() == null) return;
        
        String teacherId = mAuth.getCurrentUser().getUid();
        db.collection("class_analytics")
                .whereEqualTo("teacherId", teacherId)
                .orderBy("generatedDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || value.isEmpty()) return;
                    
                    ClassAnalytics analytics = value.getDocuments().get(0).toObject(ClassAnalytics.class);
                    if (analytics != null) {
                        currentAnalytics = analytics;
                        updateAnalyticsDisplay(analytics);
                    }
                });
    }

    private void updateAnalyticsDisplay(ClassAnalytics analytics) {
        classAverage.setText(analytics.getClassAverageDisplay());
        excellentCount.setText(String.valueOf(analytics.getExcellentStudents()));
        goodCount.setText(String.valueOf(analytics.getTotalStudents() - analytics.getExcellentStudents() - analytics.getStrugglingStudents()));
        needsAttentionCount.setText(String.valueOf(analytics.getStrugglingStudents()));
    }

    private void setupRecyclerViews() {
        // Setup recent exercises RecyclerView
        recentExercisesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // ExerciseAdapter would be implemented here
        
        loadRecentExercises();
    }

    private void loadRecentExercises() {
        if (mAuth.getCurrentUser() == null) return;
        
        String teacherId = mAuth.getCurrentUser().getUid();
        db.collection("exercises")
                .whereEqualTo("teacherId", teacherId)
                .orderBy("createdDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    
                    recentExercises.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                        Exercise exercise = doc.toObject(Exercise.class);
                        if (exercise != null) {
                            exercise.setExerciseId(doc.getId());
                            recentExercises.add(exercise);
                        }
                    }
                    
                    // Update RecyclerView adapter here
                });
    }

    private void createSampleExercises() {
        recentExercises.clear();
        
        Exercise ex1 = new Exercise(mAuth.getCurrentUser().getUid(), "Mathematics", "Algebra Quiz", "quiz");
        ex1.setDescription("Basic algebra equations");
        ex1.setStatus("published");
        
        Exercise ex2 = new Exercise(mAuth.getCurrentUser().getUid(), "Mathematics", "Geometry Assignment", "assignment");
        ex2.setDescription("Triangle properties");
        ex2.setStatus("graded");
        
        recentExercises.add(ex1);
        recentExercises.add(ex2);
    }

    private void createNewExercise() {
        // Navigate to class management first, then exercise creation
        Intent intent = new Intent(this, TeacherClassManagementActivity.class);
        startActivity(intent);
    }

    private void openGradingInterface() {
        // Navigate to class management for grading
        Intent intent = new Intent(this, TeacherClassManagementActivity.class);
        startActivity(intent);
    }
}