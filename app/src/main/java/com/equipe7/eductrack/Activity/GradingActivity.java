package com.equipe7.eductrack.Activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.Exercise;
import com.equipe7.eductrack.models.GradeEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GradingActivity extends AppCompatActivity {

    private RecyclerView recyclerExercises;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Exercise> exercisesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grading);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        loadExercisesForGrading();
    }

    private void initializeViews() {
        recyclerExercises = findViewById(R.id.recyclerExercises);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadExercisesForGrading() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherId = mAuth.getCurrentUser().getUid();
        
        db.collection("exercises")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("status", "published")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    exercisesList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Exercise exercise = document.toObject(Exercise.class);
                        exercise.setExerciseId(document.getId());
                        exercisesList.add(exercise);
                    }
                    
                    if (exercisesList.isEmpty()) {
                        Toast.makeText(this, "No exercises available for grading", Toast.LENGTH_SHORT).show();
                    }
                    
                    // Here you would set up the adapter for the RecyclerView
                    // ExerciseGradingAdapter adapter = new ExerciseGradingAdapter(exercisesList);
                    // recyclerExercises.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load exercises: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void gradeStudent(String exerciseId, String studentId, double score, String feedback) {
        GradeEntry gradeEntry = new GradeEntry(exerciseId, studentId, mAuth.getCurrentUser().getUid(), "Mathematics");
        gradeEntry.setScore(score);
        gradeEntry.setFeedback(feedback);

        db.collection("grades")
                .add(gradeEntry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Grade saved successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Also update the performance data for real-time parent sync
                    updatePerformanceData(studentId, score, exerciseId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save grade: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePerformanceData(String studentId, double score, String exerciseId) {
        // This would update the performance collection for parent-teacher sync
        // Implementation would create PerformanceData objects and sync to Firebase
        // This ensures parents see grades immediately after teacher enters them
    }
}
