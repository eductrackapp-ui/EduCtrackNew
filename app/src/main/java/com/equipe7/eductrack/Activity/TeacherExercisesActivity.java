package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.adapters.TeacherExerciseAdapter;
import com.equipe7.eductrack.models.Exercise;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TeacherExercisesActivity extends AppCompatActivity {

    private TextView tvTitle, tvExerciseCount;
    private ImageView btnBack;
    private RecyclerView exercisesRecyclerView;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private TeacherExerciseAdapter exerciseAdapter;
    private List<Exercise> exercisesList = new ArrayList<>();

    private String teacherId;
    private String className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_exercises);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        teacherId = auth.getCurrentUser().getUid();

        // Get class name from intent
        className = getIntent().getStringExtra("class_name");

        initializeViews();
        setupClickListeners();
        loadTeacherExercises();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvExerciseCount = findViewById(R.id.tvExerciseCount);
        btnBack = findViewById(R.id.btnBack);
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView);

        // Set title
        tvTitle.setText("My Exercises");

        // Setup RecyclerView
        exerciseAdapter = new TeacherExerciseAdapter(this, exercisesList, this::onExerciseClick);
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(exerciseAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadTeacherExercises() {
        firestore.collection("exercises")
                .whereEqualTo("teacherId", teacherId)
                .orderBy("createdDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    exercisesList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Exercise exercise = document.toObject(Exercise.class);
                        exercise.setExerciseId(document.getId());
                        exercisesList.add(exercise);
                    }
                    
                    // Update count
                    tvExerciseCount.setText(exercisesList.size() + " exercises");
                    exerciseAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load exercises: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onExerciseClick(Exercise exercise) {
        // Navigate to grading activity for this specific exercise
        Intent intent = new Intent(this, ExerciseGradingActivity.class);
        intent.putExtra("exercise_id", exercise.getExerciseId());
        intent.putExtra("exercise_title", exercise.getTitle());
        intent.putExtra("class_name", exercise.getClassName());
        intent.putExtra("max_points", exercise.getMaxPoints());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh exercises when returning from grading
        loadTeacherExercises();
    }
}
