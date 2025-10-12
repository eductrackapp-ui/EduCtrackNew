package com.equipe7.eductrack.Module;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Map;

public class StudentExercisesActivity extends AppCompatActivity {

    private TextInputEditText etStudentCode;
    private MaterialButton btnLoadExercises;
    private TableLayout tableExercises;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises_parent); // ton XML

        db = FirebaseFirestore.getInstance();

        etStudentCode = findViewById(R.id.etStudentCode);
        btnLoadExercises = findViewById(R.id.btnLoadExercises);
        tableExercises = findViewById(R.id.tableExercises);

        btnLoadExercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentCode = etStudentCode.getText().toString().trim();
                if (TextUtils.isEmpty(studentCode)) {
                    Toast.makeText(StudentExercisesActivity.this, "Enter student code", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadExercises(studentCode);
            }
        });
    }

    private void loadExercises(String studentCode) {
        DocumentReference docRef = db.collection("exercises").document(studentCode);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(StudentExercisesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Map<String, Object> data = snapshot.getData();
                    if (data != null) {
                        populateExercisesTable(data);
                    }
                } else {
                    Toast.makeText(StudentExercisesActivity.this, "No exercises found for this student", Toast.LENGTH_SHORT).show();
                    tableExercises.removeAllViews();
                }
            }
        });
    }

    private void populateExercisesTable(Map<String, Object> data) {
        tableExercises.removeAllViews();

        // Header
        TableRow header = new TableRow(this);

        TextView lessonHeader = new TextView(this);
        lessonHeader.setText("Lesson");
        lessonHeader.setPadding(6,6,6,6);
        lessonHeader.setTextColor(getResources().getColor(android.R.color.white));
        lessonHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        lessonHeader.setTextSize(16);
        lessonHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(lessonHeader);

        TextView exerciseHeader = new TextView(this);
        exerciseHeader.setText("Exercise Title");
        exerciseHeader.setPadding(6,6,6,6);
        exerciseHeader.setTextColor(getResources().getColor(android.R.color.white));
        exerciseHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        exerciseHeader.setTextSize(16);
        exerciseHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(exerciseHeader);

        TextView scoreHeader = new TextView(this);
        scoreHeader.setText("Score");
        scoreHeader.setPadding(6,6,6,6);
        scoreHeader.setTextColor(getResources().getColor(android.R.color.white));
        scoreHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        scoreHeader.setTextSize(16);
        scoreHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(scoreHeader);

        tableExercises.addView(header);

        // Add exercises dynamically
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            TableRow row = new TableRow(this);
            Map<String, Object> exercise = (Map<String, Object>) entry.getValue();

            TextView lesson = new TextView(this);
            lesson.setText(exercise.get("lesson").toString());
            lesson.setPadding(6,6,6,6);
            row.addView(lesson);

            TextView title = new TextView(this);
            title.setText(exercise.get("title").toString());
            title.setPadding(6,6,6,6);
            row.addView(title);

            TextView score = new TextView(this);
            score.setText(exercise.get("score").toString());
            score.setPadding(6,6,6,6);
            row.addView(score);

            tableExercises.addView(row);
        }
    }
}
