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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendResetEmail, btnCreateAccount;
    private ProgressBar progressBar;
    private ImageView ivBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // ✅ Firebase Authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // UI references
        TextInputLayout tilEmail = findViewById(R.id.tilEmail);
        etEmail = tilEmail.getEditText();
        btnSendResetEmail = findViewById(R.id.btnSendResetEmail);
        btnCreateAccount = findViewById(R.id.btnBackToLogin); // ⚡ Vérifie ton XML
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);

        db = FirebaseManager.getInstance().getFirestore();
        mAuth = FirebaseAuth.getInstance();

        // Send reset link
        btnSendResetEmail.setOnClickListener(v -> sendResetLink());

        // Back arrow to LoginActivity
        ivBack.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, UnifiedAuthActivity.class));
            finish();
        });

        // Create new account
        btnCreateAccount.setText("Create a new account");
        btnCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, RoleSelectionActivity.class));
            finish();
        });
    }

    private void sendResetLink() {
        if (etEmail == null || TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = etEmail.getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);

        // Vérifie d'abord dans "users"
        checkUserCollection(email);
    }

    private void checkUserCollection(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        QueryDocumentSnapshot userDoc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                        String role = userDoc.getString("role");

                        if (role != null && role.equalsIgnoreCase("parent")) {
                            sendFirebaseReset(email);
                        } else {
                            // Si ce n’est pas un parent → vérifier dans teachers
                            checkTeacherCollection(email);
                        }
                    } else {
                        // Pas trouvé dans users → vérifier dans teachers
                        checkTeacherCollection(email);
                    }
                })
                .addOnFailureListener(e -> {
                    hideProgress();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkTeacherCollection(String email) {
        db.collection("teachers")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        QueryDocumentSnapshot teacherDoc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                        String role = teacherDoc.getString("role");

                        if (role != null && role.equalsIgnoreCase("teacher")) {
                            sendFirebaseReset(email);
                        } else if (role != null && role.equalsIgnoreCase("admin")) {
                            hideProgress();
                            Toast.makeText(this, "This email belongs to an admin. Use the admin reset page.", Toast.LENGTH_SHORT).show();
                        } else {
                            hideProgress();
                            Toast.makeText(this, "This email is not linked to a teacher account", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hideProgress();
                        Toast.makeText(this, "No account found with this email", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    hideProgress();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendFirebaseReset(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    hideProgress();
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Failed to send reset link", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }
}
