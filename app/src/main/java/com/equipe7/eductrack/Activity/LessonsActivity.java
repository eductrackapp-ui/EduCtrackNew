package com.equipe7.eductrack.Activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.equipe7.eductrack.Adapter.LessonAdapter;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.Utils.LessonActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LessonsActivity extends AppCompatActivity implements LessonAdapter.OnLessonActionListener {

    private static final String TAG = "LessonsActivity";
    
    private RecyclerView recyclerLessons;
    private LessonAdapter lessonAdapter;
    private List<LessonActivity> lessonList;
    private ImageView btnBack, btnAddLesson;
    
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);

        try {
            // Initialize Firebase
            db = FirebaseFirestore.getInstance();
            
            // Initialize views
            initViews();
            setupRecyclerView();
            setupClickListeners();
            
            // Load lessons from Firestore or create sample data
            loadLessons();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            showError("Failed to initialize lessons");
        }
    }

    private void initViews() {
        recyclerLessons = findViewById(R.id.recyclerLessons);
        btnBack = findViewById(R.id.btnBack);
        btnAddLesson = findViewById(R.id.btnAddLesson);
    }

    private void setupRecyclerView() {
        lessonList = new ArrayList<>();
        lessonAdapter = new LessonAdapter(lessonList, this);
        lessonAdapter.setOnLessonActionListener(this);
        
        recyclerLessons.setLayoutManager(new LinearLayoutManager(this));
        recyclerLessons.setAdapter(lessonAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnAddLesson.setOnClickListener(v -> {
            // TODO: Open add lesson activity
            showAddLessonDialog();
        });
    }

    private void loadLessons() {
        // First, try to load from Firestore
        if (db != null) {
            db.collection("lessons")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    lessonList.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        // If no lessons in Firestore, create sample data
                        createSampleLessons();
                    } else {
                        // Load from Firestore
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                LessonActivity lesson = doc.toObject(LessonActivity.class);
                                lesson.setId(doc.getId());
                                lessonList.add(lesson);
                            } catch (Exception e) {
                                Log.w(TAG, "Error parsing lesson: " + doc.getId(), e);
                            }
                        }
                    }
                    
                    lessonAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + lessonList.size() + " lessons");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading lessons: ", e);
                    // Fallback to sample data
                    createSampleLessons();
                });
        } else {
            createSampleLessons();
        }
    }

    private void createSampleLessons() {
        lessonList.clear();
        
        String[] classes = {"P1", "P2", "P3", "P4", "P5", "P6"};
        String[] subjects = {"Mathematics", "French", "English", "SET", "Social Studies", "Kinyarwanda"};
        
        for (String classLevel : classes) {
            for (String subject : subjects) {
                LessonActivity lesson = new LessonActivity(classLevel, subject, subject + " - " + classLevel);
                lesson.setDescription("Learn " + subject.toLowerCase() + " concepts for " + classLevel + " level");
                lesson.setDuration(45); // 45 minutes
                lesson.setStatus("active");
                lesson.setTeacherName("Sample Teacher");
                lesson.setTopics(Arrays.asList("Topic 1", "Topic 2", "Topic 3"));
                
                lessonList.add(lesson);
            }
        }
        
        lessonAdapter.notifyDataSetChanged();
    }

    private void showAddLessonDialog() {
        // Simple implementation - in a real app, this would open a detailed form
        Toast.makeText(this, "Add Lesson feature - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // LessonAdapter.OnLessonActionListener implementation
    @Override
    public void onLessonClick(LessonActivity lesson) {
        Toast.makeText(this, "Lesson: " + lesson.getDisplayTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditLesson(LessonActivity lesson) {
        Toast.makeText(this, "Edit: " + lesson.getDisplayTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteLesson(LessonActivity lesson) {
        showDeleteConfirmation(lesson);
    }

    @Override
    public void onViewDetails(LessonActivity lesson) {
        Toast.makeText(this, "Details: " + lesson.getDisplayTitle(), Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation(LessonActivity lesson) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Lesson")
            .setMessage("Are you sure you want to delete \"" + lesson.getDisplayTitle() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> deleteLesson(lesson))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteLesson(LessonActivity lesson) {
        int position = lessonList.indexOf(lesson);
        if (position != -1) {
            lessonList.remove(position);
            lessonAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Lesson deleted", Toast.LENGTH_SHORT).show();
            
            // TODO: Also delete from Firestore if needed
        }
    }
}
