package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Auth.UnifiedAuthActivity;
import com.equipe7.eductrack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        videoView = findViewById(R.id.videoView);

        // Charger la vidéo depuis res/raw
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.eductrack);
        videoView.setVideoURI(videoUri);

        // Ne pas boucler la vidéo
        videoView.setOnPreparedListener(mp -> mp.setLooping(false));

        // Quand la vidéo se termine, vérifier l'utilisateur
        videoView.setOnCompletionListener(mp -> checkUserLoggedIn());

        // Démarrer la vidéo
        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback(); // ✅ libère le buffer quand on quitte l'écran
        }
    }

    /**
     * Vérifie si un utilisateur est déjà connecté
     * et le redirige vers l'écran correspondant.
     */
    private void checkUserLoggedIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Aucun utilisateur connecté → aller au login
            goToLogin();
            return;
        }

        // ✅ L'utilisateur est déjà connecté → on vérifie son rôle
        FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        if (role != null) {
                            if ("admin".equalsIgnoreCase(role)) {
                                goToActivity(AdminHomeActivity.class);
                            } else if ("parent".equalsIgnoreCase(role)) {
                                goToActivity(ParentHomeActivity.class);
                            } else if ("teacher".equalsIgnoreCase(role)) {
                                goToActivity(TeacherHomeActivity.class);
                            } else if ("student".equalsIgnoreCase(role)) {
                                // For now, redirect students to parent portal
                                goToActivity(ParentHomeActivity.class);
                            } else {
                                // Si le rôle est inconnu → retour login
                                goToLogin();
                            }
                        } else {
                            // Role is null - let the login activity handle profile creation
                            goToLogin();
                        }
                    } else {
                        // Document doesn't exist - let the login activity handle it
                        // Don't immediately sign out, let UnifiedAuthActivity create the profile
                        android.util.Log.i("SplashActivity", "User document not found for existing Firebase user, redirecting to login");
                        goToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("SplashActivity", "Failed to get user document", e);
                    goToLogin();
                });
    }

    private void goToLogin() {
        startActivity(new Intent(this, UnifiedAuthActivity.class));
        finish();
    }

    private void goToActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        // ✅ Efface la pile d'activités pour éviter de revenir au Splash/Login
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
