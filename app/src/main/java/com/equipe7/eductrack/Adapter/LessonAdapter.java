package com.equipe7.eductrack.Adapter;

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
import com.equipe7.eductrack.Utils.Lesson;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private final List<Lesson> lessonList;
    private final Context context;
    private OnLessonActionListener actionListener;

    public interface OnLessonActionListener {
        void onLessonClick(Lesson lesson);
        void onEditLesson(Lesson lesson);
        void onDeleteLesson(Lesson lesson);
        void onViewDetails(Lesson lesson);
    }

    public LessonAdapter(List<Lesson> lessonList, Context context) {
        this.lessonList = lessonList;
        this.context = context;
    }

    public void setOnLessonActionListener(OnLessonActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);

        // Set lesson information
        holder.tvTitle.setText(lesson.getDisplayTitle());
        holder.tvClassLevel.setText(lesson.getClassLevel());
        holder.tvSubject.setText(lesson.getSubject());
        
        // Set teacher name if available
        if (!lesson.getTeacherName().isEmpty()) {
            holder.tvTeacher.setText("Teacher: " + lesson.getTeacherName());
            holder.tvTeacher.setVisibility(View.VISIBLE);
        } else {
            holder.tvTeacher.setVisibility(View.GONE);
        }

        // Set duration
        holder.tvDuration.setText(lesson.getDurationText());

        // Set topics count
        holder.tvTopicsCount.setText(lesson.getTopicsCount() + " topics");

        // Set status
        holder.tvStatus.setText(lesson.getStatusText());
        holder.tvStatus.setTextColor(Color.parseColor(lesson.getStatusColor()));

        // Set description if available
        if (!lesson.getDescription().isEmpty()) {
            holder.tvDescription.setText(lesson.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Set subject icon based on subject
        setSubjectIcon(holder.ivSubjectIcon, lesson.getSubject());

        // Handle clicks
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onLessonClick(lesson);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditLesson(lesson);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteLesson(lesson);
            }
        });

        // Set card background based on status
        setCardBackground(holder.cardView, lesson.getStatus());
    }

    private void setSubjectIcon(ImageView imageView, String subject) {
        int iconRes;
        switch (subject.toLowerCase()) {
            case "mathematics":
            case "math":
                iconRes = R.drawable.ic_math;
                break;
            case "french":
            case "english":
                iconRes = R.drawable.ic_language;
                break;
            case "set":
            case "science":
                iconRes = R.drawable.ic_science;
                break;
            case "social studies":
            case "history":
                iconRes = R.drawable.ic_history;
                break;
            case "kinyarwanda":
                iconRes = R.drawable.ic_language;
                break;
            default:
                iconRes = R.drawable.ic_courses;
                break;
        }
        imageView.setImageResource(iconRes);
    }

    private void setCardBackground(CardView cardView, String status) {
        int backgroundColor;
        switch (status.toLowerCase()) {
            case "active":
                backgroundColor = Color.parseColor("#E8F5E8");
                break;
            case "completed":
                backgroundColor = Color.parseColor("#E3F2FD");
                break;
            case "draft":
            default:
                backgroundColor = Color.parseColor("#FFF3E0");
                break;
        }
        cardView.setCardBackgroundColor(backgroundColor);
    }

    @Override
    public int getItemCount() {
        return lessonList != null ? lessonList.size() : 0;
    }

    public void updateLesson(int position, Lesson lesson) {
        if (position >= 0 && position < lessonList.size()) {
            lessonList.set(position, lesson);
            notifyItemChanged(position);
        }
    }

    public void removeLesson(int position) {
        if (position >= 0 && position < lessonList.size()) {
            lessonList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addLesson(Lesson lesson) {
        lessonList.add(lesson);
        notifyItemInserted(lessonList.size() - 1);
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvClassLevel, tvSubject, tvTeacher, tvDuration, tvTopicsCount, tvStatus, tvDescription;
        ImageView ivSubjectIcon, btnEdit, btnDelete;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvClassLevel = itemView.findViewById(R.id.tvClassLevel);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvTopicsCount = itemView.findViewById(R.id.tvTopicsCount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivSubjectIcon = itemView.findViewById(R.id.ivSubjectIcon);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
