package com.equipe7.eductrack.Module;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import android.widget.TextView;
import javax.annotation.Nullable;
import java.util.Map;

public class StudentExamResultsActivity extends AppCompatActivity {

    private TextInputEditText etStudentCode;
    private MaterialButton btnLoadResults;
    private FirebaseFirestore db;

    // TextViews par semaine et matière
    private TextView tvMathW1, tvFrenchW1;
    private TextView tvEnglishW2, tvSetW2;
    private TextView tvSocialW3, tvKinyarwandaW3;
    private TextView tvSocialW4, tvKinyarwandaW4;
    private TextView tvSocialW5, tvKinyarwandaW5;
    private TextView tvEnglishW6, tvMathW6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams_parent);

        // Init Firestore
        db = FirebaseFirestore.getInstance();

        // Liens avec XML
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

        // Bouton pour charger les résultats
        btnLoadResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentCode = etStudentCode.getText().toString().trim();
                if(TextUtils.isEmpty(studentCode)){
                    Toast.makeText(StudentExamResultsActivity.this, "Enter student code", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadStudentResults(studentCode);
            }
        });
    }

    private void loadStudentResults(String studentCode) {
        // Référence vers le document Firestore de l'élève
        DocumentReference docRef = db.collection("students").document(studentCode);

        // Ecoute en temps réel
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(StudentExamResultsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    // Récupération des données dynamiquement
                    Map<String, Object> data = snapshot.getData();
                    if (data != null) {
                        // WEEK 1
                        tvMathW1.setText(data.containsKey("week1_math") ? data.get("week1_math").toString() : "--");
                        tvFrenchW1.setText(data.containsKey("week1_french") ? data.get("week1_french").toString() : "--");

                        // WEEK 2
                        tvEnglishW2.setText(data.containsKey("week2_english") ? data.get("week2_english").toString() : "--");
                        tvSetW2.setText(data.containsKey("week2_set") ? data.get("week2_set").toString() : "--");

                        // WEEK 3
                        tvSocialW3.setText(data.containsKey("week3_social") ? data.get("week3_social").toString() : "--");
                        tvKinyarwandaW3.setText(data.containsKey("week3_kinyarwanda") ? data.get("week3_kinyarwanda").toString() : "--");

                        // WEEK 4
                        tvSocialW4.setText(data.containsKey("week4_social") ? data.get("week4_social").toString() : "--");
                        tvKinyarwandaW4.setText(data.containsKey("week4_kinyarwanda") ? data.get("week4_kinyarwanda").toString() : "--");

                        // WEEK 5
                        tvSocialW5.setText(data.containsKey("week5_social") ? data.get("week5_social").toString() : "--");
                        tvKinyarwandaW5.setText(data.containsKey("week5_kinyarwanda") ? data.get("week5_kinyarwanda").toString() : "--");

                        // WEEK 6
                        tvEnglishW6.setText(data.containsKey("week6_english") ? data.get("week6_english").toString() : "--");
                        tvMathW6.setText(data.containsKey("week6_math") ? data.get("week6_math").toString() : "--");
                    }
                } else {
                    Toast.makeText(StudentExamResultsActivity.this, "Student not found", Toast.LENGTH_SHORT).show();
                    clearAllResults();
                }
            }
        });
    }

    private void clearAllResults() {
        tvMathW1.setText("--");
        tvFrenchW1.setText("--");
        tvEnglishW2.setText("--");
        tvSetW2.setText("--");
        tvSocialW3.setText("--");
        tvKinyarwandaW3.setText("--");
        tvSocialW4.setText("--");
        tvKinyarwandaW4.setText("--");
        tvSocialW5.setText("--");
        tvKinyarwandaW5.setText("--");
        tvEnglishW6.setText("--");
        tvMathW6.setText("--");
    }
}
