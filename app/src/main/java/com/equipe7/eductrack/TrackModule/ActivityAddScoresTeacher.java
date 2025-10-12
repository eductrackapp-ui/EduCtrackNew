package com.equipe7.eductrack.TrackModule;

import android.os.Bundle;
import android.widget.*;
        import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ActivityAddScoresTeacher extends AppCompatActivity {

    private TextInputEditText etStudentCode, etExerciseTitle, etScore;
    private Spinner spinnerAssessmentType, spinnerWeek, spinnerTrimester, spinnerYear, spinnerLesson;
    private MaterialButton btnSaveAndBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scores_teacher); // adapte le nom du layout

        etStudentCode = findViewById(R.id.etStudentCode);
        etExerciseTitle = findViewById(R.id.etExerciseTitle);
        etScore = findViewById(R.id.etScore);
        spinnerAssessmentType = findViewById(R.id.spinnerAssessmentType);
        spinnerWeek = findViewById(R.id.spinnerWeek);
        spinnerTrimester = findViewById(R.id.spinnerTrimester);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerLesson = findViewById(R.id.spinnerLesson);
        btnSaveAndBack = findViewById(R.id.btnSaveAndBack);

        db = FirebaseFirestore.getInstance();

        btnSaveAndBack.setOnClickListener(v -> saveDataToFirestore());
    }

    private void saveDataToFirestore() {
        String studentCode = etStudentCode.getText().toString().trim();
        String assessmentType = spinnerAssessmentType.getSelectedItem().toString();
        String week = spinnerWeek.getSelectedItem().toString().replace("Week ", "");
        String trimester = spinnerTrimester.getSelectedItem().toString();
        String year = spinnerYear.getSelectedItem().toString();
        String lesson = spinnerLesson.getSelectedItem().toString().toLowerCase().replace(" ", "_").replace("é", "e").replace("è", "e");
        String exerciseTitle = etExerciseTitle.getText().toString().trim();
        String scoreStr = etScore.getText().toString().trim();

        if (studentCode.isEmpty() || exerciseTitle.isEmpty() || scoreStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int score;
        try {
            score = Integer.parseInt(scoreStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Score must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifie si le code élève existe dans la collection "students"
        db.collection("students").document(studentCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Le code élève existe, on enregistre dans la bonne collection
                        String collection;
                        if (assessmentType.equals("Examens")) collection = "exams_resultas";
                        else if (assessmentType.equals("Exercices")) collection = "exercices_resultats";
                        else collection = "homework_resultats";

                        // Prépare les données à enregistrer
                        Map<String, Object> data = new HashMap<>();
                        data.put("exercise_title", exerciseTitle);
                        data.put("score", score);
                        data.put("week", week);
                        data.put("trimester", trimester);
                        data.put("year", year);
                        data.put("lesson", lesson);

                        // Le champ score par semaine et matière
                        String scoreField = lesson + "_week" + week;
                        data.put(scoreField, score);

                        // Ajoute ou met à jour le document de l'élève
                        db.collection(collection).document(studentCode)
                                .set(data, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show();
                                    finish(); // ou autre action
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show());
                    } else {
                        // Le code élève n'existe pas
                        Toast.makeText(this, "Student code not found. Please create a new account.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error checking student code", Toast.LENGTH_SHORT).show());
    }
}