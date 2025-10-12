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

public class HomeworkActivity extends AppCompatActivity {

    private TextInputEditText etStudentCode;
    private MaterialButton btnLoadHomework;
    private FirebaseFirestore db;
    private TableLayout tableHomework;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_parent); // ✅ utilise la méthode héritée

        db = FirebaseFirestore.getInstance();

        etStudentCode = findViewById(R.id.etStudentCode);
        btnLoadHomework = findViewById(R.id.btnLoadResults); // ✅ vérifie l’ID dans ton XML
        tableHomework = findViewById(R.id.tableWeek1);

        btnLoadHomework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentCode = etStudentCode.getText().toString().trim();
                if (TextUtils.isEmpty(studentCode)) {
                    Toast.makeText(HomeworkActivity.this, "Enter student code", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadHomework(studentCode);
            }
        });
    }

    private void loadHomework(String studentCode) {
        DocumentReference docRef = db.collection("homework").document(studentCode);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(HomeworkActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Map<String, Object> data = snapshot.getData();
                    if (data != null) {
                        populateHomeworkTable(data);
                    }
                } else {
                    Toast.makeText(HomeworkActivity.this, "No homework found for this student", Toast.LENGTH_SHORT).show();
                    tableHomework.removeAllViews();
                }
            }
        });
    }

    private void populateHomeworkTable(Map<String, Object> data) {
        tableHomework.removeAllViews();

        // Header
        TableRow header = new TableRow(this);

        TextView lessonHeader = new TextView(this);
        lessonHeader.setText("Lesson");
        lessonHeader.setPadding(6, 6, 6, 6);
        lessonHeader.setTextColor(getResources().getColor(android.R.color.white));
        lessonHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        header.addView(lessonHeader);

        TextView homeworkHeader = new TextView(this);
        homeworkHeader.setText("Homework Title");
        homeworkHeader.setPadding(6, 6, 6, 6);
        homeworkHeader.setTextColor(getResources().getColor(android.R.color.white));
        homeworkHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        header.addView(homeworkHeader);

        TextView scoreHeader = new TextView(this);
        scoreHeader.setText("Score");
        scoreHeader.setPadding(6, 6, 6, 6);
        scoreHeader.setTextColor(getResources().getColor(android.R.color.white));
        scoreHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        header.addView(scoreHeader);

        tableHomework.addView(header);

        // Dynamically add homework
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            TableRow row = new TableRow(this);

            if (entry.getValue() instanceof Map) {
                Map<?, ?> hw = (Map<?, ?>) entry.getValue();

                TextView lesson = new TextView(this);
                lesson.setText(hw.get("lesson") != null ? hw.get("lesson").toString() : "--");
                lesson.setPadding(6, 6, 6, 6);
                row.addView(lesson);

                TextView title = new TextView(this);
                title.setText(hw.get("title") != null ? hw.get("title").toString() : "--");
                title.setPadding(6, 6, 6, 6);
                row.addView(title);

                TextView score = new TextView(this);
                score.setText(hw.get("score") != null ? hw.get("score").toString() : "--");
                score.setPadding(6, 6, 6, 6);
                row.addView(score);

                tableHomework.addView(row);
            }
        }
    }
}
