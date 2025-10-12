package com.equipe7.eductrack.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.equipe7.eductrack.Adapter.TeacherAdapter;
import com.equipe7.eductrack.Auth.ActivityTeacherRegister;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.Utils.Teacher;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TeacherDashboard extends AppCompatActivity implements TeacherAdapter.OnTeacherActionListener {

    private static final String TAG = "TeacherDashboard";
    
    private RecyclerView recyclerTeachers;
    private TeacherAdapter adapter;
    private List<Teacher> teacherList;
    private Button btnCreateTeacher;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_teacher_dashboard);

        try {
            // Initialiser Firebase
            FirebaseApp.initializeApp(this);
            db = FirebaseFirestore.getInstance();

            // Initialiser les vues
            initViews();
            setupRecyclerView();
            setupSwipeRefresh();
            setupClickListeners();

            // Charger les enseignants depuis Firestore
            loadTeachers();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            showError("Failed to initialize dashboard");
        }
    }

    private void initViews() {
        recyclerTeachers = findViewById(R.id.recyclerTeachers);
        btnCreateTeacher = findViewById(R.id.btnCreateTeacher);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupRecyclerView() {
        recyclerTeachers.setLayoutManager(new LinearLayoutManager(this));
        teacherList = new ArrayList<>();
        adapter = new TeacherAdapter(teacherList, this);
        adapter.setOnTeacherActionListener(this);
        recyclerTeachers.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadTeachers);
            swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light)
            );
        }
    }

    private void setupClickListeners() {
        btnCreateTeacher.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboard.this, ActivityTeacherRegister.class);
            startActivity(intent);
        });
    }

    private void loadTeachers() {
        showLoading(true);
        
        if (db == null) {
            showError("Database not initialized");
            return;
        }

        CollectionReference teachersRef = db.collection("teachers");

        teachersRef.get().addOnCompleteListener((Task<QuerySnapshot> task) -> {
            showLoading(false);
            
            if (task.isSuccessful() && task.getResult() != null) {
                teacherList.clear();
                
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Teacher teacher = createTeacherFromDocument(document);
                        if (teacher != null) {
                            teacherList.add(teacher);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing teacher document: " + document.getId(), e);
                    }
                }
                
                updateUI();
                Log.d(TAG, "Loaded " + teacherList.size() + " teachers");
            } else {
                String errorMsg = task.getException() != null ? 
                    task.getException().getMessage() : "Unknown error";
                Log.e(TAG, "Error loading teachers: ", task.getException());
                showError("Failed to load teachers: " + errorMsg);
            }
        });
    }

    private Teacher createTeacherFromDocument(QueryDocumentSnapshot document) {
        String teacherCode = document.getString("teacherCode");
        String firstName = document.getString("firstName");
        String lastName = document.getString("lastName");
        String classLevel = document.getString("classLevel");
        String email = document.getString("email");
        String phone = document.getString("phone");
        String subject = document.getString("subject");
        Boolean isOnline = document.getBoolean("isOnline");
        
        if (teacherCode == null || firstName == null || lastName == null) {
            Log.w(TAG, "Missing required fields for teacher: " + document.getId());
            return null;
        }
        
        String fullName = firstName + " " + lastName;
        Teacher teacher = new Teacher(teacherCode, fullName, classLevel, email, phone, subject);
        
        if (isOnline != null) {
            teacher.setOnline(isOnline);
        }
        
        // Set last seen if available
        com.google.firebase.Timestamp lastSeenTimestamp = document.getTimestamp("lastSeen");
        if (lastSeenTimestamp != null) {
            teacher.setLastSeen(lastSeenTimestamp.toDate());
        }
        
        return teacher;
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (swipeRefreshLayout != null && !show) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void updateUI() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(teacherList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        
        if (recyclerTeachers != null) {
            recyclerTeachers.setVisibility(teacherList.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showLoading(false);
    }

    // TeacherAdapter.OnTeacherActionListener implementation
    @Override
    public void onViewProfile(Teacher teacher) {
        // TODO: Implement profile view
        Toast.makeText(this, "View profile: " + teacher.getDisplayName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditTeacher(Teacher teacher) {
        // TODO: Implement teacher editing
        Toast.makeText(this, "Edit teacher: " + teacher.getDisplayName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteTeacher(Teacher teacher) {
        showDeleteConfirmation(teacher);
    }

    @Override
    public void onSendMessage(Teacher teacher) {
        // TODO: Implement messaging
        Toast.makeText(this, "Send message to: " + teacher.getDisplayName(), Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation(Teacher teacher) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Teacher")
            .setMessage("Are you sure you want to delete " + teacher.getDisplayName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> deleteTeacher(teacher))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteTeacher(Teacher teacher) {
        // TODO: Implement teacher deletion from Firestore
        Toast.makeText(this, "Delete functionality not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadTeachers();
    }
}
