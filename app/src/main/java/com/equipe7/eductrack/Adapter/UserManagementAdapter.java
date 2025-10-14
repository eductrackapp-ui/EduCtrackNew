package com.equipe7.eductrack.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.UserManagement;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.ViewHolder> {

    private Context context;
    private List<UserManagement> users;
    private UserActionListener listener;

    public interface UserActionListener {
        void onSuspendUser(UserManagement user);
        void onReactivateUser(UserManagement user);
        void onViewDetails(UserManagement user);
        void onDeleteUser(UserManagement user);
    }

    public UserManagementAdapter(Context context, List<UserManagement> users, UserActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserManagement user = users.get(position);

        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.userRole.setText(user.getRoleDisplayName());
        holder.userStatus.setText(user.getStatus().toUpperCase());
        holder.lastLogin.setText("Last login: " + user.getFormattedLastLogin());
        holder.createdDate.setText("Joined: " + user.getFormattedCreatedDate());

        // Set role color
        holder.userRole.setTextColor(Color.parseColor(user.getRoleColor()));
        
        // Set status color
        holder.userStatus.setTextColor(Color.parseColor(user.getStatusColor()));

        // Set online indicator
        if (user.isOnline()) {
            holder.onlineIndicator.setVisibility(View.VISIBLE);
            holder.onlineIndicator.setColorFilter(Color.parseColor("#4CAF50"));
        } else {
            holder.onlineIndicator.setVisibility(View.GONE);
        }

        // Performance info (for teachers and students)
        if ("teacher".equals(user.getRole()) || "student".equals(user.getRole())) {
            holder.performanceInfo.setVisibility(View.VISIBLE);
            holder.performanceScore.setText("Performance: " + user.getPerformanceDisplay());
            holder.performanceScore.setTextColor(Color.parseColor(user.getPerformanceColor()));
            
            if ("teacher".equals(user.getRole())) {
                holder.assignmentInfo.setText("Assignments created: " + user.getTotalAssignments());
            } else {
                holder.assignmentInfo.setText("Completion rate: " + user.getCompletionRate() + "%");
            }
        } else {
            holder.performanceInfo.setVisibility(View.GONE);
        }

        // Additional info based on role
        if ("teacher".equals(user.getRole())) {
            holder.additionalInfo.setVisibility(View.VISIBLE);
            String info = "Subject: " + (user.getSubject() != null ? user.getSubject() : "Not assigned");
            if (user.getAssignedClass() != null) {
                info += " | Class: " + user.getAssignedClass();
            }
            holder.additionalInfo.setText(info);
        } else if ("parent".equals(user.getRole())) {
            holder.additionalInfo.setVisibility(View.VISIBLE);
            int childrenCount = user.getChildrenIds() != null ? user.getChildrenIds().size() : 0;
            holder.additionalInfo.setText("Children: " + childrenCount);
        } else if ("student".equals(user.getRole())) {
            holder.additionalInfo.setVisibility(View.VISIBLE);
            String info = "Class: " + (user.getAssignedClass() != null ? user.getAssignedClass() : "Not assigned");
            holder.additionalInfo.setText(info);
        } else {
            holder.additionalInfo.setVisibility(View.GONE);
        }

        // Action buttons
        setupActionButtons(holder, user);

        // Click listeners
        holder.itemView.setOnClickListener(v -> listener.onViewDetails(user));
    }

    private void setupActionButtons(ViewHolder holder, UserManagement user) {
        // Suspend/Reactivate button
        if ("active".equals(user.getStatus())) {
            holder.btnSuspend.setText("Suspend");
            holder.btnSuspend.setBackgroundColor(Color.parseColor("#F44336"));
            holder.btnSuspend.setOnClickListener(v -> listener.onSuspendUser(user));
        } else if ("suspended".equals(user.getStatus())) {
            holder.btnSuspend.setText("Reactivate");
            holder.btnSuspend.setBackgroundColor(Color.parseColor("#4CAF50"));
            holder.btnSuspend.setOnClickListener(v -> listener.onReactivateUser(user));
        } else {
            holder.btnSuspend.setText("Activate");
            holder.btnSuspend.setBackgroundColor(Color.parseColor("#2196F3"));
            holder.btnSuspend.setOnClickListener(v -> listener.onReactivateUser(user));
        }

        // Details button
        holder.btnDetails.setOnClickListener(v -> listener.onViewDetails(user));

        // Delete button (only for non-admin users)
        if (!"admin".equals(user.getRole())) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteUser(user));
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userRole, userStatus, lastLogin, createdDate;
        TextView performanceScore, assignmentInfo, additionalInfo;
        ImageView onlineIndicator;
        View performanceInfo;
        MaterialButton btnSuspend, btnDetails, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userRole = itemView.findViewById(R.id.userRole);
            userStatus = itemView.findViewById(R.id.userStatus);
            lastLogin = itemView.findViewById(R.id.lastLogin);
            createdDate = itemView.findViewById(R.id.createdDate);
            performanceScore = itemView.findViewById(R.id.performanceScore);
            assignmentInfo = itemView.findViewById(R.id.assignmentInfo);
            additionalInfo = itemView.findViewById(R.id.additionalInfo);
            onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
            performanceInfo = itemView.findViewById(R.id.performanceInfo);
            btnSuspend = itemView.findViewById(R.id.btnSuspend);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
