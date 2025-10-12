package com.equipe7.eductrack.Auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Firebase.FirebaseManager;
import com.equipe7.eductrack.R;

import java.util.HashMap;
import java.util.Map;

public class TermsOfUseActivity extends AppCompatActivity {

    private TextView tvTerms;
    private Button btnAccept;

    private FirebaseManager firebaseManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_use);

        // Initialisation des vues
        tvTerms = findViewById(R.id.tv_terms);
        btnAccept = findViewById(R.id.btn_accept);

        firebaseManager = FirebaseManager.getInstance();

        // Texte formaté des conditions
        tvTerms.setText(Html.fromHtml(getFormattedTerms()));

        // Accepter → enregistre directement et retourne le résultat
        btnAccept.setOnClickListener(view -> saveAcceptanceToFirebase(true));
    }

    private String getFormattedTerms() {
        return "<font color='red'><b>📘 Terms of Use (Conditions d’utilisation)</b></font><br><br>" +
                "EduTrack – User Terms & Conditions<br>" +
                "Last updated: July 2025<br><br>" +

                "<font color='blue'><b>1. Acceptance of Terms</b></font><br>" +
                "By accessing or using EduTrack, you agree to comply...<br><br>" +

                "<font color='blue'><b>2. User Roles and Access</b></font><br>" +
                "EduTrack supports three types of users...<br><br>" +

                "<font color='blue'><b>3. Account Responsibility</b></font><br>" +
                "Users must provide accurate info...<br><br>" +

                "<font color='blue'><b>4. Acceptable Use</b></font><br>" +
                "You agree not to impersonate...<br><br>" +

                "<font color='blue'><b>5. Modifications</b></font><br>" +
                "EduTrack reserves the right to update terms...<br><br>" +

                "<font color='blue'><b>6. Limitation of Liability</b></font><br>" +
                "EduTrack is provided “as is”…";
    }

    private void saveAcceptanceToFirebase(boolean accepted) {
        if (firebaseManager.getCurrentUser() == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseManager.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("acceptedTerms", accepted);

        firebaseManager.updateUserData(userId, data)
                .addOnSuccessListener(unused -> {
                    // Retourner résultat à l’Activity précédente
                    Intent result = new Intent();
                    result.putExtra("accepted", accepted);
                    setResult(RESULT_OK, result);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
                );
    }
}
