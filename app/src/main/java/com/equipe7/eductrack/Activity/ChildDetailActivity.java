package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.TrackModule.StudentExamResultsActivity;
import com.equipe7.eductrack.TrackModule.HomeworkActivity;
import com.equipe7.eductrack.TrackModule.ParentsReportsActivity;
import com.equipe7.eductrack.TrackModule.StudentExercisesActivity;
import com.equipe7.eductrack.Adapter.SubjectPerformanceAdapter;
import com.equipe7.eductrack.models.PerformanceData;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildDetailActivity extends AppCompatActivity {

    private TextView childName, childClass, childGrade, childBranch, studentCode;
    private TextView overallGrade, performanceStatus, weeklyAverage, monthlyAverage;
    private ImageView childAvatar;
    private RecyclerView subjectPerformanceRecycler;
    private SubjectPerformanceAdapter subjectAdapter;
    private FirebaseFirestore firestore;
    
    private String childId, childNameStr, childClassStr, childGradeStr, childBranchStr, studentCodeStr, statusStr;
    private double overallAverageValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_detail);

        firestore = FirebaseFirestore.getInstance();
        
        // Get data from intent
        getIntentData();
        
        // Initialize views
        initializeViews();
        
        // Setup click listeners
        setupClickListeners();
        
        // Display child information
        displayChildInfo();
        
        // Load performance data
        loadPerformanceData();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        childId = intent.getStringExtra("child_id");
        childNameStr = intent.getStringExtra("child_name");
        childClassStr = intent.getStringExtra("child_class");
        childGradeStr = intent.getStringExtra("child_grade");
        childBranchStr = intent.getStringExtra("child_branch");
        studentCodeStr = intent.getStringExtra("student_code");
        statusStr = intent.getStringExtra("status");
        overallAverageValue = intent.getDoubleExtra("overall_average", 0.0);
    }

    private void initializeViews() {
        // Header views
        childName = findViewById(R.id.childName);
        childClass = findViewById(R.id.childClass);
        childGrade = findViewById(R.id.childGrade);
        childBranch = findViewById(R.id.childBranch);
        studentCode = findViewById(R.id.studentCode);
        overallGrade = findViewById(R.id.overallGrade);
        performanceStatus = findViewById(R.id.performanceStatus);
        weeklyAverage = findViewById(R.id.weeklyAverage);
        monthlyAverage = findViewById(R.id.monthlyAverage);
        childAvatar = findViewById(R.id.childAvatar);
        
        // RecyclerView for subject performance
        subjectPerformanceRecycler = findViewById(R.id.subjectPerformanceRecycler);
        subjectAdapter = new SubjectPerformanceAdapter(this);
        subjectPerformanceRecycler.setLayoutManager(new LinearLayoutManager(this));
        subjectPerformanceRecycler.setAdapter(subjectAdapter);
    }

    private void setupClickListeners() {
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Quick action buttons
        findViewById(R.id.btnViewExams).setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentExamResultsActivity.class);
            intent.putExtra("student_code", studentCodeStr);
            startActivity(intent);
        });
        
        findViewById(R.id.btnViewHomework).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeworkActivity.class));
        });
        
        findViewById(R.id.btnViewReports).setOnClickListener(v -> {
            startActivity(new Intent(this, ParentsReportsActivity.class));
        });
        
        findViewById(R.id.btnViewExercises).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentExercisesActivity.class));
        });
        
        // Student code copy functionality
        findViewById(R.id.btnCopyCode).setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Student Code", studentCodeStr);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Student code copied to clipboard", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayChildInfo() {
        childName.setText(childNameStr);
        childClass.setText(childClassStr);
        childGrade.setText(childGradeStr);
        childBranch.setText(getBranchShortName(childBranchStr));
        studentCode.setText(studentCodeStr);
        overallGrade.setText(String.format("%.1f%%", overallAverageValue));
        
        // Set performance status with color
        if (statusStr != null) {
            performanceStatus.setText(statusStr.toUpperCase());
            performanceStatus.setTextColor(Color.parseColor(getStatusColor(statusStr)));
        }
    }

    private void loadPerformanceData() {
        // Load real performance data from Firestore (teacher-assigned grades)
        if (childId != null) {
            firestore.collection("performance")
                    .whereEqualTo("studentId", childId)
                    .whereEqualTo("status", "published")
                    .orderBy("gradedDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<PerformanceData> performanceList = new ArrayList<>();
                            double totalGrade = 0;
                            int subjectCount = 0;
                            Map<String, Double> subjectAverages = new HashMap<>();
                            Map<String, Integer> subjectCounts = new HashMap<>();
                            
                            for (var doc : queryDocumentSnapshots.getDocuments()) {
                                String subject = doc.getString("subject");
                                Double percentage = doc.getDouble("percentage");
                                String exerciseTitle = doc.getString("exerciseTitle");
                                String exerciseType = doc.getString("exerciseType");
                                String feedback = doc.getString("feedback");
                                
                                if (subject != null && percentage != null) {
                                    // Create performance data with teacher feedback
                                    PerformanceData perfData = new PerformanceData(childId, subject, percentage, exerciseType != null ? exerciseType : "exercise");
                                    perfData.setTitle(exerciseTitle != null ? exerciseTitle : "Assignment");
                                    perfData.setFeedback(feedback);
                                    performanceList.add(perfData);
                                    
                                    // Calculate subject averages
                                    subjectAverages.put(subject, subjectAverages.getOrDefault(subject, 0.0) + percentage);
                                    subjectCounts.put(subject, subjectCounts.getOrDefault(subject, 0) + 1);
                                    
                                    totalGrade += percentage;
                                    subjectCount++;
                                }
                            }
                            
                            // Update averages
                            if (subjectCount > 0) {
                                double overallAverage = totalGrade / subjectCount;
                                weeklyAverage.setText(String.format("%.1f%%", overallAverage));
                                monthlyAverage.setText(String.format("%.1f%%", overallAverage));
                                
                                // Update child's overall average
                                updateChildOverallAverage(overallAverage);
                            }
                            
                            // Create grades map for adapter (subject averages)
                            Map<String, Double> gradesMap = new HashMap<>();
                            for (Map.Entry<String, Double> entry : subjectAverages.entrySet()) {
                                String subject = entry.getKey();
                                double total = entry.getValue();
                                int count = subjectCounts.get(subject);
                                gradesMap.put(subject, total / count);
                            }
                            
                            subjectAdapter.updateData(gradesMap, performanceList);
                        } else {
                            // No teacher grades yet, show message
                            createNoGradesMessage();
                        }
                    })
                    .addOnFailureListener(e -> createNoGradesMessage());
        } else {
            createNoGradesMessage();
        }
    }
    
    private void updateChildOverallAverage(double average) {
        // Update the child's overall average in Firestore
        if (childId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("overallAverage", average);
            
            String status;
            if (average >= 85) status = "excellent";
            else if (average >= 70) status = "good";
            else status = "needs_improvement";
            updates.put("status", status);
            
            firestore.collection("children").document(childId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Updated successfully
                    });
        }
    }
    
    private void createNoGradesMessage() {
        weeklyAverage.setText("No grades yet");
        monthlyAverage.setText("Waiting for teacher");
        
        // Create empty performance list with message
        List<PerformanceData> emptyList = new ArrayList<>();
        Map<String, Double> emptyGrades = new HashMap<>();
        subjectAdapter.updateData(emptyGrades, emptyList);
    }

    private void createSamplePerformanceData() {
        List<PerformanceData> performanceList = new ArrayList<>();
        performanceList.add(new PerformanceData(childId, "Mathematics", 88.0, "exam"));
        performanceList.add(new PerformanceData(childId, "English", 85.0, "exam"));
        performanceList.add(new PerformanceData(childId, "Science", 82.0, "exam"));
        performanceList.add(new PerformanceData(childId, "History", 80.0, "exam"));
        performanceList.add(new PerformanceData(childId, "Geography", 86.0, "exam"));
        
        // Set sample averages
        weeklyAverage.setText("84.2%");
        monthlyAverage.setText("86.1%");
        
        // Create grades map for adapter
        Map<String, Double> sampleGrades = new HashMap<>();
        sampleGrades.put("Mathematics", 88.0);
        sampleGrades.put("English", 85.0);
        sampleGrades.put("Science", 82.0);
        sampleGrades.put("History", 80.0);
        sampleGrades.put("Geography", 86.0);
        
        subjectAdapter.updateData(sampleGrades, performanceList);
    }

    private String getBranchShortName(String fullBranchName) {
        if (fullBranchName == null) return "Unknown Campus";
        
        if (fullBranchName.contains("Kacyiru")) {
            return "Kacyiru Campus";
        } else if (fullBranchName.contains("Gisozi")) {
            return "Gisozi Campus";
        } else if (fullBranchName.contains("Kimisagara")) {
            return "Kimisagara Campus";
        }
        return "Eden Campus";
    }

    private String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "excellent": return "#4CAF50"; // Green
            case "good": return "#2196F3"; // Blue
            case "needs_improvement": return "#FF9800"; // Orange
            default: return "#9E9E9E"; // Gray
        }
    }
}
