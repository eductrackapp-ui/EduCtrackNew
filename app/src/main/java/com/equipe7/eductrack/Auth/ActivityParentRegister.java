package com.equipe7.eductrack.Auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Firebase.FirebaseManager;
import com.equipe7.eductrack.R;

import java.util.HashMap;
import java.util.Map;

public class ActivityParentRegister extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmailPhone, etPassword, etConfirmPassword;
    private Spinner spinnerRelation, spinnerSchool;
    private CheckBox cbShowPassword, cbTerms;
    private Button btnRegister;

    private FirebaseManager firebaseManager;
    private boolean termsAccepted = false;

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

        // Spinners
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

        // Ouvre TermsOfUseActivity quand on clique sur la case
        cbTerms.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityParentRegister.this, TermsOfUseActivity.class);
            startActivityForResult(intent, 1001);
        });

        // Bouton Register
        btnRegister.setOnClickListener(v -> {
            if (cbTerms.isChecked() && termsAccepted) {
                registerParent();
            } else {
                showToast("Veuillez accepter les Conditions d'utilisation avant de continuer.");
            }
        });
    }

    // Quand on revient des Termes d'utilisation
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                termsAccepted = true;
                cbTerms.setChecked(true);
                showToast("Conditions acceptées ✅ Création du compte en cours...");
                registerParent(); // Crée automatiquement le compte
            } else {
                termsAccepted = false;
                cbTerms.setChecked(false);
                showToast("Vous devez accepter les Conditions d'utilisation pour continuer.");
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

        // Validation
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(emailPhone) || TextUtils.isEmpty(password)) {
            showToast("Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Les mots de passe ne correspondent pas.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailPhone).matches()) {
            showToast("Veuillez entrer une adresse e-mail valide.");
            return;
        }

        // Préparation des données
        Map<String, Object> parentData = new HashMap<>();
        parentData.put("firstName", firstName);
        parentData.put("lastName", lastName);
        parentData.put("relation", relation);
        parentData.put("school", school);
        parentData.put("email", emailPhone);
        parentData.put("role", "parent");
        parentData.put("timestamp", System.currentTimeMillis());

        AuthenticationManager authManager = AuthenticationManager.getInstance(this);
        btnRegister.setEnabled(false);

        authManager.registerUser(
                AuthenticationManager.UserType.PARENT,
                parentData,
                emailPhone,
                password,
                new AuthenticationManager.AuthCallback() {
                    @Override
                    public void onSuccess(String message, String userId) {
                        showToast("Compte parent créé avec succès !");
                        Intent intent = new Intent(ActivityParentRegister.this, ActivityStudentRegister.class);
                        intent.putExtra("parentId", userId);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        showToast("Erreur : " + error);
                        btnRegister.setEnabled(true);
                    }

                    @Override
                    public void onProgress(String message) {
                        showToast(message);
                    }
                }
        );
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
