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

import com.equipe7.eductrack.Auth.ActivityTeacherRegister;
import com.equipe7.eductrack.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.*;

public class AdminTeacherListActivity extends AppCompatActivity {

    private RecyclerView recyclerTeachers;
    private TeacherAdapter adapter;
    private List<Teacher> teacherList = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private EditText etSearch;
    private FloatingActionButton fabAddTeacher;
    private ImageView btnBack, btnClearSearch;
    private TextView tvTeacherCount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutSubjectFilters;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_teacher_dashboard);

        recyclerTeachers = findViewById(R.id.recyclerTeachers);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        etSearch = findViewById(R.id.etSearch);
        fabAddTeacher = findViewById(R.id.fabAddTeacher);
        btnBack = findViewById(R.id.btnBack);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        tvTeacherCount = findViewById(R.id.tvTeacherCount);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        layoutSubjectFilters = findViewById(R.id.layoutSubjectFilters);

        db = FirebaseFirestore.getInstance();

        recyclerTeachers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeacherAdapter(teacherList);
        recyclerTeachers.setAdapter(adapter);

        // FAB: Add teacher
        fabAddTeacher.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityTeacherRegister.class);
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
                searchTeachers(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadTeachers);

        // Load teachers on start
        loadTeachers();
    }

    private void loadTeachers() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("teachers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    teacherList.clear();
                    Set<String> subjects = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Teacher teacher = doc.toObject(Teacher.class);
                        if (teacher != null) {
                            teacherList.add(teacher);
                            if (teacher.getClasse() != null) subjects.add(teacher.getClasse());
                        }
                    }
                    adapter.updateList(teacherList);
                    tvTeacherCount.setText(teacherList.size() + " teachers");
                    layoutEmptyState.setVisibility(teacherList.isEmpty() ? View.VISIBLE : View.GONE);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    setupSubjectFilters(new ArrayList<>(subjects));
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Error loading teachers", Toast.LENGTH_SHORT).show();
                });
    }

    private void searchTeachers(String query) {
        if (query.isEmpty()) {
            adapter.updateList(teacherList);
            layoutEmptyState.setVisibility(teacherList.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }
        List<Teacher> filtered = new ArrayList<>();
        for (Teacher t : teacherList) {
            if ((t.getName() != null && t.getName().toLowerCase().contains(query.toLowerCase()))
                    || (t.getCode() != null && t.getCode().toLowerCase().contains(query.toLowerCase()))
                    || (t.getClasse() != null && t.getClasse().toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(t);
            }
        }
        adapter.updateList(filtered);
        layoutEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupSubjectFilters(List<String> subjects) {
        layoutSubjectFilters.removeAllViews();
        Collections.sort(subjects);
        for (String subject : subjects) {
            TextView chip = new TextView(this);
            chip.setText(subject);
            chip.setTextSize(13f);
            chip.setPadding(32, 12, 32, 12);
            chip.setBackgroundResource(R.drawable.circle_background); // drawable arrondi
            chip.setTextColor(getResources().getColor(R.color.colorPrimary));
            chip.setOnClickListener(v -> {
                etSearch.setText(subject);
            });
            layoutSubjectFilters.addView(chip);
        }
    }

    // Teacher model
    public static class Teacher {
        private String name, role, code, poste, classe;
        public Teacher() {}
        public String getName() { return name; }
        public String getRole() { return role; }
        public String getCode() { return code; }
        public String getPoste() { return poste; }
        public String getClasse() { return classe; }
    }

    // Adapter minimal pour afficher les infos de l'enseignant
    public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {
        private List<Teacher> teachers;
        public TeacherAdapter(List<Teacher> teachers) { this.teachers = new ArrayList<>(teachers); }
        public void updateList(List<Teacher> newList) {
            this.teachers = new ArrayList<>(newList);
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_teacher, parent, false);
            return new TeacherViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
            Teacher t = teachers.get(position);
            holder.tvName.setText(t.getName());
            holder.tvRole.setText(t.getRole());
            holder.tvCode.setText(t.getCode());
            holder.tvPoste.setText(t.getPoste());
            holder.tvClasse.setText(t.getClasse());
        }
        @Override
        public int getItemCount() { return teachers.size(); }
        class TeacherViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole, tvCode, tvPoste, tvClasse;
            TeacherViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvRole = itemView.findViewById(R.id.tvRole);
                tvCode = itemView.findViewById(R.id.tvCode);
                tvPoste = itemView.findViewById(R.id.tvPoste);
                tvClasse = itemView.findViewById(R.id.tvClasse);
            }
        }
    }
}