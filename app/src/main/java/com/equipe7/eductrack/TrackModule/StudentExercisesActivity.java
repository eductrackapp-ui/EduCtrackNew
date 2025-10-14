package com.equipe7.eductrack.TrackModule;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentExercisesActivity extends AppCompatActivity {

    private TextInputEditText etStudentCode;
    private MaterialButton btnLoadResults;
    private TextView tvMathW1;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_exercises_parent_beautiful);

            db = FirebaseFirestore.getInstance();

            // Safe view initialization
            try {
                View backButton = findViewById(R.id.ivBack);
                if (backButton != null) {
                    backButton.setOnClickListener(v -> finish());
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                etStudentCode = findViewById(R.id.etStudentCode);
                btnLoadResults = findViewById(R.id.btnLoadResults);
                tvMathW1 = findViewById(R.id.tvMathW1);

                if (btnLoadResults != null) {
                    btnLoadResults.setOnClickListener(v -> {
                        if (etStudentCode != null) {
                            String studentCode = etStudentCode.getText().toString().trim();
                            if (!studentCode.isEmpty()) {
                                loadExercisesResults(studentCode);
                            } else {
                                Toast.makeText(this, "Enter student code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Toast.makeText(this, "Some features may not be available", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error loading page: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadExercisesResults(String studentCode) {
        // Show loading state
        if (tvMathW1 != null) tvMathW1.setText("--");

        db.collection("exercise_results").document(studentCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String math = documentSnapshot.getString("mathematics");
                        if (tvMathW1 != null) tvMathW1.setText(math != null ? math : "--");
                        
                        Toast.makeText(this, "Exercise results loaded", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}