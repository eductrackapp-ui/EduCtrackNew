package com.equipe7.eductrack.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.equipe7.eductrack.Activity.NotificationActivity;
import com.equipe7.eductrack.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    // Interface pour gérer le clic sur une notification
    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationActivity.NotificationItem item);
    }

    private final List<NotificationActivity.NotificationItem> items;
    private final OnNotificationClickListener clickListener;

    // Constructeur
    public NotificationAdapter(List<NotificationActivity.NotificationItem> items,
                               OnNotificationClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationActivity.NotificationItem item = items.get(position);

        holder.tvTitle.setText(item.title);
        holder.tvMessage.setText(item.message);

        // ✅ Style visuel : notifications lues en gris, non lues en normal
        float alpha = item.isRead ? 0.6f : 1f;
        holder.tvTitle.setAlpha(alpha);
        holder.tvMessage.setAlpha(alpha);

        // ✅ Clic → callback vers l’Activity
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNotificationClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    // ViewHolder
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}
