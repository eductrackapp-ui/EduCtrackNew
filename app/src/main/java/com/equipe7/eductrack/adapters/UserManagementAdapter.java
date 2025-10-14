package com.equipe7.eductrack.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.UserModel;
import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.ViewHolder> {

    private List<UserModel> users;
    private Context context;
    private UserActionListener listener;

    public interface UserActionListener {
        void onUserClick(UserModel user);
        void onDeleteUser(UserModel user);
        void onEditUser(UserModel user);
    }

    public UserManagementAdapter(List<UserModel> users, Context context, UserActionListener listener) {
        this.users = users;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_enhanced, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = users.get(position);
        
        // Set user name
        holder.tvUserName.setText(user.getName() != null ? user.getName() : "Unknown");
        
        // Set user initial
        String name = user.getName() != null ? user.getName() : "U";
        holder.tvUserInitial.setText(name.substring(0, 1).toUpperCase());
        
        // Set email
        holder.tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
        
        // Set role
        String role = user.getRole() != null ? user.getRole() : "User";
        holder.tvUserRole.setText(role);
        
        // Set role color
        int roleColor;
        switch (role.toLowerCase()) {
            case "teacher":
                roleColor = Color.parseColor("#667eea");
                break;
            case "parent":
                roleColor = Color.parseColor("#4CAF50");
                break;
            case "student":
                roleColor = Color.parseColor("#FF9800");
                break;
            case "admin":
                roleColor = Color.parseColor("#9C27B0");
                break;
            default:
                roleColor = Color.parseColor("#999999");
                break;
        }
        holder.tvUserRole.setTextColor(roleColor);
        holder.userAvatar.setBackgroundColor(roleColor);
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
        
        // More options menu
        holder.btnMoreOptions.setOnClickListener(v -> {
            showPopupMenu(holder.btnMoreOptions, user);
        });
    }

    private void showPopupMenu(View view, UserModel user) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.menu_user_options, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_view_details) {
                if (listener != null) listener.onUserClick(user);
                return true;
            } else if (id == R.id.action_edit) {
                if (listener != null) listener.onEditUser(user);
                return true;
            } else if (id == R.id.action_delete) {
                if (listener != null) listener.onDeleteUser(user);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserRole, tvUserInitial;
        ImageView btnMoreOptions;
        LinearLayout userAvatar;

        ViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserInitial = itemView.findViewById(R.id.tvUserInitial);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
            userAvatar = itemView.findViewById(R.id.userAvatar);
        }
    }
}
