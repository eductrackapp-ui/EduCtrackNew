package com.equipe7.eductrack.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.equipe7.eductrack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminNotification extends AppCompatActivity {

    private ImageView navHome, navNotifications, navProfile, navMessages;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_notification);

        // Init Firestore and user
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Top bar
        navHome = findViewById(R.id.navHome);
        navNotifications = findViewById(R.id.navNotifications);
        navProfile = findViewById(R.id.navProfile);
        navMessages = findViewById(R.id.navMessages);

        // Navigation clicks
        navHome.setOnClickListener(v -> startActivity(new Intent(this, AdminHomeActivity.class)));
        navNotifications.setOnClickListener(v -> { /* already here */ });
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, AdminProfileActivity.class)));
        navMessages.setOnClickListener(v -> startActivity(new Intent(this, BulletinsActivity.class)));

        // RecyclerView
        recyclerView = findViewById(R.id.notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        loadNotificationsFromFirestore();
    }

    private void loadNotificationsFromFirestore() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userRole = documentSnapshot.getString("role");
                        fetchNotifications(userRole);
                    }
                });
    }

    private void fetchNotifications(String userRole) {
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String message = doc.getString("message");
                        String role = doc.getString("role");

                        if ("All".equalsIgnoreCase(role) || role.equalsIgnoreCase(userRole)) {
                            notificationList.add(new NotificationItem(title, message));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // Notification model
    public static class NotificationItem {
        public String title;
        public String message;

        public NotificationItem(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }

    // RecyclerView Adapter
    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        private final List<NotificationItem> items;

        public NotificationAdapter(List<NotificationItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = getLayoutInflater().inflate(R.layout.notification_item, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            NotificationItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvMessage.setText(item.message);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class NotificationViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView tvTitle, tvMessage;

            public NotificationViewHolder(android.view.View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvMessage = itemView.findViewById(R.id.tvMessage);
            }
        }
    }
}
