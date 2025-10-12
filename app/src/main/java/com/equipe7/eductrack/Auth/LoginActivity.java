package com.equipe7.eductrack.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Activity.ParentHomeActivity;
import com.equipe7.eductrack.Activity.TeacherHomeActivity;
import com.equipe7.eductrack.Activity.RoleSelectionActivity;
import com.equipe7.eductrack.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgot, tvRegister, tvAdminLogin;
    private ProgressBar progressBar;

    private boolean isLoading = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgot = findViewById(R.id.tvForgot);
        tvRegister = findViewById(R.id.tvRegister);
        tvAdminLogin = findViewById(R.id.tvAdminLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            if (!isLoading) signInWithPassword();
        });

        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RoleSelectionActivity.class))
        );

        tvAdminLogin.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, AdminLoginActivity.class))
        );
    }

    private void signInWithPassword() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        isLoading = true;
        progressBar.setVisibility(ProgressBar.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    isLoading = false;
                    progressBar.setVisibility(ProgressBar.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user.getUid());
                        }
                    } else {
                        Toast.makeText(this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // --------------------------
    // Vérifie le rôle dans "users" puis "teachers"
    // --------------------------
    private void checkUserRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            handleUserRole(document);
                        } else {
                            // Si pas trouvé dans "users", on cherche dans "teachers"
                            db.collection("teachers").document(uid).get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            DocumentSnapshot docTeacher = task2.getResult();
                                            if (docTeacher != null && docTeacher.exists()) {
                                                handleUserRole(docTeacher);
                                            } else {
                                                Toast.makeText(this,
                                                        "No account found in database. Please register first.",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(this,
                                                    "Error fetching teacher data",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --------------------------
    // Redirection selon le rôle
    // --------------------------
    private void handleUserRole(DocumentSnapshot document) {
        String role = document.getString("role");

        if ("parent".equalsIgnoreCase(role)) {
            startActivity(new Intent(LoginActivity.this, ParentHomeActivity.class));
            finish();
        } else if ("teacher".equalsIgnoreCase(role)) {
            startActivity(new Intent(LoginActivity.this, TeacherHomeActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Role not recognized", Toast.LENGTH_SHORT).show();
        }
    }
}
