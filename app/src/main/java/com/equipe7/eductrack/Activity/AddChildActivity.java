package com.equipe7.eductrack.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.Child;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddChildActivity extends AppCompatActivity {

    private TextInputEditText etChildName, etClassName, etGrade, etStudentCode;
    private AutoCompleteTextView spinnerBranch;
    private Button btnGenerateCode, btnAddChild;
    private ProgressBar progressBar;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    private static final String[] BRANCHES = {
        "Eden Family School Kacyiru",
        "Eden Family School Gisozi",
        "Eden Family School Kimisagara"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        setupBranchSpinner();
        setupClickListeners();
        
        // Auto-generate student code on load
        generateStudentCode();
    }

    private void initializeViews() {
        etChildName = findViewById(R.id.etChildName);
        etClassName = findViewById(R.id.etClassName);
        etGrade = findViewById(R.id.etGrade);
        etStudentCode = findViewById(R.id.etStudentCode);
        spinnerBranch = findViewById(R.id.spinnerBranch);
        btnGenerateCode = findViewById(R.id.btnGenerateCode);
        btnAddChild = findViewById(R.id.btnAddChild);
        progressBar = findViewById(R.id.progressBar);
        
        // Make student code read-only
        if (etStudentCode != null) {
            etStudentCode.setFocusable(false);
            etStudentCode.setClickable(false);
        }
    }

    private void setupBranchSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            BRANCHES
        );
        spinnerBranch.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Back button
        View backButton = findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Generate code button
        btnGenerateCode.setOnClickListener(v -> generateStudentCode());

        // Add child button
        btnAddChild.setOnClickListener(v -> validateAndAddChild());
    }

    private void generateStudentCode() {
        // Generate a random 8-character alphanumeric student code
        String code = generateRandomCode(8);
        
        // Check if code already exists in database
        checkCodeUniqueness(code);
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }

    private void checkCodeUniqueness(String code) {
        progressBar.setVisibility(View.VISIBLE);
        
        firestore.collection("children")
                .whereEqualTo("studentCode", code)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null && !snapshot.isEmpty()) {
                            // Code already exists, generate a new one
                            generateStudentCode();
                        } else {
                            // Code is unique, use it
                            etStudentCode.setText(code);
                        }
                    } else {
                        // Error checking, but use the code anyway
                        etStudentCode.setText(code);
                    }
                });
    }

    private void validateAndAddChild() {
        String childName = etChildName.getText().toString().trim();
        String className = etClassName.getText().toString().trim();
        String grade = etGrade.getText().toString().trim();
        String studentCode = etStudentCode.getText().toString().trim();
        String branch = spinnerBranch.getText().toString().trim();

        // Validation
        if (childName.isEmpty()) {
            etChildName.setError("Child name is required");
            etChildName.requestFocus();
            return;
        }

        if (className.isEmpty()) {
            etClassName.setError("Class name is required");
            etClassName.requestFocus();
            return;
        }

        if (grade.isEmpty()) {
            etGrade.setError("Grade is required");
            etGrade.requestFocus();
            return;
        }

        if (branch.isEmpty()) {
            Toast.makeText(this, "Please select a school branch", Toast.LENGTH_SHORT).show();
            spinnerBranch.requestFocus();
            return;
        }

        if (studentCode.isEmpty()) {
            Toast.makeText(this, "Please generate a student code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add child to Firebase
        addChildToFirebase(childName, className, grade, studentCode, branch);
    }

    private void addChildToFirebase(String childName, String className, String grade, 
                                     String studentCode, String branch) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to add a child", Toast.LENGTH_SHORT).show();
            return;
        }

        String parentId = currentUser.getUid();
        
        progressBar.setVisibility(View.VISIBLE);
        btnAddChild.setEnabled(false);

        // Create child data map
        Map<String, Object> childData = new HashMap<>();
        childData.put("name", childName);
        childData.put("className", className);
        childData.put("grade", grade);
        childData.put("studentCode", studentCode);
        childData.put("branch", branch);
        childData.put("parentId", parentId);
        childData.put("overallAverage", 0.0);
        childData.put("status", "good");
        childData.put("schoolYear", "2024-2025");
        childData.put("createdAt", System.currentTimeMillis());

        // Add to Firestore
        firestore.collection("children")
                .add(childData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    btnAddChild.setEnabled(true);
                    
                    Toast.makeText(this, "Child added successfully! Student Code: " + studentCode, 
                            Toast.LENGTH_LONG).show();
                    
                    // Return to parent home
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnAddChild.setEnabled(true);
                    
                    Toast.makeText(this, "Error adding child: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                });
    }
}
