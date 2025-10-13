package com.equipe7.eductrack.TrackModule;

import com.equipe7.eductrack.R;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
        import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.*;
        import java.util.*;

public class ActivityAddReportTeacher extends AppCompatActivity {

    private TextInputEditText etStudentCode;
    private MaterialButton btnLoadResults, btnCalculate, btnSaveFirebase;
    private TextView tvAnnualTotal, tvVerdict;

    private Map<String, Map<String, EditText>> subjectInputs = new HashMap<>();
    private Map<String, Map<String, TextView>> subjectTotals = new HashMap<>();

    private final String[] subjects = {"Mathematics", "French", "English", "SET", "Social Studies", "Kinyarwanda"};
    private final String[] periods = {"P1", "P2", "P3"};
    private final String[] types = {"Exo", "Dev", "Exam"};

    private FirebaseFirestore db;

    // Variables pour le rapport final
    private double annualTotal = 0;
    private int annualCount = 0;
    private String annualPercentStr = "--";
    private String verdict = "Verdict: --";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scores_report_teacher); // adapte le nom du layout

        db = FirebaseFirestore.getInstance();

        etStudentCode = findViewById(R.id.etStudentCode);
        btnLoadResults = findViewById(R.id.btnLoadResults);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnSaveFirebase = findViewById(R.id.btnSaveFirebase);
        tvAnnualTotal = findViewById(R.id.tvAnnualTotal);
        tvVerdict = findViewById(R.id.tvVerdict);

        // Initialiser les champs du tableau
        for (String subject : subjects) {
            Map<String, EditText> periodInputs = new HashMap<>();
            Map<String, TextView> periodTotals = new HashMap<>();
            for (String period : periods) {
                for (String type : types) {
                    String id = "et" + subject.replace(" ", "") + type + period;
                    int resId = getResources().getIdentifier(id, "id", getPackageName());
                    periodInputs.put(type + period, findViewById(resId));
                }
                String totalId = "tv" + subject.replace(" ", "") + "Total" + period;
                int totalResId = getResources().getIdentifier(totalId, "id", getPackageName());
                periodTotals.put(period, findViewById(totalResId));
            }
            subjectInputs.put(subject, periodInputs);
            subjectTotals.put(subject, periodTotals);
        }

        btnLoadResults.setOnClickListener(v -> loadStudentScores());
        btnCalculate.setOnClickListener(v -> calculateReport());
        btnSaveFirebase.setOnClickListener(v -> saveReportToFirebase());
    }

    private void loadStudentScores() {
        String studentCode = etStudentCode.getText().toString().trim();
        if (studentCode.isEmpty()) {
            Toast.makeText(this, "Enter student code", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("scores").document(studentCode)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        for (String subject : subjects) {
                            for (String period : periods) {
                                for (String type : types) {
                                    String key = subject + "_" + type + "_" + period;
                                    Object value = document.get(key);
                                    EditText et = subjectInputs.get(subject).get(type + period);
                                    if (et != null) {
                                        et.setText(value != null ? String.valueOf(value) : "");
                                    }
                                }
                            }
                        }
                        Toast.makeText(this, "Scores loaded", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No data found for this student", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading scores", Toast.LENGTH_SHORT).show());
    }

    private void calculateReport() {
        annualTotal = 0;
        annualCount = 0;
        boolean missingData = false;

        for (String subject : subjects) {
            for (String period : periods) {
                double sum = 0;
                int count = 0;
                for (String type : types) {
                    EditText et = subjectInputs.get(subject).get(type + period);
                    String val = et.getText().toString().trim();
                    if (TextUtils.isEmpty(val) || val.equals("?")) {
                        missingData = true;
                        et.setText("?");
                    } else {
                        try {
                            double score = Double.parseDouble(val);
                            sum += score;
                            count++;
                        } catch (NumberFormatException e) {
                            et.setText("?");
                            missingData = true;
                        }
                    }
                }
                TextView tvTotal = subjectTotals.get(subject).get(period);
                if (count == types.length) {
                    tvTotal.setText(String.valueOf(sum));
                    annualTotal += sum;
                    annualCount += count;
                } else {
                    tvTotal.setText("?");
                }
            }
        }

        annualPercentStr = annualCount > 0 ? String.format("%.2f", (annualTotal / (annualCount * 100)) * 100) : "--";
        String annualTotalStr = annualCount > 0 ? String.valueOf(annualTotal) : "--";
        tvAnnualTotal.setText("Annual Total: " + annualTotalStr + " | Annual %: " + annualPercentStr + "%");

        if (annualCount == 0 || missingData) {
            verdict = "Verdict: --";
        } else {
            double percent = (annualTotal / (annualCount * 100)) * 100;
            verdict = percent < 50 ? "Verdict: Failed" : "Verdict: Passed";
        }
        tvVerdict.setText(verdict);
    }

    private void saveReportToFirebase() {
        String studentCode = etStudentCode.getText().toString().trim();
        if (studentCode.isEmpty()) {
            Toast.makeText(this, "Enter student code", Toast.LENGTH_SHORT).show();
            return;
        }
        // Calculer le rapport avant de sauvegarder
        calculateReport();

        Map<String, Object> reportData = new HashMap<>();
        for (String subject : subjects) {
            for (String period : periods) {
                for (String type : types) {
                    EditText et = subjectInputs.get(subject).get(type + period);
                    String val = et.getText().toString().trim();
                    String key = subject + "_" + type + "_" + period;
                    reportData.put(key, val);
                }
                // Ajouter le total de la période
                TextView tvTotal = subjectTotals.get(subject).get(period);
                String totalKey = subject + "_Total_" + period;
                reportData.put(totalKey, tvTotal.getText().toString());
            }
        }
        // Ajouter le total annuel, le pourcentage et le verdict
        reportData.put("AnnualTotal", String.valueOf(annualTotal));
        reportData.put("AnnualPercent", annualPercentStr);
        reportData.put("Verdict", verdict);

        db.collection("reports").document(studentCode)
                .set(reportData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Report saved to Firebase", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving report", Toast.LENGTH_SHORT).show());
    }
}