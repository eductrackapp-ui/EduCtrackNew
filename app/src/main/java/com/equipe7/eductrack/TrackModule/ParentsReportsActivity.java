package com.equipe7.eductrack.TrackModule;

import android.os.Bundle;
import android.view.View;
import com.equipe7.eductrack.R;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.*;
import java.util.*;
import android.graphics.Color;

public class ParentsReportsActivity extends AppCompatActivity {

    private TextInputEditText etStudentCode;
    private MaterialButton btnLoadResults, btnUpdate;
    private TableLayout tableLessons;
    private TextView tvAnnualTotal, tvVerdict;
    private BarChart progressChart;

    // Table cell references
    private Map<String, EditText> editTexts = new HashMap<>();
    private Map<String, TextView> totalTexts = new HashMap<>();

    private final String[] subjects = {"Mathematics", "French", "English", "SET", "Social Studies", "Kinyarwanda"};
    private final String[] subjectKeys = {"Math", "French", "English", "Set", "Social", "Kinyarwanda"};
    private final String[] trimesters = {"T1", "T2", "T3"};
    private final String[] types = {"Exercices", "Dev", "Exam"};

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_parent_report_beautiful);

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
                btnUpdate = findViewById(R.id.btnUpdate);
                tvAnnualTotal = findViewById(R.id.tvAnnualTotal);
                tvVerdict = findViewById(R.id.tvVerdict);
                progressChart = findViewById(R.id.progressChart);
                
                if (progressChart != null) {
                    setupChart();
                }
                
                if (btnLoadResults != null) {
                    btnLoadResults.setOnClickListener(v -> loadStudentReport());
                }
                if (btnUpdate != null) {
                    btnUpdate.setOnClickListener(v -> loadStudentReport());
                }
            } catch (Exception e) {
                Toast.makeText(this, "Some features may not be available", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error loading page: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void setupChart() {
        if (progressChart != null) {
            progressChart.getDescription().setEnabled(false);
            progressChart.setFitBars(true);
        }
    }
    
    private void loadSimplifiedReport(String code) {
        Toast.makeText(this, "Loading report data...", Toast.LENGTH_SHORT).show();
    }

    private void loadStudentReport() {
        String studentCode = etStudentCode.getText() != null ? etStudentCode.getText().toString().trim() : "";
        if (studentCode.isEmpty()) {
            Toast.makeText(this, "Enter student code", Toast.LENGTH_SHORT).show();
            return;
        }
        btnLoadResults.setEnabled(false);
        btnUpdate.setEnabled(false);
        clearTable();

        db.collection("repport").document(studentCode)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        fillTableWithData(document.getData());
                        btnUpdate.setEnabled(true);
                    } else {
                        Toast.makeText(this, "Student not found in repport", Toast.LENGTH_LONG).show();
                    }
                    btnLoadResults.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnLoadResults.setEnabled(true);
                });
    }

    private void clearTable() {
        if (tvAnnualTotal != null) tvAnnualTotal.setText("Annual Total: -- | Annual %: --%");
        if (tvVerdict != null) tvVerdict.setText("Verdict: --");
        if (progressChart != null) progressChart.clear();
    }

    private void fillTableWithData(Map<String, Object> data) {
        double annualTotal = 0;
        double annualMax = 0;
        Map<String, Double> subjectTotals = new LinkedHashMap<>();

        for (int s = 0; s < subjectKeys.length; s++) {
            String subject = subjectKeys[s];
            double subjectTotal = 0;
            double subjectMax = 0;
            for (int t = 0; t < trimesters.length; t++) {
                String trimester = trimesters[t];
                double trimesterTotal = 0;
                double trimesterMax = 0;
                for (String type : types) {
                    String key = subject.toLowerCase() + "_" + type.toLowerCase() + "_" + trimester.toLowerCase();
                    String idName = "et" + subject + type + trimester;
                    double value = 0;
                    if (data != null && data.containsKey(key)) {
                        try {
                            value = Double.parseDouble(data.get(key).toString());
                        } catch (Exception ignored) {}
                    }
                    if (editTexts.containsKey(idName)) editTexts.get(idName).setText(value == 0 ? "" : String.valueOf(value));
                    trimesterTotal += value;
                    trimesterMax += 20; // Suppose each type is out of 20
                }
                String totalId = "tv" + subject + "Total" + trimester;
                if (totalTexts.containsKey(totalId)) totalTexts.get(totalId).setText(String.valueOf(trimesterTotal));
                subjectTotal += trimesterTotal;
                subjectMax += trimesterMax;
            }
            subjectTotals.put(subjects[s], subjectTotal);
            annualTotal += subjectTotal;
            annualMax += subjectMax;
        }

        // Annual summary
        double percent = annualMax > 0 ? (annualTotal * 100.0 / annualMax) : 0;
        if (tvAnnualTotal != null) {
            tvAnnualTotal.setText("Annual Total: " + annualTotal + " | Annual %: " + String.format(Locale.US, "%.1f", percent) + "%");
        }
        String verdict = percent >= 50 ? "Pass" : "Fail";
        if (tvVerdict != null) {
            tvVerdict.setText("Verdict: " + verdict);
        }

        // Bar chart
        if (progressChart != null && !subjectTotals.isEmpty()) {
            showBarChart(subjectTotals, annualMax / subjectTotals.size());
        }
    }

    private void showBarChart(Map<String, Double> subjectTotals, double maxPerSubject) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Double> entry : subjectTotals.entrySet()) {
            double percent = maxPerSubject > 0 ? (entry.getValue() * 100.0 / maxPerSubject) : 0;
            entries.add(new BarEntry(i, (float) percent));
            labels.add(entry.getKey());
            i++;
        }
        BarDataSet dataSet = new BarDataSet(entries, "Subject %");

        // Couleurs personnalisées pour chaque matière (ordre: Math, French, English, SET, Social Studies, Kinyarwanda)
        int[] colors = new int[]{
                Color.parseColor("#1976D2"), // Math: bleu
                Color.parseColor("#E53935"), // French: rouge
                Color.parseColor("#43A047"), // English: vert
                Color.parseColor("#FBC02D"), // SET: jaune
                Color.parseColor("#8E24AA"), // Social Studies: violet
                Color.parseColor("#00897B")  // Kinyarwanda: turquoise
        };
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        progressChart.setData(barData);
        XAxis xAxis = progressChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        progressChart.getAxisRight().setEnabled(false);
        progressChart.getDescription().setEnabled(false);
        progressChart.setFitBars(true);
        progressChart.invalidate();
    }
}