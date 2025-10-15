package com.equipe7.eductrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.Exercise;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TeacherExerciseAdapter extends RecyclerView.Adapter<TeacherExerciseAdapter.ExerciseViewHolder> {

    private Context context;
    private List<Exercise> exercises;
    private OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    public TeacherExerciseAdapter(Context context, List<Exercise> exercises, OnExerciseClickListener listener) {
        this.context = context;
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);

        // Set exercise information
        holder.exerciseTitle.setText(exercise.getTitle());
        holder.exerciseSubject.setText(exercise.getSubject());
        holder.exerciseType.setText(exercise.getType().toUpperCase());
        holder.exerciseClass.setText(exercise.getClassName());
        holder.maxPoints.setText(String.valueOf((int) exercise.getMaxPoints()) + " pts");

        // Set created date
        if (exercise.getCreatedDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.createdDate.setText(sdf.format(exercise.getCreatedDate()));
        } else {
            holder.createdDate.setText("No date");
        }

        // Set status and color
        String status = exercise.getStatus();
        if (status == null) status = "draft";
        
        holder.exerciseStatus.setText(status.toUpperCase());
        switch (status.toLowerCase()) {
            case "published":
                holder.exerciseStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.statusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "graded":
                holder.exerciseStatus.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                holder.statusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                break;
            default:
                holder.exerciseStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                holder.statusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                break;
        }

        // Set exercise type icon
        switch (exercise.getType().toLowerCase()) {
            case "quiz":
                holder.exerciseIcon.setImageResource(R.drawable.ic_quiz);
                break;
            case "exam":
                holder.exerciseIcon.setImageResource(R.drawable.ic_exam);
                break;
            case "assignment":
                holder.exerciseIcon.setImageResource(R.drawable.ic_assignment);
                break;
            default:
                holder.exerciseIcon.setImageResource(R.drawable.ic_exercise);
                break;
        }

        // Set difficulty
        holder.difficulty.setText(exercise.getDifficulty());

        // Click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExerciseClick(exercise);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView exerciseIcon, statusIndicator;
        TextView exerciseTitle, exerciseSubject, exerciseType, exerciseClass;
        TextView maxPoints, createdDate, exerciseStatus, difficulty;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.exerciseCard);
            exerciseIcon = itemView.findViewById(R.id.exerciseIcon);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            exerciseTitle = itemView.findViewById(R.id.exerciseTitle);
            exerciseSubject = itemView.findViewById(R.id.exerciseSubject);
            exerciseType = itemView.findViewById(R.id.exerciseType);
            exerciseClass = itemView.findViewById(R.id.exerciseClass);
            maxPoints = itemView.findViewById(R.id.maxPoints);
            createdDate = itemView.findViewById(R.id.createdDate);
            exerciseStatus = itemView.findViewById(R.id.exerciseStatus);
            difficulty = itemView.findViewById(R.id.difficulty);
        }
    }
}
