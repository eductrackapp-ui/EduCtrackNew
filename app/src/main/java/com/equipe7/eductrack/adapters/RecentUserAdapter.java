package com.equipe7.eductrack.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.UserModel;
import java.util.List;

public class RecentUserAdapter extends RecyclerView.Adapter<RecentUserAdapter.ViewHolder> {

    private List<UserModel> users;
    private Context context;

    public RecentUserAdapter(List<UserModel> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_user, parent, false);
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
        
        // Set role
        String role = user.getRole() != null ? user.getRole() : "User";
        holder.tvUserRole.setText(role);
        
        // Set last active (placeholder for now)
        holder.tvLastActive.setText("Active recently");
        
        // Set status indicator color based on online status
        // For now, random - in production you'd check Firebase presence
        if (position % 3 == 0) {
            holder.statusIndicator.setBackgroundColor(Color.parseColor("#4CAF50")); // Online
        } else {
            holder.statusIndicator.setBackgroundColor(Color.parseColor("#9E9E9E")); // Offline
        }
        
        // Set initial background color based on role
        if ("Teacher".equalsIgnoreCase(role)) {
            holder.tvUserInitial.setBackgroundColor(Color.parseColor("#667eea"));
        } else if ("Parent".equalsIgnoreCase(role)) {
            holder.tvUserInitial.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else if ("Student".equalsIgnoreCase(role)) {
            holder.tvUserInitial.setBackgroundColor(Color.parseColor("#FF9800"));
        } else {
            holder.tvUserInitial.setBackgroundColor(Color.parseColor("#9C27B0"));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateData(List<UserModel> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserRole, tvLastActive, tvUserInitial;
        LinearLayout statusIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvLastActive = itemView.findViewById(R.id.tvLastActive);
            tvUserInitial = itemView.findViewById(R.id.tvUserInitial);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
    }
}
