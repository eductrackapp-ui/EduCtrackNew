package com.equipe7.eductrack.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.Child;
import java.util.List;

public class TeacherStudentAdapter extends RecyclerView.Adapter<TeacherStudentAdapter.StudentViewHolder> {

    private Context context;
    private List<Child> students;
    private OnStudentClickListener listener;

    public interface OnStudentClickListener {
        void onStudentClick(Child student);
    }

    public TeacherStudentAdapter(Context context, List<Child> students, OnStudentClickListener listener) {
        this.context = context;
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Child student = students.get(position);
        
        // Set student information
        holder.studentName.setText(student.getName());
        holder.studentCode.setText("ID: " + student.getStudentCode());
        holder.studentClass.setText(student.getClassName());
        
        // Set performance data
        if (student.getOverallAverage() > 0) {
            holder.studentGrade.setText(String.format("%.1f%%", student.getOverallAverage()));
            holder.performanceStatus.setText(getStatusDisplay(student.getStatus()));
            holder.performanceStatus.setTextColor(Color.parseColor(getStatusColor(student.getStatus())));
        } else {
            holder.studentGrade.setText("--");
            holder.performanceStatus.setText("No grades");
            holder.performanceStatus.setTextColor(Color.parseColor("#9E9E9E"));
        }
        
        // Set performance indicator
        setPerformanceIndicator(holder.performanceIndicator, student.getOverallAverage());
        
        // Click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStudentClick(student);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "No Data";
        
        switch (status.toLowerCase()) {
            case "excellent": return "Excellent";
            case "good": return "Good";
            case "needs_improvement": return "Needs Help";
            default: return "No Data";
        }
    }

    private String getStatusColor(String status) {
        if (status == null) return "#9E9E9E";
        
        switch (status.toLowerCase()) {
            case "excellent": return "#4CAF50"; // Green
            case "good": return "#2196F3"; // Blue
            case "needs_improvement": return "#FF9800"; // Orange
            default: return "#9E9E9E"; // Gray
        }
    }

    private void setPerformanceIndicator(View indicator, double average) {
        int color;
        if (average >= 85) {
            color = Color.parseColor("#4CAF50"); // Green
        } else if (average >= 70) {
            color = Color.parseColor("#2196F3"); // Blue
        } else if (average >= 60) {
            color = Color.parseColor("#FF9800"); // Orange
        } else if (average > 0) {
            color = Color.parseColor("#F44336"); // Red
        } else {
            color = Color.parseColor("#E0E0E0"); // Light Gray
        }
        indicator.setBackgroundColor(color);
    }

    public void updateStudents(List<Child> newStudents) {
        this.students = newStudents;
        notifyDataSetChanged();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView studentAvatar;
        TextView studentName, studentCode, studentClass;
        TextView studentGrade, performanceStatus;
        View performanceIndicator;
        ImageView btnGrade;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.studentCard);
            studentAvatar = itemView.findViewById(R.id.studentAvatar);
            studentName = itemView.findViewById(R.id.studentName);
            studentCode = itemView.findViewById(R.id.studentCode);
            studentClass = itemView.findViewById(R.id.studentClass);
            studentGrade = itemView.findViewById(R.id.studentGrade);
            performanceStatus = itemView.findViewById(R.id.performanceStatus);
            performanceIndicator = itemView.findViewById(R.id.performanceIndicator);
            btnGrade = itemView.findViewById(R.id.btnGrade);
        }
    }
}
