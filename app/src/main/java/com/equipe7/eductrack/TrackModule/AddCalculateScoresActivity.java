package com.equipe7.eductrack.TrackModule;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class AddCalculateScoresActivity extends AppCompatActivity {

    private TextInputEditText etStudentCode;
    private MaterialButton btnLoadScores, btnPostScores;
    private TableLayout tableScores;
    private TextView tvPeriodResults;

    private FirebaseFirestore db;

    // Stockage des scores
    private Map<String, Double> totalPerSubject = new HashMap<>();
    private double totalGeneral = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calculate_scores_teacher);

        db = FirebaseFirestore.getInstance();

        etStudentCode = findViewById(R.id.etStudentCode);
        btnLoadScores = findViewById(R.id.btnLoadScores);
        btnPostScores = findViewById(R.id.btnPostScores);
        tableScores = findViewById(R.id.tableScores);
        tvPeriodResults = findViewById(R.id.tvPeriodResults);

        btnLoadScores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentCode = etStudentCode.getText().toString().trim();
                if (TextUtils.isEmpty(studentCode)) {
                    Toast.makeText(AddCalculateScoresActivity.this, "Enter student code", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadStudentScores(studentCode);
            }
        });

        btnPostScores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentCode = etStudentCode.getText().toString().trim();
                if (TextUtils.isEmpty(studentCode)) {
                    Toast.makeText(AddCalculateScoresActivity.this, "Enter student code", Toast.LENGTH_SHORT).show();
                    return;
                }
                postScoresToFirestore(studentCode);
            }
        });
    }

    private void loadStudentScores(String studentCode) {
        tableScores.removeAllViews();
        totalPerSubject.clear();
        totalGeneral = 0.0;

        // Header du tableau
        TableRow header = new TableRow(this);
        String[] headers = {"Week", "Subject", "Homework", "Exercise", "Exam", "Total"};
        for (String h : headers) {
            TextView tv = new TextView(this);
            tv.setText(h);
            tv.setPadding(8, 8, 8, 8);
            tv.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
            tv.setTextColor(getResources().getColor(android.R.color.white));
            header.addView(tv);
        }
        tableScores.addView(header);

        // Charger homework
        DocumentReference homeworkRef = db.collection("homework").document(studentCode);
        homeworkRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshotHomework, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(AddCalculateScoresActivity.this, "Error loading homework: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> homeworkData = snapshotHomework != null && snapshotHomework.exists() ? snapshotHomework.getData() : new HashMap<>();

                // Charger exercises
                DocumentReference exerciseRef = db.collection("exercises").document(studentCode);
                exerciseRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshotExercise, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(AddCalculateScoresActivity.this, "Error loading exercises: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Object> exerciseData = snapshotExercise != null && snapshotExercise.exists() ? snapshotExercise.getData() : new HashMap<>();

                        // Charger exams
                        DocumentReference examRef = db.collection("exams").document(studentCode);
                        examRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshotExam, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Toast.makeText(AddCalculateScoresActivity.this, "Error loading exams: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Map<String, Object> examData = snapshotExam != null && snapshotExam.exists() ? snapshotExam.getData() : new HashMap<>();

                                populateScoreTable(homeworkData, exerciseData, examData);
                            }
                        });
                    }
                });
            }
        });
    }

    private void populateScoreTable(Map<String, Object> homework, Map<String, Object> exercises, Map<String, Object> exams) {
        tableScores.removeViews(1, tableScores.getChildCount() - 1);

        for (int week = 1; week <= 6; week++) {
            String weekKey = "week" + week;

            Map<String, Object> hwWeek = (Map<String, Object>) homework.get(weekKey);
            Map<String, Object> exWeek = (Map<String, Object>) exercises.get(weekKey);
            Map<String, Object> examWeek = (Map<String, Object>) exams.get(weekKey);

            if (hwWeek == null && exWeek == null && examWeek == null) continue;

            // Pour chaque matière
            String[] subjects = {"Mathematics", "English", "French", "SET", "Kinyarwanda", "Social Studies"};

            for (String subject : subjects) {
                TableRow row = new TableRow(this);

                TextView tvWeek = new TextView(this);
                tvWeek.setText("Week " + week);
                tvWeek.setPadding(6, 6, 6, 6);
                row.addView(tvWeek);

                TextView tvSubject = new TextView(this);
                tvSubject.setText(subject);
                tvSubject.setPadding(6, 6, 6, 6);
                row.addView(tvSubject);

                double hwScore = hwWeek != null && hwWeek.get(subject) != null ? Double.parseDouble(hwWeek.get(subject).toString()) : 0.0;
                double exScore = exWeek != null && exWeek.get(subject) != null ? Double.parseDouble(exWeek.get(subject).toString()) : 0.0;
                double examScore = examWeek != null && examWeek.get(subject) != null ? Double.parseDouble(examWeek.get(subject).toString()) : 0.0;

                TextView tvHW = new TextView(this);
                tvHW.setText(String.valueOf(hwScore));
                tvHW.setPadding(6, 6, 6, 6);
                row.addView(tvHW);

                TextView tvEx = new TextView(this);
                tvEx.setText(String.valueOf(exScore));
                tvEx.setPadding(6, 6, 6, 6);
                row.addView(tvEx);

                TextView tvExam = new TextView(this);
                tvExam.setText(String.valueOf(examScore));
                tvExam.setPadding(6, 6, 6, 6);
                row.addView(tvExam);

                double total = hwScore + exScore + examScore;
                totalGeneral += total;

                totalPerSubject.put(subject, totalPerSubject.getOrDefault(subject, 0.0) + total);

                TextView tvTotal = new TextView(this);
                tvTotal.setText(String.valueOf(total));
                tvTotal.setPadding(6, 6, 6, 6);
                row.addView(tvTotal);

                tableScores.addView(row);
            }
        }

        // Affichage des totaux par matière et période
        StringBuilder periodSummary = new StringBuilder("Total per Subject:\n");
        for (Map.Entry<String, Double> entry : totalPerSubject.entrySet()) {
            periodSummary.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        periodSummary.append("Grand Total: ").append(totalGeneral);
        tvPeriodResults.setText(periodSummary.toString());
    }

    private void postScoresToFirestore(String studentCode) {
        Map<String, Object> scoreData = new HashMap<>();
        scoreData.put("totalPerSubject", totalPerSubject);
        scoreData.put("totalGeneral", totalGeneral);

        db.collection("student_scores").document(studentCode)
                .set(scoreData)
                .addOnSuccessListener(aVoid -> Toast.makeText(AddCalculateScoresActivity.this, "Scores posted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AddCalculateScoresActivity.this, "Failed to post scores: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
