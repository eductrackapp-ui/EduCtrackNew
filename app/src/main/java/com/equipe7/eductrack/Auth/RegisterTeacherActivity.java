package com.equipe7.eductrack.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Activity.TeacherDashboard;
import com.equipe7.eductrack.Auth.TermsOfUseActivity;
import com.equipe7.eductrack.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterTeacherActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private Spinner spinnerGender, spinnerSchool, spinnerClass, spinnerPosition;
    private CheckBox cbConfidentiality;
    private TextView tvConfidentiality;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_teacher);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerPosition = findViewById(R.id.spinnerPosition);

        cbConfidentiality = findViewById(R.id.cbConfidentiality);
        tvConfidentiality = findViewById(R.id.tvConfidentiality);
        btnRegister = findViewById(R.id.btnRegister);

        // Ouvrir la page Confidentialité
        tvConfidentiality.setOnClickListener(v -> {
            Intent intent = new Intent(this, TermsOfUseActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> registerTeacher());
    }

    private void registerTeacher() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        String gender = spinnerGender.getSelectedItem() != null ? spinnerGender.getSelectedItem().toString() : "";
        String school = spinnerSchool.getSelectedItem() != null ? spinnerSchool.getSelectedItem().toString() : "";
        String classLevel = spinnerClass.getSelectedItem() != null ? spinnerClass.getSelectedItem().toString() : "";
        String position = spinnerPosition.getSelectedItem() != null ? spinnerPosition.getSelectedItem().toString() : "";

        // ✅ validations
        if (TextUtils.isEmpty(firstName)) { showToast("Enter first name"); return; }
        if (TextUtils.isEmpty(lastName)) { showToast("Enter last name"); return; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { showToast("Enter valid email"); return; }
        if (TextUtils.isEmpty(password) || password.length() < 6) { showToast("Password must be at least 6 characters"); return; }
        if (!password.equals(confirmPassword)) { showToast("Passwords do not match"); return; }
        if (TextUtils.isEmpty(gender) || gender.equals("Select gender")) { showToast("Select gender"); return; }
        if (TextUtils.isEmpty(school) || school.equals("Select school")) { showToast("Select school"); return; }
        if (TextUtils.isEmpty(classLevel) || classLevel.equals("Select class")) { showToast("Select class"); return; }
        if (TextUtils.isEmpty(position) || position.equals("Select role")) { showToast("Select position"); return; }
        if (!cbConfidentiality.isChecked()) { showToast("You must accept the Confidentiality Policy"); return; }

        // ✅ création compte Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();

                        Map<String, Object> teacherData = new HashMap<>();
                        teacherData.put("firstName", firstName);
                        teacherData.put("lastName", lastName);
                        teacherData.put("email", email);
                        teacherData.put("gender", gender);
                        teacherData.put("school", school);
                        teacherData.put("classLevel", classLevel);
                        teacherData.put("position", position);
                        teacherData.put("uid", uid);

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("role", "teacher");

                        db.collection("teachers").document(uid).set(teacherData);
                        db.collection("users").document(uid).set(userData);

                        showToast("Teacher account created");
                        startActivity(new Intent(this, TeacherDashboard.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> showToast("Error: " + e.getMessage()));
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
