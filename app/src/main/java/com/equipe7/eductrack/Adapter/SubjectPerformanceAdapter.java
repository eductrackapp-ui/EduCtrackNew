package com.equipe7.eductrack.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.PerformanceData;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SubjectPerformanceAdapter extends RecyclerView.Adapter<SubjectPerformanceAdapter.ViewHolder> {
    
    private Context context;
    private Map<String, SubjectSummary> subjectSummaries;
    private List<String> subjectNames;
    
    public static class SubjectSummary {
        public String subjectName;
        public double averageGrade;
        public int totalAssignments;
        public String trend; // "up", "down", "stable"
        public String lastGrade;
        public String subjectColor;
        
        public SubjectSummary(String name, double average, int total) {
            this.subjectName = name;
            this.averageGrade = average;
            this.totalAssignments = total;
            this.trend = "stable";
            this.subjectColor = getSubjectColor(name);
        }
        
        private String getSubjectColor(String subject) {
            switch (subject.toLowerCase()) {
                case "mathematics": case "math": return "#FF5722";
                case "english": case "language": return "#2196F3";
                case "science": case "physics": case "chemistry": return "#4CAF50";
                case "history": case "social studies": return "#FF9800";
                case "geography": return "#9C27B0";
                case "art": case "arts": return "#E91E63";
                case "physical education": case "pe": return "#607D8B";
                default: return "#795548";
            }
        }
        
        public String getGradeDisplay() {
            return String.format("%.1f%%", averageGrade);
        }
        
        public String getPerformanceLevel() {
            if (averageGrade >= 85) return "Excellent";
            if (averageGrade >= 70) return "Good";
            if (averageGrade >= 60) return "Fair";
            return "Needs Improvement";
        }
        
        public int getProgressPercentage() {
            return (int) Math.min(100, Math.max(0, averageGrade));
        }
    }
    
    public SubjectPerformanceAdapter(Context context) {
        this.context = context;
        this.subjectSummaries = new HashMap<>();
        this.subjectNames = new java.util.ArrayList<>();
    }
    
    public void updateData(Map<String, Double> subjectGrades, List<PerformanceData> performanceList) {
        subjectSummaries.clear();
        subjectNames.clear();
        
        // Calculate summaries for each subject
        Map<String, java.util.List<Double>> subjectScores = new HashMap<>();
        
        for (PerformanceData perf : performanceList) {
            String subject = perf.getSubject();
            if (!subjectScores.containsKey(subject)) {
                subjectScores.put(subject, new java.util.ArrayList<>());
            }
            subjectScores.get(subject).add(perf.getPercentage());
        }
        
        // Create summaries
        for (Map.Entry<String, java.util.List<Double>> entry : subjectScores.entrySet()) {
            String subject = entry.getKey();
            java.util.List<Double> scores = entry.getValue();
            
            double average = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            SubjectSummary summary = new SubjectSummary(subject, average, scores.size());
            
            // Determine trend (simplified)
            if (scores.size() >= 2) {
                double recent = scores.get(scores.size() - 1);
                double previous = scores.get(scores.size() - 2);
                if (recent > previous + 5) summary.trend = "up";
                else if (recent < previous - 5) summary.trend = "down";
            }
            
            summary.lastGrade = String.format("%.1f%%", scores.get(scores.size() - 1));
            
            subjectSummaries.put(subject, summary);
            subjectNames.add(subject);
        }
        
        // Add subjects from current grades if not in performance data
        if (subjectGrades != null) {
            for (Map.Entry<String, Double> entry : subjectGrades.entrySet()) {
                if (!subjectSummaries.containsKey(entry.getKey())) {
                    SubjectSummary summary = new SubjectSummary(entry.getKey(), entry.getValue(), 1);
                    summary.lastGrade = String.format("%.1f%%", entry.getValue());
                    subjectSummaries.put(entry.getKey(), summary);
                    subjectNames.add(entry.getKey());
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject_performance, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String subjectName = subjectNames.get(position);
        SubjectSummary summary = subjectSummaries.get(subjectName);
        
        if (summary != null) {
            holder.subjectName.setText(summary.subjectName);
            holder.averageGrade.setText(summary.getGradeDisplay());
            holder.performanceLevel.setText(summary.getPerformanceLevel());
            holder.assignmentCount.setText(summary.totalAssignments + " assignments");
            holder.lastGrade.setText("Last: " + summary.lastGrade);
            
            // Set progress bar
            holder.progressBar.setProgress(summary.getProgressPercentage());
            
            // Set colors
            int color = Color.parseColor(summary.subjectColor);
            holder.subjectIcon.setColorFilter(color);
            holder.progressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            
            // Set trend indicator
            switch (summary.trend) {
                case "up":
                    holder.trendIcon.setImageResource(R.drawable.ic_trending_up);
                    holder.trendIcon.setColorFilter(Color.parseColor("#4CAF50"));
                    break;
                case "down":
                    holder.trendIcon.setImageResource(R.drawable.ic_trending_down);
                    holder.trendIcon.setColorFilter(Color.parseColor("#F44336"));
                    break;
                default:
                    holder.trendIcon.setImageResource(R.drawable.ic_trending_flat);
                    holder.trendIcon.setColorFilter(Color.parseColor("#9E9E9E"));
                    break;
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return subjectNames.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subjectName, averageGrade, performanceLevel, assignmentCount, lastGrade;
        ImageView subjectIcon, trendIcon;
        ProgressBar progressBar;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectName = itemView.findViewById(R.id.subjectName);
            averageGrade = itemView.findViewById(R.id.averageGrade);
            performanceLevel = itemView.findViewById(R.id.performanceLevel);
            assignmentCount = itemView.findViewById(R.id.assignmentCount);
            lastGrade = itemView.findViewById(R.id.lastGrade);
            subjectIcon = itemView.findViewById(R.id.subjectIcon);
            trendIcon = itemView.findViewById(R.id.trendIcon);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
