package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;

public class TeacherDashboard extends AppCompatActivity {

    private RecyclerView recyclerTeachers;
    private TeacherAdapter adapter;
    private List<Teacher> teacherList;
    private Button btnCreateTeacher;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_teacher_dashboard);

        // Initialiser Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Initialiser les vues
        recyclerTeachers = findViewById(R.id.recyclerTeachers);
        btnCreateTeacher = findViewById(R.id.btnCreateTeacher);

        // Configurer RecyclerView
        recyclerTeachers.setLayoutManager(new LinearLayoutManager(this));
        teacherList = new ArrayList<>();
        adapter = new TeacherAdapter(teacherList);
        recyclerTeachers.setAdapter(adapter);

        // Charger les enseignants depuis Firestore
        loadTeachers();

        // Bouton pour ouvrir la page d'inscription enseignant
        btnCreateTeacher.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboard.this, ActivityTeacherRegister.class);
            startActivity(intent);
        });
    }

    private void loadTeachers() {
        CollectionReference teachersRef = db.collection("teachers");

        teachersRef.get().addOnCompleteListener((Task<QuerySnapshot> task) -> {
            if (task.isSuccessful()) {
                teacherList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Récupérer les bons champs
                    String teacherCode = document.getString("teacherCode");
                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");
                    String classLevel = document.getString("classLevel");

                    if (teacherCode != null && firstName != null && lastName != null) {
                        String fullName = firstName + " " + lastName;
                        teacherList.add(new Teacher(teacherCode, fullName, classLevel));
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e("Firestore", "Erreur lors du chargement", task.getException());
                Toast.makeText(TeacherDashboard.this,
                        "Erreur de chargement des enseignants", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
