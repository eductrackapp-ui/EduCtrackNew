package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.equipe7.eductrack.Auth.ActivityStudentRegister;
import com.equipe7.eductrack.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.*;

public class AdminStudentDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerStudents;
    private StudentAdapter adapter;
    private List<Student> studentList = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private EditText etSearch;
    private FloatingActionButton fabAddStudent;
    private ImageView btnBack, btnClearSearch;
    private TextView tvStudentCount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutClassFilters;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_student_dashboard);

        recyclerStudents = findViewById(R.id.recyclerStudents);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        etSearch = findViewById(R.id.etSearch);
        fabAddStudent = findViewById(R.id.fabAddStudent);
        btnBack = findViewById(R.id.btnBack);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        layoutClassFilters = findViewById(R.id.layoutClassFilters);

        db = FirebaseFirestore.getInstance();

        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(studentList);
        recyclerStudents.setAdapter(adapter);

        // FAB: Add student
        fabAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityStudentRegister.class);
            startActivity(intent);
        });

        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Clear search
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                searchStudents(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadStudents);

        // Load students on start
        loadStudents();
    }

    private void loadStudents() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    studentList.clear();
                    Set<String> classes = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Student student = doc.toObject(Student.class);
                        if (student != null) {
                            studentList.add(student);
                            if (student.getClasse() != null) classes.add(student.getClasse());
                        }
                    }
                    adapter.updateList(studentList);
                    tvStudentCount.setText(studentList.size() + " students");
                    layoutEmptyState.setVisibility(studentList.isEmpty() ? View.VISIBLE : View.GONE);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    setupClassFilters(new ArrayList<>(classes));
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show();
                });
    }

    private void searchStudents(String query) {
        if (query.isEmpty()) {
            adapter.updateList(studentList);
            layoutEmptyState.setVisibility(studentList.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }
        List<Student> filtered = new ArrayList<>();
        for (Student s : studentList) {
            if ((s.getName() != null && s.getName().toLowerCase().contains(query.toLowerCase()))
                    || (s.getCode() != null && s.getCode().toLowerCase().contains(query.toLowerCase()))
                    || (s.getClasse() != null && s.getClasse().toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(s);
            }
        }
        adapter.updateList(filtered);
        layoutEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupClassFilters(List<String> classes) {
        layoutClassFilters.removeAllViews();
        Collections.sort(classes);
        for (String classe : classes) {
            TextView chip = new TextView(this);
            chip.setText(classe);
            chip.setTextSize(13f);
            chip.setPadding(32, 12, 32, 12);
            chip.setBackgroundResource(R.drawable.circle_background); // drawable arrondi
            chip.setTextColor(getResources().getColor(R.color.colorPrimary));
            chip.setOnClickListener(v -> {
                etSearch.setText(classe);
            });
            layoutClassFilters.addView(chip);
        }
    }

    // Student model
    public static class Student {
        private String name, role, code, classe;
        public Student() {}
        public String getName() { return name; }
        public String getRole() { return role; }
        public String getCode() { return code; }
        public String getClasse() { return classe; }
    }

    // Adapter minimal pour afficher les infos de l'élève
    public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
        private List<Student> students;
        public StudentAdapter(List<Student> students) { this.students = new ArrayList<>(students); }
        public void updateList(List<Student> newList) {
            this.students = new ArrayList<>(newList);
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_student, parent, false);
            return new StudentViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            Student s = students.get(position);
            holder.tvName.setText(s.getName());
            holder.tvRole.setText(s.getRole());
            holder.tvCode.setText(s.getCode());
            holder.tvClasse.setText(s.getClasse());
        }
        @Override
        public int getItemCount() { return students.size(); }
        class StudentViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole, tvCode, tvClasse;
            StudentViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvRole = itemView.findViewById(R.id.tvRole);
                tvCode = itemView.findViewById(R.id.tvCode);
                tvClasse = itemView.findViewById(R.id.tvClasse);
            }
        }
    }
}