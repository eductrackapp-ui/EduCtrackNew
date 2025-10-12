package com.equipe7.eductrack.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.equipe7.eductrack.Activity.AdminHomeActivity;
import com.equipe7.eductrack.Activity.AddScoresActivity;
import com.equipe7.eductrack.R;

import java.util.HashMap;
import java.util.Map;

public class LessonsActivity extends AppCompatActivity {

    private TextView tvP1, tvP2, tvP3, tvP4, tvP5, tvP6;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);

        // --- Toolbar avec flèche retour ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lessons by Class");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Active la flèche
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back); // ton icône flèche
        }

        // Initialisation des TextViews
        tvP1 = findViewById(R.id.tvP1);
        tvP2 = findViewById(R.id.tvP2);
        tvP3 = findViewById(R.id.tvP3);
        tvP4 = findViewById(R.id.tvP4);
        tvP5 = findViewById(R.id.tvP5);
        tvP6 = findViewById(R.id.tvP6);

        // Charger les leçons par classe
        Map<String, String> lessons = getLessons();

        tvP1.setText("P1 Lessons:\n" + lessons.get("P1"));
        tvP2.setText("P2 Lessons:\n" + lessons.get("P2"));
        tvP3.setText("P3 Lessons:\n" + lessons.get("P3"));
        tvP4.setText("P4 Lessons:\n" + lessons.get("P4"));
        tvP5.setText("P5 Lessons:\n" + lessons.get("P5"));
        tvP6.setText("P6 Lessons:\n" + lessons.get("P6"));
    }

    private Map<String, String> getLessons() {
        Map<String, String> lessons = new HashMap<>();

        lessons.put("P1", "Mathematics, French, English, SET, Social Studies, Kinyarwanda");
        lessons.put("P2", "Mathematics, French, English, SET, Social Studies, Kinyarwanda");
        lessons.put("P3", "Mathematics, French, English, SET, Social Studies, Kinyarwanda");
        lessons.put("P4", "Mathematics, French, English, SET, Social Studies, Kinyarwanda");
        lessons.put("P5", "Mathematics, French, English, SET, Social Studies, Kinyarwanda");
        lessons.put("P6", "Mathematics, French, English, SET, Social Studies, Kinyarwanda");

        return lessons;
    }

    // Gestion du clic sur la flèche retour
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Retourne sur HomeActivity
            Intent intent = new Intent(LessonsActivity.this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
