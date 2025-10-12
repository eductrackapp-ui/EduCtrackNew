package com.equipe7.eductrack.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Activity.AdminHomeActivity;
import com.equipe7.eductrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvForgot, tvRegister;
    private ImageView ivBack; // ✅ bouton retour

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialisation des composants UI
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvForgot = findViewById(R.id.tvForgot);
        tvRegister = findViewById(R.id.tvRegister);
        ivBack = findViewById(R.id.ivBack); // ✅ récupère l’ImageView

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bouton retour
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminLoginActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // ferme AdminLoginActivity
        });

        // Bouton de connexion
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email requis");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Mot de passe requis");
                return;
            }

            loginAdmin(email, password);
        });

        // Lien mot de passe oublié
        if (tvForgot != null) {
            tvForgot.setOnClickListener(v -> {
                Intent intent = new Intent(AdminLoginActivity.this, AdminForgotPassword.class);
                startActivity(intent);
            });
        }

        // Lien vers la création de compte admin
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Toast.makeText(this, "Redirection vers la page d'inscription", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, CreateAdminAccountActivity.class));
            });
        }
    }

    private void loginAdmin(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Vérifie si l'utilisateur est bien un admin dans Firestore
                        db.collection("admins").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnLogin.setEnabled(true);

                                    if (documentSnapshot.exists()) {
                                        Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(AdminLoginActivity.this, AdminHomeActivity.class));
                                        finish(); // Ferme l'écran de login
                                    } else {
                                        Toast.makeText(this, "Accès refusé : vous n'êtes pas administrateur", Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnLogin.setEnabled(true);
                                    Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        Toast.makeText(this, "Échec d'authentification : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
