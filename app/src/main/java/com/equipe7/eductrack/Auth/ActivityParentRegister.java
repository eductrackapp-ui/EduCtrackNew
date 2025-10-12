package com.equipe7.eductrack.Auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Firebase.FirebaseManager;
import com.equipe7.eductrack.R;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityParentRegister extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmailPhone, etPassword, etConfirmPassword;
    private Spinner spinnerRelation, spinnerSchool;
    private CheckBox cbShowPassword, cbTerms;
    private Button btnRegister;

    private FirebaseManager firebaseManager;
    private boolean termsViewed = false; // Pour vérifier si TermsOfUseActivity a été consulté

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_register);

        firebaseManager = FirebaseManager.getInstance();

        // Bind views
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmailPhone = findViewById(R.id.etEmailPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerRelation = findViewById(R.id.spinnerRelation);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        cbShowPassword = findViewById(R.id.cbShowPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);

        // Spinner Adapters
        ArrayAdapter<CharSequence> relationAdapter = ArrayAdapter.createFromResource(
                this, R.array.parent_relations, android.R.layout.simple_spinner_item
        );
        relationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelation.setAdapter(relationAdapter);

        ArrayAdapter<CharSequence> schoolAdapter = ArrayAdapter.createFromResource(
                this, R.array.school_names, android.R.layout.simple_spinner_item
        );
        schoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSchool.setAdapter(schoolAdapter);

        // Show/hide password
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        // TermsOfUseActivity obligatoire avant d’activer REGISTER
        cbTerms.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityParentRegister.this, TermsOfUseActivity.class);
            startActivityForResult(intent, 1001); // Request code pour suivi
        });

        // Le bouton REGISTER est désactivé par défaut
        btnRegister.setEnabled(false);
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> btnRegister.setEnabled(isChecked && termsViewed));

        // REGISTER button
        btnRegister.setOnClickListener(v -> registerParent());
    }

    // Vérification si TermsOfUseActivity a été consulté
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                termsViewed = true; // L'utilisateur a consulté les termes
                btnRegister.setEnabled(cbTerms.isChecked() && termsViewed);
            } else {
                termsViewed = false;
                btnRegister.setEnabled(false);
                cbTerms.setChecked(false);
                showToast("Vous devez consulter les Terms of Use pour continuer");
            }
        }
    }

    private void registerParent() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String emailPhone = etEmailPhone.getText().toString().trim();
        String relation = spinnerRelation.getSelectedItem() != null ? spinnerRelation.getSelectedItem().toString() : "";
        String school = spinnerSchool.getSelectedItem() != null ? spinnerSchool.getSelectedItem().toString() : "";
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Additional validation for terms
        if (!cbTerms.isChecked() || !termsViewed) { 
            showToast("You must accept and review the Terms of Use to continue"); 
            return; 
        }
        
        if (!password.equals(confirmPassword)) { 
            showToast("Passwords do not match"); 
            return; 
        }

        // Prepare user data
        Map<String, Object> parentData = new HashMap<>();
        parentData.put("firstName", firstName);
        parentData.put("lastName", lastName);
        parentData.put("relation", relation);
        parentData.put("school", school);

        // Use AuthenticationManager for registration
        AuthenticationManager authManager = AuthenticationManager.getInstance(this);
        
        // Disable button to prevent multiple submissions
        btnRegister.setEnabled(false);
        
        authManager.registerUser(
            AuthenticationManager.UserType.PARENT, 
            parentData, 
            emailPhone, 
            password, 
            new AuthenticationManager.AuthCallback() {
                @Override
                public void onSuccess(String message, String userId) {
                    showToast("Parent registered successfully!");
                    
                    // Navigate to student registration
                    Intent intent = new Intent(ActivityParentRegister.this, ActivityStudentRegister.class);
                    intent.putExtra("parentId", userId);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    showToast(error);
                    btnRegister.setEnabled(true); // Re-enable button
                }

                @Override
                public void onProgress(String message) {
                    // You could show a progress dialog here
                    showToast(message);
                }
            }
        );
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
