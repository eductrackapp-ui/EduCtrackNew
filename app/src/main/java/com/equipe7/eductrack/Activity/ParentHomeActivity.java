package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.R;
import com.equipe7.eductrack.TrackModule.HomeworkActivity;
import com.equipe7.eductrack.TrackModule.StudentExercisesActivity;
import com.equipe7.eductrack.Activity.LessonsActivity;
import com.equipe7.eductrack.TrackModule.ParentsReportsActivity;
import com.equipe7.eductrack.Activity.StudentListActivity;
import com.equipe7.eductrack.TrackModule.StudentExamResultsActivity;
import com.equipe7.eductrack.Activity.NotificationActivity;
import com.equipe7.eductrack.Activity.SearchResultActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ParentHomeActivity extends AppCompatActivity {

    public static class SearchableItem {
        public String type;
        public String name;
        public String code;
        public String className;

        public SearchableItem() {} // Nécessaire pour Firebase

        public SearchableItem(String type, String name, String code, String className) {
            this.type = type;
            this.name = name;
            this.code = code;
            this.className = className;
        }
    }

    private List<SearchableItem> database = new ArrayList<>();
    private DatabaseReference dbRef;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_home_activity);

        tvWelcome = findViewById(R.id.tvWelcome);

        // Affiche le nom de l'utilisateur connecté
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        tvWelcome.setText("Bienvenue, " + name);
                    } else {
                        tvWelcome.setText("Bienvenue !");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    tvWelcome.setText("Bienvenue !");
                }
            });
        } else {
            tvWelcome.setText("Bienvenue !");
        }

        // Barre de navigation du bas
        findViewById(R.id.btnExams).setOnClickListener(v -> startActivity(new Intent(this, StudentExamResultsActivity.class)));
        findViewById(R.id.btnHomework).setOnClickListener(v -> startActivity(new Intent(this, HomeworkActivity.class)));
        findViewById(R.id.btnReport).setOnClickListener(v -> startActivity(new Intent(this, ParentsReportsActivity.class)));
        findViewById(R.id.btnExercise).setOnClickListener(v -> startActivity(new Intent(this, StudentExercisesActivity.class)));
        findViewById(R.id.btnHome).setOnClickListener(v -> startActivity(new Intent(this, ParentHomeActivity.class)));

        // Boutons "ALL COURSES" et "Students"
        findViewById(R.id.btnAllCourses).setOnClickListener(v -> startActivity(new Intent(this, LessonsActivity.class)));
        findViewById(R.id.btnStudents).setOnClickListener(v -> startActivity(new Intent(this, StudentListActivity.class)));

        // Bouton notifications
        ImageView navNotifications = findViewById(R.id.navNotifications);
        navNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));

        // Barre de recherche
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                searchInFirebase(query);
                return true;
            }
            return false;
        });

        // Charger les données de recherche depuis Firebase
        loadDatabaseFromFirebase();
    }

    // Recherche universelle dans Firebase
    private void searchInFirebase(String query) {
        String lowerQuery = query.toLowerCase();
        List<SearchableItem> results = new ArrayList<>();
        for (SearchableItem item : database) {
            if ((item.name != null && item.name.toLowerCase().contains(lowerQuery)) ||
                    (item.code != null && item.code.toLowerCase().contains(lowerQuery)) ||
                    (item.className != null && item.className.toLowerCase().contains(lowerQuery))) {
                results.add(item);
            }
        }
        // Passe les résultats à l'activité de résultats
        Intent intent = new Intent(this, SearchResultActivity.class);
        intent.putExtra("search_query", query);
        SearchResultActivity.results = results; // exemple simple
        startActivity(intent);
    }

    // Charger les données de recherche depuis Firebase
    private void loadDatabaseFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("searchable_items");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                database.clear();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    SearchableItem item = itemSnap.getValue(SearchableItem.class);
                    if (item != null) database.add(item);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}