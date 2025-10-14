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
        setContentView(R.layout.activity_exercises_parent_beautiful);

        etStudentCode = findViewById(R.id.etStudentCode);
        btnLoadResults = findViewById(R.id.btnLoadResults);

        // Only initialize views that exist in beautiful layout
        tvMathW1 = findViewById(R.id.tvMathW1);
        
        // Back button
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        btnLoadResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentCode = etStudentCode.getText().toString().trim();
                if (studentCode.isEmpty()) {
                    Toast.makeText(StudentExercisesActivity.this, "Entrer le code élève", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadExercisesResults(studentCode);
            }
        });
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