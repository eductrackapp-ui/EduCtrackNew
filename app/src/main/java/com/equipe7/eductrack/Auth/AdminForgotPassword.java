package com.equipe7.eductrack.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Activity.RoleSelectionActivity;
import com.equipe7.eductrack.Firebase.FirebaseManager;
import com.equipe7.eductrack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminForgotPassword extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendResetLink;
    private ProgressBar progressBar;
    private TextView tvCreateAccount;
    private ImageView ivBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // ✅ Firebase Authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_forgot_password);

        // UI elements
        etEmail = findViewById(R.id.etEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetEmail);
        progressBar = findViewById(R.id.progressBar);
        tvCreateAccount = findViewById(R.id.btnBackToLogin);
        ivBack = findViewById(R.id.ivBack);

        db = FirebaseManager.getInstance().getFirestore();
        mAuth = FirebaseAuth.getInstance();

        // Send reset link
        btnSendResetLink.setOnClickListener(v -> sendResetLink());

        // Back arrow to AdminLoginActivity
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminForgotPassword.this, AdminLoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Create account link to RoleSelectionActivity
        tvCreateAccount.setText("Create a new account");
        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(AdminForgotPassword.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void sendResetLink() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Vérifie si l'email existe dans la collection admins
        db.collection("admins")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        QueryDocumentSnapshot adminDoc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                        String role = adminDoc.getString("role");

                        if (role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("admins"))) {
                            // ✅ Utilisation de FirebaseAuth pour envoyer le mail
                            mAuth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(task -> {
                                        progressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            Toast.makeText(AdminForgotPassword.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(AdminForgotPassword.this, "Failed to send reset link", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Access denied: not an admin", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "No admin account found with this email", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
