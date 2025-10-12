package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.equipe7.eductrack.Adapter.ParentPagerAdapter;
import com.equipe7.eductrack.Module.StudentExamResultsActivity;
import com.equipe7.eductrack.Module.HomeworkActivity;
import com.equipe7.eductrack.Module.ReportsActivity;
import com.equipe7.eductrack.Module.StudentExamResultsActivity;
import com.equipe7.eductrack.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ActivityParentDashboard extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_activity_parent_examens);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        ParentPagerAdapter adapter = new ParentPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("Performance");
            else tab.setText("Progress");
        }).attach();

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
            startActivity(new Intent(this, ParentHomeActivity.class));
            overridePendingTransition(0,0);
        });

        btnReport.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
            overridePendingTransition(0,0);
        });
    }
}
