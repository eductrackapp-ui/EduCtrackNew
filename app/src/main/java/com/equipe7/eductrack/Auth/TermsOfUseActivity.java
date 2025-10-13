package com.equipe7.eductrack.Auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Firebase.FirebaseManager;
import com.equipe7.eductrack.R;

public class TermsOfUseActivity extends AppCompatActivity {

    private TextView tvTerms;
    private Button btnAccept, btnDecline;
    private FirebaseManager firebaseManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_use);

        tvTerms = findViewById(R.id.tv_terms);
        btnAccept = findViewById(R.id.btn_accept);
        btnDecline = findViewById(R.id.btn_decline);

        firebaseManager = FirebaseManager.getInstance();

        tvTerms.setText(Html.fromHtml(getFormattedTerms()));

        btnAccept.setOnClickListener(view -> saveAcceptanceToFirebase(true));

        btnDecline.setOnClickListener(view -> {
            Intent result = new Intent();
            result.putExtra("accepted", false);
            setResult(RESULT_CANCELED, result);
            finish();
        });
    }

    private String getFormattedTerms() {
        return "<h2><font color='red'>📘 Terms of Use (Conditions d’utilisation)</font></h2><br>" +
                "<b>EduTrack – User Terms & Conditions</b><br>" +
                "Last updated: July 2025<br><br>" +
                "<font color='blue'><b>1. Acceptance of Terms</b></font><br>" +
                "By accessing or using EduTrack, you agree to comply with these terms.<br><br>" +
                "<font color='blue'><b>2. User Roles and Access</b></font><br>" +
                "EduTrack supports three types of users: Students, Teachers, and Admins.<br><br>" +
                "<font color='blue'><b>3. Account Responsibility</b></font><br>" +
                "Users must provide accurate information and keep their credentials secure.<br><br>" +
                "<font color='blue'><b>4. Acceptable Use</b></font><br>" +
                "You agree not to misuse the platform, impersonate others, or attempt unauthorized access.<br><br>" +
                "<font color='blue'><b>5. Modifications</b></font><br>" +
                "EduTrack reserves the right to update these terms at any time.<br><br>" +
                "<font color='blue'><b>6. Limitation of Liability</b></font><br>" +
                "EduTrack is provided “as is” without warranties. Use at your own risk.<br><br>" +
                "<i>By clicking Accept, you acknowledge that you have read and agreed to these terms.</i>";
    }

    private void saveAcceptanceToFirebase(boolean accepted) {
        if (!firebaseManager.isUserLoggedIn()) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseManager.getCurrentUser().getUid();

        // Ici on utilise updateUserField pour mettre à jour uniquement le champ "acceptedTerms"
        firebaseManager.updateUserField(userId, "acceptedTerms", accepted)
                .addOnSuccessListener(unused -> {
                    Intent result = new Intent();
                    result.putExtra("accepted", accepted);
                    setResult(RESULT_OK, result);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("TermsOfUseActivity", "Erreur Firebase", e);
                    Toast.makeText(this, "Erreur lors de l'enregistrement: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
