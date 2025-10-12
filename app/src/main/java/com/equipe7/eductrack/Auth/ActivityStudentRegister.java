package com.equipe7.eductrack.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Activity.ParentHomeActivity;
import com.equipe7.eductrack.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityStudentRegister extends AppCompatActivity {

    private EditText etFirstName, etLastName;
    private Spinner spinnerRelation, spinnerSchool;
    private CheckBox cbTerms;
    private TextView tvTerms;
    private Button btnRegister;

    private String parentId;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<Intent> termsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    boolean accepted = result.getData() != null && result.getData().getBooleanExtra("accepted", false);
                    cbTerms.setChecked(accepted);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_student_account);

        db = FirebaseFirestore.getInstance();
        parentId = getIntent().getStringExtra("parentId");

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        spinnerRelation = findViewById(R.id.spinnerRelation);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        cbTerms = findViewById(R.id.cbTerms);
        tvTerms = findViewById(R.id.tvTerms);
        btnRegister = findViewById(R.id.btnRegister);

        // Setup relation spinner if it exists
        if (spinnerRelation != null) {
            ArrayAdapter<CharSequence> relationAdapter = ArrayAdapter.createFromResource(
                    this, R.array.parent_relations, android.R.layout.simple_spinner_item
            );
            relationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRelation.setAdapter(relationAdapter);
        }
        
        // Setup school spinner if it exists
        if (spinnerSchool != null) {
            ArrayAdapter<CharSequence> schoolAdapter = ArrayAdapter.createFromResource(
                    this, R.array.school_names, android.R.layout.simple_spinner_item
            );
            schoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSchool.setAdapter(schoolAdapter);
        }

        tvTerms.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityStudentRegister.this, TermsOfUseActivity.class);
            termsLauncher.launch(intent);
        });

        btnRegister.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String relation = spinnerRelation.getSelectedItem() != null ? spinnerRelation.getSelectedItem().toString() : "";
        String school = spinnerSchool.getSelectedItem() != null ? spinnerSchool.getSelectedItem().toString() : "";

        // Validation
        if (TextUtils.isEmpty(firstName)) { showToast("Enter student's first name"); return; }
        if (TextUtils.isEmpty(lastName)) { showToast("Enter student's last name"); return; }
        if (TextUtils.isEmpty(relation) || relation.equals("Select relationship")) { showToast("Select relation"); return; }
        if (TextUtils.isEmpty(school) || school.equals("Select school")) { showToast("Select school"); return; }
        if (!cbTerms.isChecked()) { showToast("You must accept the Conditions of Use"); return; }

        // Prepare student data
        Map<String, Object> studentData = new HashMap<>();
        studentData.put("firstName", firstName);
        studentData.put("lastName", lastName);
        studentData.put("relation", relation);
        studentData.put("school", school);
        studentData.put("parentId", parentId);
        studentData.put("classLevel", "P1"); // Default class level, can be updated later

        // Disable button to prevent multiple submissions
        btnRegister.setEnabled(false);

        // Since students don't have email/password, we'll create them directly in Firestore
        // without Firebase Auth (they login through parent accounts)
        String studentCode = generateStudentCode();
        studentData.put("studentCode", studentCode);
        studentData.put("role", "student");
        studentData.put("isActive", true);
        studentData.put("createdAt", System.currentTimeMillis());

        db.collection("students")
                .add(studentData)
                .addOnSuccessListener((DocumentReference documentReference) -> {
                    showToast("Student registered with code: " + studentCode);
                    
                    // Navigate to parent home
                    Intent intent = new Intent(ActivityStudentRegister.this, ParentHomeActivity.class);
                    intent.putExtra("parentId", parentId);
                    intent.putExtra("studentId", documentReference.getId());
                    intent.putExtra("studentCode", studentCode);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Registration failed: " + e.getMessage());
                    btnRegister.setEnabled(true); // Re-enable button
                });
    }

    private String generateStudentCode() {
        int code = 100000000 + (int)(Math.random() * 900000000);
        return String.valueOf(code);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
