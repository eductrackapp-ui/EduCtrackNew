package com.equipe7.eductrack.Module;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Activity.ActivityParentDashboard;
import com.equipe7.eductrack.R;

public class ReportsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_report);

        LinearLayout btnExams = findViewById(R.id.btnExams);
        LinearLayout btnHomework = findViewById(R.id.btnHomework);
        LinearLayout btnHome = findViewById(R.id.btnHome);
        LinearLayout btnReport = findViewById(R.id.btnReport);

        btnExams.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentExamResultsActivity.class));
            overridePendingTransition(0,0);
        });

        btnHomework.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeworkActivity.class));
            overridePendingTransition(0,0);
        });

        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, ActivityParentDashboard.class));
            overridePendingTransition(0,0);
        });

        btnReport.setOnClickListener(v -> overridePendingTransition(0,0));
    }
}
