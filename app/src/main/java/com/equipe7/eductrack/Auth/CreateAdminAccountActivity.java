// CreateAdminAccountActivity.java
package com.equipe7.eductrack.Auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Activity.AdminHomeActivity;
import com.equipe7.eductrack.Firebase.FirebaseManager;
import com.equipe7.eductrack.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class CreateAdminAccountActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword,
            etInstitutionName, etCompanyName, etSdmcCode;
    private Button btnCreateAccount;

    private FirebaseManager firebaseManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_admin_account);

        firebaseManager = FirebaseManager.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etInstitutionName = findViewById(R.id.etInstitutionName);
        etCompanyName = findViewById(R.id.etCompanyName);
        etSdmcCode = findViewById(R.id.etSdmcCode);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(v -> checkAdminLimitThenCreate());
    }

    private void checkAdminLimitThenCreate() {
        firebaseManager.getFirestore()
                .collection("admins")
                .get()
                .addOnSuccessListener((QuerySnapshot snapshot) -> {
                    if (snapshot.size() >= 4) {
                        toast("❌ Limit reached (4 admins max) — cannot create a new admin.");
                    } else {
                        createAdminAccount();
                    }
                })
                .addOnFailureListener(e -> toast("Firestore error: " + e.getMessage()));
    }

    private void createAdminAccount() {
        String fullName = safeText(etFullName);
        String email = safeText(etEmail);
        String password = safeText(etPassword);
        String confirmPassword = safeText(etConfirmPassword);
        String institutionName = safeText(etInstitutionName);
        String companyName = safeText(etCompanyName);
        String sdmcCode = safeText(etSdmcCode);

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(institutionName)
                || TextUtils.isEmpty(companyName) || TextUtils.isEmpty(sdmcCode)) {
            toast("⚠️ All fields are required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password too short (min 6 characters)");
            return;
        }
        if (!password.equals(confirmPassword)) {
            toast("⚠️ Passwords do not match");
            return;
        }

        firebaseManager.getAuth()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        toast("Auth error: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        return;
                    }

                    FirebaseUser firebaseUser = firebaseManager.getCurrentUser();
                    if (firebaseUser == null) {
                        toast("User not available after creation");
                        return;
                    }

                    Map<String, Object> adminData = new HashMap<>();
                    adminData.put("fullName", fullName);
                    adminData.put("email", email);
                    adminData.put("institutionName", institutionName);
                    adminData.put("companyName", companyName);
                    adminData.put("sdmcCode", sdmcCode);
                    adminData.put("role", "admin");
                    adminData.put("acceptedTerms", true);
                    adminData.put("createdAt", System.currentTimeMillis());

                    firebaseManager.getFirestore()
                            .collection("admins")
                            .document(firebaseUser.getUid())
                            .set(adminData)
                            .addOnSuccessListener(unused -> {
                                toast("✅ Admin account created!");
                                startActivity(new Intent(this, AdminHomeActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    toast("Error saving admin: " + e.getMessage()));
                });
    }

    private String safeText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
