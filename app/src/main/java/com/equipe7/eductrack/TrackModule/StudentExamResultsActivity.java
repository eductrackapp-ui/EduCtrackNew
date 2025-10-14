package com.equipe7.eductrack.TrackModule;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.equipe7.eductrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class StudentExamResultsActivity extends AppCompatActivity {

    private TextInputEditText etStudentCode;
    private MaterialButton btnLoadResults;
    private TextView tvMathW1, tvEnglishW2;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_exams_parent_beautiful);

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
                tvEnglishW2 = findViewById(R.id.tvEnglishW2);

                if (btnLoadResults != null) {
                    btnLoadResults.setOnClickListener(v -> {
                        if (etStudentCode != null) {
                            String studentCode = etStudentCode.getText().toString().trim();
                            if (!studentCode.isEmpty()) {
                                loadExamResults(studentCode);
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

    private void loadExamResults(String studentCode) {
        // Show loading state
        if (tvMathW1 != null) tvMathW1.setText("--");
        if (tvEnglishW2 != null) tvEnglishW2.setText("--");

        db.collection("exam_results").document(studentCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String math = documentSnapshot.getString("mathematics");
                        String english = documentSnapshot.getString("english");
                        
                        if (tvMathW1 != null) tvMathW1.setText(math != null ? math : "--");
                        if (tvEnglishW2 != null) tvEnglishW2.setText(english != null ? english : "--");
                        
                        Toast.makeText(this, "Exam results loaded", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}