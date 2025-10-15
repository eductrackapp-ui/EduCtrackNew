package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.Child;
import com.equipe7.eductrack.models.Exercise;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateExerciseActivity extends AppCompatActivity {

    private TextView tvClassName;
    private ImageView btnBack;
    private EditText etTitle, etDescription, etSkill, etMaxPoints;
    private Spinner spinnerType, spinnerDifficulty, spinnerSubject;
    private Button btnSave, btnCancel, btnPreview;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private String className;
    private List<Child> studentsList;
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_exercise);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        teacherId = mAuth.getCurrentUser().getUid();

        // Get data from intent
        getIntentData();
        
        initializeViews();
        setupSpinners();
        setupClickListeners();
    }

    private void getIntentData() {
        className = getIntent().getStringExtra("class_name");
        studentsList = getIntent().getParcelableArrayListExtra("students_list");
        if (studentsList == null) {
            studentsList = new ArrayList<>();
        }
    }

    private void initializeViews() {
        tvClassName = findViewById(R.id.tvClassName);
        btnBack = findViewById(R.id.btnBack);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etSkill = findViewById(R.id.etSkill);
        etMaxPoints = findViewById(R.id.etMaxPoints);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        // btnPreview = findViewById(R.id.btnPreview); // Commented out - not in layout
        
        // Set class name
        if (className != null) {
            tvClassName.setText("Create Exercise for " + className);
        }
        
        // Set default max points
        etMaxPoints.setText("100");
    }

    private void setupSpinners() {
        // Exercise types
        String[] types = {"Exercise", "Assignment", "Quiz", "Exam"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        
        // Difficulty levels
        String[] difficulties = {"Easy", "Medium", "Hard"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);
        spinnerDifficulty.setSelection(1); // Default to Medium
        
        // Subjects
        String[] subjects = {"Mathematics", "English", "Science", "History", "Geography", "French", "Art", "Physical Education"};
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveExercise());
        btnCancel.setOnClickListener(v -> finish());
        // btnPreview.setOnClickListener(v -> previewExercise()); // Commented out - button not in layout
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

        if (teacherId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = spinnerType.getSelectedItem().toString().toLowerCase();
        String subject = spinnerSubject.getSelectedItem().toString();
        String difficulty = spinnerDifficulty.getSelectedItem().toString().toLowerCase();
        
        Exercise exercise = new Exercise(teacherId, subject, title, type);
        exercise.setDescription(description);
        exercise.setSkill(skill);
        exercise.setDifficulty(difficulty);
        exercise.setClassName(className);
        exercise.setGradeLevel("Grade 6"); // This could be dynamic
        exercise.setCreatedDate(new Date());
        exercise.setStatus("draft");
        
        // Set max points
        double maxPoints = 100.0;
        if (!maxPointsStr.isEmpty()) {
            try {
                maxPoints = Double.parseDouble(maxPointsStr);
            } catch (NumberFormatException e) {
                maxPoints = 100.0;
            }
        }
        exercise.setMaxPoints(maxPoints);
        
        // Set target students (all students in class)
        List<String> targetStudentIds = new ArrayList<>();
        for (Child student : studentsList) {
            targetStudentIds.add(student.getChildId());
        }
        exercise.setTargetStudents(targetStudentIds);

        // Save to Firebase
        db.collection("exercises")
                .add(exercise)
                .addOnSuccessListener(documentReference -> {
                    String exerciseId = documentReference.getId();
                    exercise.setExerciseId(exerciseId);
                    
                    // Create grade entries for all students
                    createGradeEntries(exerciseId, exercise);
                    
                    Toast.makeText(this, "Exercise created successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to grading activity
                    Intent intent = new Intent(this, ExerciseGradingActivity.class);
                    intent.putExtra("exercise_id", exerciseId);
                    intent.putExtra("exercise_title", title);
                    intent.putExtra("class_name", className);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create exercise: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createGradeEntries(String exerciseId, Exercise exercise) {
        // Create individual grade entries for each student
        for (Child student : studentsList) {
            com.equipe7.eductrack.models.GradeEntry gradeEntry = new com.equipe7.eductrack.models.GradeEntry(
                exerciseId, student.getChildId(), teacherId, exercise.getSubject());
            
            gradeEntry.setStudentName(student.getName());
            gradeEntry.setExerciseTitle(exercise.getTitle());
            gradeEntry.setExerciseType(exercise.getType());
            gradeEntry.setMaxScore(exercise.getMaxPoints());
            gradeEntry.setStatus("pending");
            
            // Save grade entry
            db.collection("grades")
                    .add(gradeEntry)
                    .addOnSuccessListener(docRef -> {
                        // Grade entry created successfully
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    private void previewExercise() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in title and description to preview", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create preview dialog or activity
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Exercise Preview");
        
        String preview = "Title: " + title + "\n\n" +
                        "Subject: " + spinnerSubject.getSelectedItem().toString() + "\n" +
                        "Type: " + spinnerType.getSelectedItem().toString() + "\n" +
                        "Difficulty: " + spinnerDifficulty.getSelectedItem().toString() + "\n" +
                        "Max Points: " + etMaxPoints.getText().toString() + "\n\n" +
                        "Description:\n" + description + "\n\n" +
                        "Target Students: " + studentsList.size() + " students in " + className;
        
        builder.setMessage(preview);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
