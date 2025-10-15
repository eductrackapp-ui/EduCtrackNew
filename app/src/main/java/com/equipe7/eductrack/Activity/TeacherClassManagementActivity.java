package com.equipe7.eductrack.Activity;

import android.content.Intent;
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
import com.equipe7.eductrack.adapters.TeacherStudentAdapter;
import com.equipe7.eductrack.models.Child;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TeacherClassManagementActivity extends AppCompatActivity {

    private TextView tvClassName, tvStudentCount, tvClassAverage;
    private RecyclerView studentsRecyclerView;
    private ImageView btnBack, btnViewAnalytics;
    private CardView btnCreateExercise, btnGradeBook;
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private TeacherStudentAdapter studentAdapter;
    private List<Child> studentsList = new ArrayList<>();
    
    private String teacherClassName;
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_class_management);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        teacherId = auth.getCurrentUser().getUid();

        initializeViews();
        setupClickListeners();
        loadTeacherClass();
    }

    private void initializeViews() {
        tvClassName = findViewById(R.id.tvClassName);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        tvClassAverage = findViewById(R.id.tvClassAverage);
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        btnBack = findViewById(R.id.btnBack);
        btnCreateExercise = findViewById(R.id.btnCreateExercise);
        btnGradeBook = findViewById(R.id.btnGradeBook);
        btnViewAnalytics = findViewById(R.id.btnViewAnalytics);

        // Setup RecyclerView
        studentAdapter = new TeacherStudentAdapter(this, studentsList, this::onStudentClick);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentsRecyclerView.setAdapter(studentAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnCreateExercise.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateExerciseActivity.class);
            intent.putExtra("class_name", teacherClassName);
            intent.putParcelableArrayListExtra("students_list", new ArrayList<>(studentsList));
            startActivity(intent);
        });
        
        btnGradeBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherExercisesActivity.class);
            intent.putExtra("class_name", teacherClassName);
            startActivity(intent);
        });
        
        btnViewAnalytics.setOnClickListener(v -> {
            // For now, show a toast - analytics activity can be implemented later
            Toast.makeText(this, "Analytics feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadTeacherClass() {
        // First get teacher's assigned class
        firestore.collection("teachers").document(teacherId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        teacherClassName = documentSnapshot.getString("assignedClass");
                        String subject = documentSnapshot.getString("subject");
                        
                        if (teacherClassName != null) {
                            tvClassName.setText(teacherClassName + " - " + subject);
                            loadClassStudents();
                        } else {
                            createSampleTeacherData();
                        }
                    } else {
                        createSampleTeacherData();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading teacher data", Toast.LENGTH_SHORT).show();
                    createSampleTeacherData();
                });
    }

    private void loadClassStudents() {
        firestore.collection("children")
                .whereEqualTo("className", teacherClassName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    studentsList.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Child student = document.toObject(Child.class);
                        student.setChildId(document.getId());
                        
                        // Load student's recent performance
                        loadStudentPerformance(student);
                        studentsList.add(student);
                    }
                    
                    updateClassStatistics();
                    studentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show();
                    createSampleStudentData();
                });
    }

    private void loadStudentPerformance(Child student) {
        firestore.collection("grades")
                .whereEqualTo("studentId", student.getChildId())
                .orderBy("gradedDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalGrade = 0;
                    int gradeCount = 0;
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double score = doc.getDouble("score");
                        Double maxScore = doc.getDouble("maxScore");
                        
                        if (score != null && maxScore != null && maxScore > 0) {
                            totalGrade += (score / maxScore) * 100;
                            gradeCount++;
                        }
                    }
                    
                    if (gradeCount > 0) {
                        double average = totalGrade / gradeCount;
                        student.setOverallAverage(average);
                        student.setStatus(getPerformanceStatus(average));
                    }
                    
                    studentAdapter.notifyDataSetChanged();
                });
    }

    private String getPerformanceStatus(double average) {
        if (average >= 85) return "excellent";
        if (average >= 70) return "good";
        return "needs_improvement";
    }

    private void updateClassStatistics() {
        int totalStudents = studentsList.size();
        tvStudentCount.setText(totalStudents + " Students");
        
        if (totalStudents > 0) {
            double totalAverage = 0;
            int studentsWithGrades = 0;
            
            for (Child student : studentsList) {
                if (student.getOverallAverage() > 0) {
                    totalAverage += student.getOverallAverage();
                    studentsWithGrades++;
                }
            }
            
            if (studentsWithGrades > 0) {
                double classAverage = totalAverage / studentsWithGrades;
                tvClassAverage.setText(String.format("%.1f%% Average", classAverage));
            } else {
                tvClassAverage.setText("No grades yet");
            }
        }
    }

    private void createSampleTeacherData() {
        teacherClassName = "Class 6A";
        tvClassName.setText("Class 6A - Mathematics");
        createSampleStudentData();
    }

    private void createSampleStudentData() {
        studentsList.clear();
        
        // Create sample students
        Child student1 = new Child("student1", "Emma Johnson", "6A", "Grade 6");
        student1.setStudentCode("STU001AB");
        student1.setOverallAverage(88.5);
        student1.setStatus("excellent");
        student1.setBranch("Eden Family School Kacyiru");
        
        Child student2 = new Child("student2", "Michael Brown", "6A", "Grade 6");
        student2.setStudentCode("STU002CD");
        student2.setOverallAverage(76.2);
        student2.setStatus("good");
        student2.setBranch("Eden Family School Kacyiru");
        
        Child student3 = new Child("student3", "Sarah Wilson", "6A", "Grade 6");
        student3.setStudentCode("STU003EF");
        student3.setOverallAverage(92.1);
        student3.setStatus("excellent");
        student3.setBranch("Eden Family School Kacyiru");
        
        Child student4 = new Child("student4", "David Miller", "6A", "Grade 6");
        student4.setStudentCode("STU004GH");
        student4.setOverallAverage(65.8);
        student4.setStatus("needs_improvement");
        student4.setBranch("Eden Family School Kacyiru");
        
        studentsList.add(student1);
        studentsList.add(student2);
        studentsList.add(student3);
        studentsList.add(student4);
        
        updateClassStatistics();
        studentAdapter.notifyDataSetChanged();
    }

    private void onStudentClick(Child student) {
        // For now, show student details - later we can implement individual student view
        Toast.makeText(this, "Student: " + student.getName() + "\nOverall Average: " + 
                String.format("%.1f%%", student.getOverallAverage()), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from other activities
        if (teacherClassName != null) {
            loadClassStudents();
        }
    }
}
