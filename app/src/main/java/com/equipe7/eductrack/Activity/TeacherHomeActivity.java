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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.equipe7.eductrack.R;
import com.equipe7.eductrack.Adapter.CarouselAdapter;
import com.equipe7.eductrack.models.CarouselItem;
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
    private TextView badgeMessages, badgeNotifications, tvWelcome;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Carousel
    private ViewPager2 combinedCarousel;
    private WormDotsIndicator combinedIndicator;

    // Barre de recherche
    private EditText etSearch;

    // Boutons bas de navigation
    private LinearLayout btnDashboard, btnHome, btnAllCourses, btnStudents;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_home_activity);

        // Firebase init
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Liens XML
        navNotifications = findViewById(R.id.navNotifications);
        navProfile = findViewById(R.id.navProfile);

        badgeNotifications = findViewById(R.id.badgeNotifications);
        tvWelcome = findViewById(R.id.tvWelcome);

        combinedCarousel = findViewById(R.id.dashboardCarousel);
        combinedIndicator = findViewById(R.id.dashboardIndicator);

        etSearch = findViewById(R.id.etSearch);

        // ✅ Initialisation des boutons existants
        btnDashboard = findViewById(R.id.btnDashboard);
        btnHome = findViewById(R.id.btnHome);
        btnAllCourses = findViewById(R.id.btnAllCourses); // Nouveau bouton
        btnStudents = findViewById(R.id.btnStudents);     // Nouveau bouton

        // Actions navigation haut
        navNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Actions barre bas
        btnDashboard.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherDashboard.class));
            overridePendingTransition(0, 0);
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // ✅ Nouveau : All Courses → LessonsActivity
        btnAllCourses.setOnClickListener(v -> {
            startActivity(new Intent(this, LessonsActivity.class));
            overridePendingTransition(0, 0);
        });

        // ✅ Nouveau : Students → StudentListActivity
        btnStudents.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentListActivity.class));
            overridePendingTransition(0, 0);
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
}
