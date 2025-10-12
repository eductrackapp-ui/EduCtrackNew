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
    private TextView tvMathW1, tvFrenchW1, tvEnglishW2, tvSetW2,
            tvSocialW3, tvKinyarwandaW3, tvSocialW4, tvKinyarwandaW4,
            tvSocialW5, tvKinyarwandaW5, tvEnglishW6, tvMathW6;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises_parent); // adapte le nom du layout

        etStudentCode = findViewById(R.id.etStudentCode);
        btnLoadResults = findViewById(R.id.btnLoadResults);

        tvMathW1 = findViewById(R.id.tvMathW1);
        tvFrenchW1 = findViewById(R.id.tvFrenchW1);
        tvEnglishW2 = findViewById(R.id.tvEnglishW2);
        tvSetW2 = findViewById(R.id.tvSetW2);
        tvSocialW3 = findViewById(R.id.tvSocialW3);
        tvKinyarwandaW3 = findViewById(R.id.tvKinyarwandaW3);
        tvSocialW4 = findViewById(R.id.tvSocialW4);
        tvKinyarwandaW4 = findViewById(R.id.tvKinyarwandaW4);
        tvSocialW5 = findViewById(R.id.tvSocialW5);
        tvKinyarwandaW5 = findViewById(R.id.tvKinyarwandaW5);
        tvEnglishW6 = findViewById(R.id.tvEnglishW6);
        tvMathW6 = findViewById(R.id.tvMathW6);

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
        // Réinitialise tous les champs à "?"
        tvMathW1.setText("?");
        tvFrenchW1.setText("?");
        tvEnglishW2.setText("?");
        tvSetW2.setText("?");
        tvSocialW3.setText("?");
        tvKinyarwandaW3.setText("?");
        tvSocialW4.setText("?");
        tvKinyarwandaW4.setText("?");
        tvSocialW5.setText("?");
        tvKinyarwandaW5.setText("?");
        tvEnglishW6.setText("?");
        tvMathW6.setText("?");

        db.collection("exercices_resultats").document(studentCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Semaine 1
                        tvMathW1.setText(documentSnapshot.getString("math_week1") != null ? documentSnapshot.getString("math_week1") : "?");
                        tvFrenchW1.setText(documentSnapshot.getString("french_week1") != null ? documentSnapshot.getString("french_week1") : "?");
                        // Semaine 2
                        tvEnglishW2.setText(documentSnapshot.getString("english_week2") != null ? documentSnapshot.getString("english_week2") : "?");
                        tvSetW2.setText(documentSnapshot.getString("set_week2") != null ? documentSnapshot.getString("set_week2") : "?");
                        // Semaine 3
                        tvSocialW3.setText(documentSnapshot.getString("social_week3") != null ? documentSnapshot.getString("social_week3") : "?");
                        tvKinyarwandaW3.setText(documentSnapshot.getString("kinyarwanda_week3") != null ? documentSnapshot.getString("kinyarwanda_week3") : "?");
                        // Semaine 4
                        tvSocialW4.setText(documentSnapshot.getString("social_week4") != null ? documentSnapshot.getString("social_week4") : "?");
                        tvKinyarwandaW4.setText(documentSnapshot.getString("kinyarwanda_week4") != null ? documentSnapshot.getString("kinyarwanda_week4") : "?");
                        // Semaine 5
                        tvSocialW5.setText(documentSnapshot.getString("social_week5") != null ? documentSnapshot.getString("social_week5") : "?");
                        tvKinyarwandaW5.setText(documentSnapshot.getString("kinyarwanda_week5") != null ? documentSnapshot.getString("kinyarwanda_week5") : "?");
                        // Semaine 6
                        tvEnglishW6.setText(documentSnapshot.getString("english_week6") != null ? documentSnapshot.getString("english_week6") : "?");
                        tvMathW6.setText(documentSnapshot.getString("math_week6") != null ? documentSnapshot.getString("math_week6") : "?");
                    } else {
                        Toast.makeText(this, "Aucun résultat trouvé pour ce code.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors du chargement.", Toast.LENGTH_SHORT).show());
    }
}