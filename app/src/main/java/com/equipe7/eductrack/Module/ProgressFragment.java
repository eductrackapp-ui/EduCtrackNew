package com.equipe7.eductrack.Module;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.equipe7.eductrack.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fragment to display student progress
public class ProgressFragment extends Fragment {

    private BarChart progressChart;
    private Spinner spinnerStudents;
    private TextView tvProgressInfo;
    private FirebaseFirestore db;

    private List<String> studentNames = new ArrayList<>();
    private Map<String, String> studentMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_progress, container, false);

        progressChart = v.findViewById(R.id.progressChart);
        spinnerStudents = v.findViewById(R.id.spinnerStudents);
        tvProgressInfo = v.findViewById(R.id.tvProgressInfo);

        db = FirebaseFirestore.getInstance();

        loadStudents();

        return v;
    }

    // Load students from Firestore
    private void loadStudents() {
        db.collection("students")
                .get()
                .addOnSuccessListener(query -> {
                    studentNames.clear();
                    studentMap.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        String studentId = doc.getId();
                        String name = doc.getString("name"); // Ensure "name" field exists
                        if (name == null) name = studentId;
                        studentNames.add(name);
                        studentMap.put(name, studentId);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            studentNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerStudents.setAdapter(adapter);

                    spinnerStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedName = studentNames.get(position);
                            String studentId = studentMap.get(selectedName);
                            tvProgressInfo.setText("Progress of " + selectedName);
                            loadChartData(studentId);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                });
    }

    // Load student performance and display in chart
    private void loadChartData(String studentId) {
        db.collection("students")
                .document(studentId)
                .collection("performances")
                .get()
                .addOnSuccessListener(query -> {
                    List<BarEntry> entries = new ArrayList<>();
                    int index = 0;

                    for (QueryDocumentSnapshot doc : query) {
                        Long avg = doc.getLong("average");
                        if (avg != null) {
                            entries.add(new BarEntry(index, avg));
                            index++;
                        }
                    }

                    BarDataSet dataSet = new BarDataSet(entries, "Progress (%)");
                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                    dataSet.setValueTextSize(14f);

                    BarData data = new BarData(dataSet);
                    data.setBarWidth(0.9f);

                    progressChart.setData(data);
                    progressChart.setFitBars(true);

                    // Configure chart UI
                    progressChart.getDescription().setEnabled(false);

                    Legend legend = progressChart.getLegend();
                    legend.setEnabled(true);
                    legend.setTextSize(12f);
                    legend.setTextColor(Color.BLACK);
                    legend.setForm(Legend.LegendForm.CIRCLE);
                    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

                    XAxis xAxis = progressChart.getXAxis();
                    xAxis.setGranularity(1f);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(false);

                    progressChart.getAxisLeft().setAxisMaximum(100f);
                    progressChart.getAxisLeft().setAxisMinimum(0f);
                    progressChart.getAxisRight().setEnabled(false);

                    progressChart.animateY(1500);
                    progressChart.invalidate();
                });
    }
}
