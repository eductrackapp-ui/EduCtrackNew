package com.equipe7.eductrack.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Auth.AuthenticationManager;
import com.equipe7.eductrack.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for admin to create teacher accounts
 */
public class AdminCreateTeacherActivity extends AppCompatActivity {
    
    private static final String TAG = "AdminCreateTeacher";
    
    private EditText etFirstName, etLastName, etEmail, etPassword, etPhone;
    private Spinner spinnerGender, spinnerSchool, spinnerClass, spinnerPosition, spinnerSubject;
    private Button btnCreateTeacher, btnBack;
    private ProgressBar progressBar;
    
    private AuthenticationManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_teacher);

        authManager = AuthenticationManager.getInstance(this);
        
        initViews();
        setupSpinners();
        setupClickListeners();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerPosition = findViewById(R.id.spinnerPosition);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        
        btnCreateTeacher = findViewById(R.id.btnCreateTeacher);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        // Gender spinner
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // School spinner
        String[] schools = {"Select School", "Eden Family School - Kacyiru", 
            "Eden Family School - Gisozi", "Eden Family School - Kimisagara"};
        ArrayAdapter<String> schoolAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, schools);
        schoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSchool.setAdapter(schoolAdapter);

        // Class spinner
        String[] classes = {"Select Class", "P1", "P2", "P3", "P4", "P5", "P6"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, classes);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);

        // Position spinner
        String[] positions = {"Select Position", "Head Teacher", "Class Teacher", 
            "Subject Teacher", "Assistant Teacher", "Substitute Teacher"};
        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, positions);
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosition.setAdapter(positionAdapter);

        // Subject spinner
        String[] subjects = {"Select Subject", "Mathematics", "English", "French", 
            "Kinyarwanda", "Social Studies", "Science", "Physical Education", "Arts"};
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);
    }

    private void setupClickListeners() {
        btnCreateTeacher.setOnClickListener(v -> createTeacherAccount());
        btnBack.setOnClickListener(v -> finish());
    }

    private void createTeacherAccount() {
        // Get form data
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        
        String gender = spinnerGender.getSelectedItem().toString();
        String school = spinnerSchool.getSelectedItem().toString();
        String classLevel = spinnerClass.getSelectedItem().toString();
        String position = spinnerPosition.getSelectedItem().toString();
        String subject = spinnerSubject.getSelectedItem().toString();

        // Validate input
        if (!validateInput(firstName, lastName, email, password, phone, 
                          gender, school, classLevel, position, subject)) {
            return;
        }

        // Prepare teacher data
        Map<String, Object> teacherData = new HashMap<>();
        teacherData.put("firstName", firstName);
        teacherData.put("lastName", lastName);
        teacherData.put("phone", phone);
        teacherData.put("gender", gender);
        teacherData.put("school", school);
        teacherData.put("classLevel", classLevel);
        teacherData.put("position", position);
        teacherData.put("subject", subject);
        teacherData.put("createdByAdmin", true);

        // Show progress and disable button
        showProgress(true);
        btnCreateTeacher.setEnabled(false);

        // Create teacher account using AuthenticationManager
        authManager.registerUser(
            AuthenticationManager.UserType.TEACHER,
            teacherData,
            email,
            password,
            new AuthenticationManager.AuthCallback() {
                @Override
                public void onSuccess(String message, String userId) {
                    showProgress(false);
                    Log.d(TAG, "Teacher created successfully: " + userId);
                    
                    Toast.makeText(AdminCreateTeacherActivity.this, 
                        "Teacher account created successfully!\nTeacher ID: " + userId, 
                        Toast.LENGTH_LONG).show();
                    
                    // Clear form
                    clearForm();
                    btnCreateTeacher.setEnabled(true);
                }

                @Override
                public void onFailure(String error) {
                    showProgress(false);
                    btnCreateTeacher.setEnabled(true);
                    
                    Log.e(TAG, "Teacher creation failed: " + error);
                    Toast.makeText(AdminCreateTeacherActivity.this, 
                        "Failed to create teacher: " + error, 
                        Toast.LENGTH_LONG).show();
                }

                @Override
                public void onProgress(String message) {
                    Log.d(TAG, "Progress: " + message);
                    // You could update a progress text here
                }
            }
        );
    }

    private boolean validateInput(String firstName, String lastName, String email, 
                                String password, String phone, String gender, 
                                String school, String classLevel, String position, String subject) {
        
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }

        if (gender.equals("Select Gender")) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (school.equals("Select School")) {
            Toast.makeText(this, "Please select school", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (classLevel.equals("Select Class")) {
            Toast.makeText(this, "Please select class level", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (position.equals("Select Position")) {
            Toast.makeText(this, "Please select position", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (subject.equals("Select Subject")) {
            Toast.makeText(this, "Please select subject", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void clearForm() {
        etFirstName.setText("");
        etLastName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etPhone.setText("");
        
        spinnerGender.setSelection(0);
        spinnerSchool.setSelection(0);
        spinnerClass.setSelection(0);
        spinnerPosition.setSelection(0);
        spinnerSubject.setSelection(0);
    }
}
