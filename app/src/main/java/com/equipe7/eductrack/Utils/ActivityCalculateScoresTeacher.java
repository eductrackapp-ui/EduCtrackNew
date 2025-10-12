package com.equipe7.eductrack.Utils;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
        import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class ActivityCalculateScoresTeacher extends AppCompatActivity {

    private TextInputEditText etStudentCode;
    private Spinner spinnerSubject, spinnerType;
    private MaterialButton btnLoadScores;
    private TableLayout tableScores;
    private TextView tvPeriodResults;
    private FirebaseFirestore db;

    private final String[] subjects = {
            "Science Élémentaire et Technologie", "Kinyarwanda", "English",
            "Social Studies and Religion", "Mathématique", "Français"
    };
    private final String[] types = {"Examens", "Exercices", "Homework"};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calculate_scores_teacher); // adapte le nom du layout

        etStudentCode = findViewById(R.id.etStudentCode);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerType = findViewById(R.id.spinnerType);
        btnLoadScores = findViewById(R.id.btnLoadScores);
        tableScores = findViewById(R.id.tableScores);
        tvPeriodResults = findViewById(R.id.tvPeriodResults);

        db = FirebaseFirestore.getInstance();

        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        btnLoadScores.setOnClickListener(v -> loadAndDisplayScores());
    }

    private void loadAndDisplayScores() {
        String studentCode = etStudentCode.getText().toString().trim();
        String subject = spinnerSubject.getSelectedItem().toString().toLowerCase(Locale.ROOT).replace(" ", "_").replace("é", "e").replace("è", "e");
        String type = spinnerType.getSelectedItem().toString();

        if (studentCode.isEmpty()) {
            Toast.makeText(this, "Enter student code", Toast.LENGTH_SHORT).show();
            return;
        }

        String collection;
        if (type.equals("Examens")) collection = "exams_resultas";
        else if (type.equals("Exercices")) collection = "exercices_resultats";
        else collection = "homework_resultats";

        db.collection(collection).document(studentCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        displayScores(documentSnapshot, subject);
                    } else {
                        Toast.makeText(this, "Student not found or no results", Toast.LENGTH_SHORT).show();
                        tableScores.removeAllViews();
                        tvPeriodResults.setText("");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show());
    }

    private void displayScores(DocumentSnapshot doc, String subject) {
        tableScores.removeAllViews();
        TableRow header = new TableRow(this);
        addCell(header, "Month");
        addCell(header, "Total Points");
        addCell(header, "Average /50");
        tableScores.addView(header);

        int[] monthTotals = new int[3];
        int[] monthCounts = new int[3];

        for (int month = 0; month < 3; month++) {
            int total = 0, count = 0;
            for (int w = 1 + month * 4; w <= 4 + month * 4; w++) {
                String key = subject + "_week" + w;
                Long val = doc.getLong(key);
                if (val != null) {
                    total += val;
                    count++;
                }
            }
            monthTotals[month] = total;
            monthCounts[month] = count;
            TableRow row = new TableRow(this);
            addCell(row, "Month " + (month + 1));
            addCell(row, String.valueOf(total));
            addCell(row, count > 0 ? String.valueOf(total / count) : "--");
            tableScores.addView(row);
        }

        // Affichage résumé
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append("Month ").append(i + 1).append(": ");
            sb.append(monthCounts[i] > 0 ? (monthTotals[i] / monthCounts[i]) : "--");
            sb.append(" /50\n");
        }
        tvPeriodResults.setText(sb.toString());
    }

    private void addCell(TableRow row, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(12, 8, 12, 8);
        tv.setTextSize(16);
        row.addView(tv);
    }
}