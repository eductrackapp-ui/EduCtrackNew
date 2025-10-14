package com.equipe7.eductrack.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.Exercise;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class CreateExerciseActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etSkill, etMaxPoints;
    private Spinner spinnerType, spinnerDifficulty;
    private Button btnSave, btnCancel;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_exercise);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etSkill = findViewById(R.id.etSkill);
        etMaxPoints = findViewById(R.id.etMaxPoints);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveExercise());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveExercise() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String skill = etSkill.getText().toString().trim();
        String maxPointsStr = etMaxPoints.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherId = mAuth.getCurrentUser().getUid();
        String type = spinnerType.getSelectedItem().toString().toLowerCase();
        
        Exercise exercise = new Exercise(teacherId, "Mathematics", title, type);
        exercise.setDescription(description);
        exercise.setSkill(skill);
        exercise.setCreatedDate(new Date());
        
        if (!maxPointsStr.isEmpty()) {
            try {
                double maxPoints = Double.parseDouble(maxPointsStr);
                exercise.setMaxPoints(maxPoints);
            } catch (NumberFormatException e) {
                exercise.setMaxPoints(100.0);
            }
        }

        // Save to Firebase
        db.collection("exercises")
                .add(exercise)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Exercise created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create exercise: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
