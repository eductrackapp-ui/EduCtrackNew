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

public class HomeworkActivity extends AppCompatActivity {

    private TextInputEditText etStudentCode;
    private MaterialButton btnLoadResults;
    private TextView tvMathW1, tvEnglishW2;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_homework_parent_beautiful);

            db = FirebaseFirestore.getInstance();

            // Safe view initialization with null checks
            try {
                View backButton = findViewById(R.id.ivBack);
                if (backButton != null) {
                    backButton.setOnClickListener(v -> finish());
                }
            } catch (Exception e) {
                // Back button not found, ignore
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
                                loadHomeworkResults(studentCode);
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

    private void loadHomeworkResults(String studentCode) {
        // Reset fields
        if (tvMathW1 != null) tvMathW1.setText("Math: --  |  French: --");
        if (tvEnglishW2 != null) tvEnglishW2.setText("English: --  |  SET: --");

        db.collection("homework_results").document(studentCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Week 1
                        String math = documentSnapshot.getString("math_week1");
                        String french = documentSnapshot.getString("french_week1");
                        if (tvMathW1 != null) {
                            tvMathW1.setText("Math: " + (math != null ? math : "--") + "  |  French: " + (french != null ? french : "--"));
                        }
                        
                        // Week 2
                        String english = documentSnapshot.getString("english_week2");
                        String set = documentSnapshot.getString("set_week2");
                        if (tvEnglishW2 != null) {
                            tvEnglishW2.setText("English: " + (english != null ? english : "--") + "  |  SET: " + (set != null ? set : "--"));
                        }
                        
                        Toast.makeText(this, "Homework loaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No results found for this code", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}