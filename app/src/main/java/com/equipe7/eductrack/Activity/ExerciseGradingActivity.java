package com.equipe7.eductrack.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.adapters.GradingAdapter;
import com.equipe7.eductrack.models.GradeEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseGradingActivity extends AppCompatActivity implements GradingAdapter.OnGradeChangeListener {

    private TextView tvExerciseTitle, tvClassName, tvGradedCount, tvAverageGrade;
    private ImageView btnBack, btnPublishGrades;
    private RecyclerView gradingRecyclerView;
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private GradingAdapter gradingAdapter;
    private List<GradeEntry> gradesList = new ArrayList<>();
    
    private String exerciseId;
    private String exerciseTitle;
    private String className;
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_grading);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        teacherId = auth.getCurrentUser().getUid();

        getIntentData();
        initializeViews();
        setupClickListeners();
        loadGradeEntries();
    }

    private void getIntentData() {
        exerciseId = getIntent().getStringExtra("exercise_id");
        exerciseTitle = getIntent().getStringExtra("exercise_title");
        className = getIntent().getStringExtra("class_name");
        // Get max points if provided
        double maxPoints = getIntent().getDoubleExtra("max_points", 100.0);
    }

    private void initializeViews() {
        tvExerciseTitle = findViewById(R.id.tvExerciseTitle);
        tvClassName = findViewById(R.id.tvClassName);
        tvGradedCount = findViewById(R.id.tvGradedCount);
        tvAverageGrade = findViewById(R.id.tvAverageGrade);
        btnBack = findViewById(R.id.btnBack);
        btnPublishGrades = findViewById(R.id.btnPublishGrades);
        gradingRecyclerView = findViewById(R.id.gradingRecyclerView);

        // Set exercise info
        tvExerciseTitle.setText(exerciseTitle);
        tvClassName.setText(className);

        // Setup RecyclerView
        gradingAdapter = new GradingAdapter(this, gradesList, this);
        gradingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gradingRecyclerView.setAdapter(gradingAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnPublishGrades.setOnClickListener(v -> publishGradesToParents());
    }

    private void loadGradeEntries() {
        firestore.collection("grades")
                .whereEqualTo("exerciseId", exerciseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    gradesList.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        GradeEntry gradeEntry = document.toObject(GradeEntry.class);
                        gradeEntry.setGradeId(document.getId());
                        gradesList.add(gradeEntry);
                    }
                    
                    updateGradingStatistics();
                    gradingAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading grades", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateGradingStatistics() {
        int totalStudents = gradesList.size();
        int gradedStudents = 0;
        double totalScore = 0;
        int scoredCount = 0;

        for (GradeEntry grade : gradesList) {
            if (grade.getStatus().equals("graded")) {
                gradedStudents++;
                totalScore += grade.getPercentage();
                scoredCount++;
            }
        }

        tvGradedCount.setText(gradedStudents + "/" + totalStudents + " Graded");
        
        if (scoredCount > 0) {
            double average = totalScore / scoredCount;
            tvAverageGrade.setText(String.format("%.1f%% Average", average));
        } else {
            tvAverageGrade.setText("No grades yet");
        }
    }

    @Override
    public void onGradeChanged(GradeEntry gradeEntry, double newScore, String feedback) {
        // Update the grade entry
        gradeEntry.setScore(newScore);
        gradeEntry.setFeedback(feedback);
        gradeEntry.setStatus("graded");
        gradeEntry.setGradedDate(new java.util.Date());

        // Save to Firebase
        firestore.collection("grades").document(gradeEntry.getGradeId())
                .set(gradeEntry)
                .addOnSuccessListener(aVoid -> {
                    updateGradingStatistics();
                    Toast.makeText(this, "Grade saved for " + gradeEntry.getStudentName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save grade", Toast.LENGTH_SHORT).show();
                });
    }

    private void publishGradesToParents() {
        // Check if all students are graded
        int gradedCount = 0;
        for (GradeEntry grade : gradesList) {
            if (grade.getStatus().equals("graded")) {
                gradedCount++;
            }
        }

        if (gradedCount == 0) {
            Toast.makeText(this, "Please grade at least one student before publishing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Publish Grades")
                .setMessage("Are you sure you want to publish " + gradedCount + " grades to parents? This will send notifications to all parents.")
                .setPositiveButton("Publish", (dialog, which) -> {
                    performGradePublication();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performGradePublication() {
        int publishedCount = 0;
        
        for (GradeEntry gradeEntry : gradesList) {
            if (gradeEntry.getStatus().equals("graded")) {
                // Update student's performance data
                updateStudentPerformance(gradeEntry);
                
                // Send notification to parent
                sendGradeNotificationToParent(gradeEntry);
                
                publishedCount++;
            }
        }

        // Make publishedCount effectively final for lambda
        final int finalPublishedCount = publishedCount;

        // Update exercise status
        firestore.collection("exercises").document(exerciseId)
                .update("status", "graded")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Published " + finalPublishedCount + " grades successfully!", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void updateStudentPerformance(GradeEntry gradeEntry) {
        // Create or update performance data for the student
        Map<String, Object> performanceData = new HashMap<>();
        performanceData.put("studentId", gradeEntry.getStudentId());
        performanceData.put("subject", gradeEntry.getSubject());
        performanceData.put("exerciseId", gradeEntry.getExerciseId());
        performanceData.put("exerciseTitle", gradeEntry.getExerciseTitle());
        performanceData.put("exerciseType", gradeEntry.getExerciseType());
        performanceData.put("score", gradeEntry.getScore());
        performanceData.put("maxScore", gradeEntry.getMaxScore());
        performanceData.put("percentage", gradeEntry.getPercentage());
        performanceData.put("grade", gradeEntry.getPercentage()); // For compatibility
        performanceData.put("feedback", gradeEntry.getFeedback());
        performanceData.put("teacherId", teacherId);
        performanceData.put("gradedDate", gradeEntry.getGradedDate());
        performanceData.put("status", "published");

        // Save to performance collection (this is what parents will see)
        firestore.collection("performance")
                .add(performanceData)
                .addOnSuccessListener(documentReference -> {
                    // Performance data saved successfully
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void sendGradeNotificationToParent(GradeEntry gradeEntry) {
        // Find the parent of this student
        firestore.collection("children")
                .document(gradeEntry.getStudentId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String parentId = documentSnapshot.getString("parentId");
                        
                        if (parentId != null) {
                            // Create notification for parent
                            Map<String, Object> notification = new HashMap<>();
                            notification.put("receiverId", parentId);
                            notification.put("senderId", teacherId);
                            notification.put("title", "New Grade Available");
                            notification.put("message", "Your child " + gradeEntry.getStudentName() + 
                                " received a grade of " + gradeEntry.getPercentageDisplay() + 
                                " for " + gradeEntry.getExerciseTitle());
                            notification.put("type", "grade");
                            notification.put("isRead", false);
                            notification.put("createdDate", new java.util.Date());
                            notification.put("studentId", gradeEntry.getStudentId());
                            notification.put("exerciseId", gradeEntry.getExerciseId());

                            firestore.collection("notifications")
                                    .add(notification)
                                    .addOnSuccessListener(docRef -> {
                                        // Notification sent successfully
                                    });
                        }
                    }
                });
    }
}
