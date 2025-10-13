package com.equipe7.eductrack.Activity;

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
import com.equipe7.eductrack.R;
import com.google.firebase.firestore.*;
import java.util.*;

public class StudentListActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView btnClearSearch, btnBack;
    private RecyclerView recyclerStudents;
    private TextView tvStudentCount, tvSelectedStudentName, tvSelectedStudentClass, tvSelectedStudentCode;
    private LinearLayout cardSelectedStudent;
    private Button btnSaveSelectedStudent;
    private StudentAdapter studentAdapter;
    private List<Student> studentList = new ArrayList<>();
    private List<Student> filteredList = new ArrayList<>();
    private Student selectedStudent = null;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        // Initialisation des vues
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnBack = findViewById(R.id.btnBack);
        recyclerStudents = findViewById(R.id.recyclerStudents);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        cardSelectedStudent = findViewById(R.id.cardSelectedStudent);
        tvSelectedStudentName = findViewById(R.id.tvSelectedStudentName);
        tvSelectedStudentClass = findViewById(R.id.tvSelectedStudentClass);
        tvSelectedStudentCode = findViewById(R.id.tvSelectedStudentCode);
        btnSaveSelectedStudent = findViewById(R.id.btnSaveSelectedStudent);

        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter = new StudentAdapter(filteredList, this::showSelectedStudent) {
            @NonNull
            @Override
            public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
            }
        };
        recyclerStudents.setAdapter(studentAdapter);

        cardSelectedStudent.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> finish());

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            filterStudents("");
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString());
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSaveSelectedStudent.setOnClickListener(v -> {
            if (selectedStudent != null) {
                Toast.makeText(this, "Student saved: " + selectedStudent.name, Toast.LENGTH_SHORT).show();
                // Ajoute ici la logique pour enregistrer l'élève dans une autre collection ou pour l'utilisateur
            }
        });

        loadStudents();
    }

    private void loadStudents() {
        db.collection("students").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Student student = doc.toObject(Student.class);
                            studentList.add(student);
                        }
                        tvStudentCount.setText("Students: " + studentList.size());
                        filterStudents(etSearch.getText().toString());
                    }
                });
    }

    private void filterStudents(String query) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase();
        for (Student student : studentList) {
            if (student.name.toLowerCase().contains(lowerQuery) ||
                    student.className.toLowerCase().contains(lowerQuery) ||
                    student.code.toLowerCase().contains(lowerQuery)) {
                filteredList.add(student);
            }
        }
        studentAdapter.notifyDataSetChanged();
    }

    private void showSelectedStudent(Student student) {
        selectedStudent = student;
        cardSelectedStudent.setVisibility(View.VISIBLE);
        tvSelectedStudentName.setText("Name: " + student.name);
        tvSelectedStudentClass.setText("Class: " + student.className);
        tvSelectedStudentCode.setText("Code: " + student.code);
    }

    // Classe Student
    public static class Student {
        public String name = "";
        public String className = "";
        public String code = "";
        public Student() {}
        public Student(String name, String className, String code) {
            this.name = name;
            this.className = className;
            this.code = code;
        }
    }

    // Adapter pour RecyclerView
    public static abstract class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
        private final List<Student> students;
        private final OnStudentClickListener listener;

        public interface OnStudentClickListener { void onStudentClick(Student student); }

        public StudentAdapter(List<Student> students, OnStudentClickListener listener) {
            this.students = students;
            this.listener = listener;
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = View.inflate(parent.getContext(), android.R.layout.simple_list_item_2, null);
            return new StudentViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            Student s = students.get(position);
            holder.text1.setText(s.name + " (" + s.code + ")");
            holder.text2.setText("Class: " + s.className);
            holder.itemView.setOnClickListener(v -> listener.onStudentClick(s));
        }

        @Override
        public int getItemCount() { return students.size(); }

        static class StudentViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            StudentViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}