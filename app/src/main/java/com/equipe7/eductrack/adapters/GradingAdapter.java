package com.equipe7.eductrack.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.GradeEntry;
import java.util.List;

public class GradingAdapter extends RecyclerView.Adapter<GradingAdapter.GradingViewHolder> {

    private Context context;
    private List<GradeEntry> gradesList;
    private OnGradeChangeListener listener;

    public interface OnGradeChangeListener {
        void onGradeChanged(GradeEntry gradeEntry, double newScore, String feedback);
    }

    public GradingAdapter(Context context, List<GradeEntry> gradesList, OnGradeChangeListener listener) {
        this.context = context;
        this.gradesList = gradesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GradingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_grading, parent, false);
        return new GradingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradingViewHolder holder, int position) {
        GradeEntry gradeEntry = gradesList.get(position);
        
        // Set student information
        holder.studentName.setText(gradeEntry.getStudentName());
        holder.maxScore.setText("/ " + String.format("%.0f", gradeEntry.getMaxScore()));
        
        // Set current score if graded
        if (gradeEntry.getStatus().equals("graded")) {
            holder.scoreInput.setText(String.format("%.1f", gradeEntry.getScore()));
            holder.feedbackInput.setText(gradeEntry.getFeedback());
            holder.percentageDisplay.setText(gradeEntry.getPercentageDisplay());
            holder.gradeStatus.setText("GRADED");
            holder.gradeStatus.setTextColor(Color.parseColor("#4CAF50"));
            setGradeIndicator(holder.gradeIndicator, gradeEntry.getPercentage());
        } else {
            holder.scoreInput.setText("");
            holder.feedbackInput.setText("");
            holder.percentageDisplay.setText("--");
            holder.gradeStatus.setText("PENDING");
            holder.gradeStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.gradeIndicator.setBackgroundColor(Color.parseColor("#E0E0E0"));
        }
        
        // Set up text watchers for real-time updates
        setupScoreWatcher(holder, gradeEntry);
        setupFeedbackWatcher(holder, gradeEntry);
    }

    private void setupScoreWatcher(GradingViewHolder holder, GradeEntry gradeEntry) {
        holder.scoreInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String scoreText = s.toString().trim();
                if (!scoreText.isEmpty()) {
                    try {
                        double score = Double.parseDouble(scoreText);
                        double maxScore = gradeEntry.getMaxScore();
                        
                        if (score >= 0 && score <= maxScore) {
                            double percentage = (score / maxScore) * 100;
                            holder.percentageDisplay.setText(String.format("%.1f%%", percentage));
                            setGradeIndicator(holder.gradeIndicator, percentage);
                            
                            // Update status
                            holder.gradeStatus.setText("GRADED");
                            holder.gradeStatus.setTextColor(Color.parseColor("#4CAF50"));
                            
                            // Notify listener
                            if (listener != null) {
                                String feedback = holder.feedbackInput.getText().toString().trim();
                                listener.onGradeChanged(gradeEntry, score, feedback);
                            }
                        } else {
                            holder.percentageDisplay.setText("Invalid");
                            holder.gradeIndicator.setBackgroundColor(Color.parseColor("#F44336"));
                        }
                    } catch (NumberFormatException e) {
                        holder.percentageDisplay.setText("--");
                        holder.gradeIndicator.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    }
                } else {
                    holder.percentageDisplay.setText("--");
                    holder.gradeStatus.setText("PENDING");
                    holder.gradeStatus.setTextColor(Color.parseColor("#FF9800"));
                    holder.gradeIndicator.setBackgroundColor(Color.parseColor("#E0E0E0"));
                }
            }
        });
    }

    private void setupFeedbackWatcher(GradingViewHolder holder, GradeEntry gradeEntry) {
        holder.feedbackInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String feedback = s.toString().trim();
                String scoreText = holder.scoreInput.getText().toString().trim();
                
                if (!scoreText.isEmpty() && listener != null) {
                    try {
                        double score = Double.parseDouble(scoreText);
                        listener.onGradeChanged(gradeEntry, score, feedback);
                    } catch (NumberFormatException e) {
                        // Invalid score, don't update
                    }
                }
            }
        });
    }

    private void setGradeIndicator(View indicator, double percentage) {
        int color;
        if (percentage >= 85) {
            color = Color.parseColor("#4CAF50"); // Green - Excellent
        } else if (percentage >= 70) {
            color = Color.parseColor("#2196F3"); // Blue - Good
        } else if (percentage >= 60) {
            color = Color.parseColor("#FF9800"); // Orange - Fair
        } else {
            color = Color.parseColor("#F44336"); // Red - Needs Improvement
        }
        indicator.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return gradesList.size();
    }

    public void updateGrades(List<GradeEntry> newGrades) {
        this.gradesList = newGrades;
        notifyDataSetChanged();
    }

    static class GradingViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView studentAvatar;
        TextView studentName, maxScore, percentageDisplay, gradeStatus;
        EditText scoreInput, feedbackInput;
        View gradeIndicator;

        public GradingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.gradingCard);
            studentAvatar = itemView.findViewById(R.id.studentAvatar);
            studentName = itemView.findViewById(R.id.studentName);
            maxScore = itemView.findViewById(R.id.maxScore);
            percentageDisplay = itemView.findViewById(R.id.percentageDisplay);
            gradeStatus = itemView.findViewById(R.id.gradeStatus);
            scoreInput = itemView.findViewById(R.id.scoreInput);
            feedbackInput = itemView.findViewById(R.id.feedbackInput);
            gradeIndicator = itemView.findViewById(R.id.gradeIndicator);
        }
    }
}
