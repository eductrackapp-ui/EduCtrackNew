package com.equipe7.eductrack.Firebase;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class EduTrackApplication extends Application {

    private static FirebaseAuth mAuth;
    private static FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialiser Firebase
        FirebaseApp.initializeApp(this);

        // Auth
        mAuth = FirebaseAuth.getInstance();

        // Firestore
        db = FirebaseFirestore.getInstance();

        // 🔧 Configurer Firestore (persistance locale activée)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // permet de travailler offline
                .build();
        db.setFirestoreSettings(settings);
    }

    // --- Getters globaux ---
    public static FirebaseAuth getFirebaseAuth() {
        return mAuth;
    }

    public static FirebaseFirestore getFirestore() {
        return db;
    }
}
