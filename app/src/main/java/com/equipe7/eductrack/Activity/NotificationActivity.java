package com.equipe7.eductrack.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.equipe7.eductrack.Adapter.NotificationAdapter;
import com.equipe7.eductrack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private ImageView navHome, navNotifications, navProfile, navMessages;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration notificationListener; // 🔔 écouteur temps réel

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Initialisation Firestore et utilisateur
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Top bar
        navHome = findViewById(R.id.navHome);
        navNotifications = findViewById(R.id.navNotifications);
        navProfile = findViewById(R.id.navProfile);
        navMessages = findViewById(R.id.navMessages);

        // Gestion des clics
        navHome.setOnClickListener(v -> startActivity(new Intent(this, ParentHomeActivity.class)));
        navNotifications.setOnClickListener(v -> { /* déjà ici */ });
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        navMessages.setOnClickListener(v -> startActivity(new Intent(this, BulletinsActivity.class)));

        // RecyclerView
        recyclerView = findViewById(R.id.notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();

        // ✅ Adapter avec callback pour marquer comme lu
        adapter = new NotificationAdapter(notificationList, this::markNotificationAsRead);
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
                        listenForNotifications(userRole); // ✅ écoute en temps réel
                    }
                });
    }

    // ✅ Écoute en temps réel avec historique
    private void listenForNotifications(String userRole) {
        notificationListener = db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null || queryDocumentSnapshots == null) {
                        return;
                    }

                    notificationList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String message = doc.getString("message");
                        String role = doc.getString("role");
                        Boolean isRead = doc.getBoolean("isRead");

                        // ✅ On garde toutes les notifications (historique)
                        if ("All".equalsIgnoreCase(role) || role.equalsIgnoreCase(userRole)) {
                            notificationList.add(new NotificationItem(
                                    doc.getId(), // 🔑 garder l’ID du document
                                    title,
                                    message,
                                    isRead != null && isRead
                            ));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListener != null) {
            notificationListener.remove(); // ✅ éviter les fuites mémoire
        }
    }

    // Modèle notification
    public static class NotificationItem {
        public String id;      // 🔑 ID Firestore
        public String title;
        public String message;
        public boolean isRead;

        public NotificationItem(String id, String title, String message, boolean isRead) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.isRead = isRead;
        }
    }

    // ✅ Fonction pour marquer comme lu (sans supprimer)
    private void markNotificationAsRead(NotificationItem item) {
        db.collection("notifications").document(item.id)
                .update("isRead", true)
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
