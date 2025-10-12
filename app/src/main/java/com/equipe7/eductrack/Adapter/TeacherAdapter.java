package com.equipe7.eductrack.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.equipe7.eductrack.R;
import com.equipe7.eductrack.Utils.Teacher;

import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    private final List<Teacher> teacherList;
    private final Context context;
    private OnTeacherActionListener actionListener;

    public interface OnTeacherActionListener {
        void onViewProfile(Teacher teacher);
        void onEditTeacher(Teacher teacher);
        void onDeleteTeacher(Teacher teacher);
        void onSendMessage(Teacher teacher);
    }

    public TeacherAdapter(List<Teacher> teacherList, Context context) {
        this.teacherList = teacherList;
        this.context = context;
    }

    public void setOnTeacherActionListener(OnTeacherActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        Teacher teacher = teacherList.get(position);

        // Set teacher information
        holder.tvName.setText(teacher.getDisplayName());
        holder.tvCode.setText(teacher.getTeacherCode());
        holder.tvClassSubject.setText(teacher.getClassAndSubject());
        
        // Set email if available
        if (teacher.getEmail() != null && !teacher.getEmail().isEmpty()) {
            holder.tvEmail.setText(teacher.getEmail());
            holder.tvEmail.setVisibility(View.VISIBLE);
        } else {
            holder.tvEmail.setVisibility(View.GONE);
        }

        // Set online status
        holder.tvStatus.setText(teacher.getStatusText());
        if (teacher.isOnline()) {
            holder.statusIndicator.setBackgroundResource(R.drawable.status_online);
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.statusIndicator.setBackgroundResource(R.drawable.status_offline);
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Set profile image (placeholder for now)
        holder.ivTeacherAvatar.setImageResource(R.drawable.teacher);

        // Handle action button click
        holder.btnTeacherActions.setOnClickListener(v -> showPopupMenu(v, teacher));

        // Handle item click for profile view
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onViewProfile(teacher);
            }
        });
    }

    private void showPopupMenu(View view, Teacher teacher) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.teacher_actions_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            if (actionListener == null) return false;
            
            int itemId = item.getItemId();
            if (itemId == R.id.action_view_profile) {
                actionListener.onViewProfile(teacher);
                return true;
            } else if (itemId == R.id.action_edit_teacher) {
                actionListener.onEditTeacher(teacher);
                return true;
            } else if (itemId == R.id.action_send_message) {
                actionListener.onSendMessage(teacher);
                return true;
            } else if (itemId == R.id.action_delete_teacher) {
                actionListener.onDeleteTeacher(teacher);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    @Override
    public int getItemCount() {
        return teacherList != null ? teacherList.size() : 0;
    }

    public void updateTeacher(int position, Teacher teacher) {
        if (position >= 0 && position < teacherList.size()) {
            teacherList.set(position, teacher);
            notifyItemChanged(position);
        }
    }

    public void removeTeacher(int position) {
        if (position >= 0 && position < teacherList.size()) {
            teacherList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class TeacherViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvClassSubject, tvEmail, tvStatus;
        ImageView ivTeacherAvatar, btnTeacherActions;
        View statusIndicator;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvClassSubject = itemView.findViewById(R.id.tvClassSubject);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivTeacherAvatar = itemView.findViewById(R.id.ivTeacherAvatar);
            btnTeacherActions = itemView.findViewById(R.id.btnTeacherActions);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
    }
}
