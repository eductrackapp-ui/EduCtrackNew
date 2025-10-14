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
        setContentView(R.layout.activity_homework_parent_beautiful);

        // Initialize views - only the ones that exist in beautiful layout
        etStudentCode = findViewById(R.id.etStudentCode);
        btnLoadResults = findViewById(R.id.btnLoadResults);

        // Only initialize views that exist
        tvMathW1 = findViewById(R.id.tvMathW1);
        tvEnglishW2 = findViewById(R.id.tvEnglishW2);
        
        // Back button
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        btnLoadResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentCode = etStudentCode.getText().toString().trim();
                if (studentCode.isEmpty()) {
                    Toast.makeText(HomeworkActivity.this, "Entrer le code élève", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadHomeworkResults(studentCode);
            }
        });
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