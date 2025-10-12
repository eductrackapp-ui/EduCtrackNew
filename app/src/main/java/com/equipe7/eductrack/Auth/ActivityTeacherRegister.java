package com.equipe7.eductrack.Auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class ActivityTeacherRegister extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private Spinner spinnerGender, spinnerSchool, spinnerClass, spinnerPosition;
    private CheckBox cbConfidentiality;
    private Button btnRegister;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseFunctions functions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_teacher);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        functions = FirebaseFunctions.getInstance();

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
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerTeacher());
    }

    private void registerTeacher() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        String gender = spinnerGender.getSelectedItem() != null ? spinnerGender.getSelectedItem().toString() : "";
        String school = spinnerSchool.getSelectedItem() != null ? spinnerSchool.getSelectedItem().toString() : "";
        String classLevel = spinnerClass.getSelectedItem() != null ? spinnerClass.getSelectedItem().toString() : "";
        String position = spinnerPosition.getSelectedItem() != null ? spinnerPosition.getSelectedItem().toString() : "";

        if (TextUtils.isEmpty(firstName)) { toast("Enter first name"); return; }
        if (TextUtils.isEmpty(lastName)) { toast("Enter last name"); return; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Enter a valid email"); return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            toast("Password must be at least 6 characters"); return;
        }
        if (!password.equals(confirmPassword)) {
            toast("Passwords do not match"); return;
        }
        if (TextUtils.isEmpty(gender)) { toast("Select gender"); return; }
        if (TextUtils.isEmpty(school)) { toast("Select school"); return; }
        if (TextUtils.isEmpty(classLevel)) { toast("Select class"); return; }
        if (TextUtils.isEmpty(position)) { toast("Select position"); return; }
        if (!cbConfidentiality.isChecked()) { toast("You must accept the Confidentiality Policy"); return; }

        String teacherCode = generateTeacherCode(school, classLevel);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", uid);
                    data.put("firstName", firstName);
                    data.put("lastName", lastName);
                    data.put("email", email);
                    data.put("gender", gender);
                    data.put("school", school);
                    data.put("classLevel", classLevel);
                    data.put("position", position);
                    data.put("teacherCode", teacherCode);

                    db.collection("teachers")
                            .document(uid)
                            .set(data)
                            .addOnSuccessListener(doc -> {
                                toast("Teacher registered. Code: " + teacherCode);
                                callRoleAssignmentFunction(uid);
                                finish();
                            })
                            .addOnFailureListener(e -> toast("Firestore error: " + e.getMessage()));
                })
                .addOnFailureListener(e -> toast("Auth error: " + e.getMessage()));
    }

    private void callRoleAssignmentFunction(String uid) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", uid);

        functions.getHttpsCallable("assignTeacherRole")
                .call(payload)
                .addOnSuccessListener(result -> toast("Teacher role assigned"))
                .addOnFailureListener(e -> toast("Role assignment failed: " + e.getMessage()));
    }

    private String generateTeacherCode(String school, String classLevel) {
        String schoolPrefix = getSchoolPrefix(school);
        String classSuffix = getClassSuffix(classLevel);
        int randomDigits = 1000 + new Random().nextInt(9000);
        return "EDF-" + schoolPrefix + "-" + classSuffix + "-" + randomDigits;
    }

    private String getSchoolPrefix(String school) {
        if (school == null) return "XX";
        String s = school.trim().toLowerCase();
        if (s.contains("kacyiru")) return "KA";
        if (s.contains("gisozi")) return "GI";
        if (s.contains("kimisagara")) return "KI";
        return "XX";
    }

    private String getClassSuffix(String classLevel) {
        if (classLevel == null) return "P0";
        String c = classLevel.trim().toLowerCase();
        if (c.contains("class a") || c.equals("a")) return "P1";
        if (c.contains("class b") || c.equals("b")) return "P2";
        if (c.contains("class c") || c.equals("c")) return "P3";
        if (c.contains("class d") || c.equals("d")) return "P4";
        if (c.contains("class e") || c.equals("e")) return "P5";
        if (c.contains("class f") || c.equals("f")) return "P6";
        return "P0";
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
