package com.equipe7.eductrack.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Auth.LoginActivity;
import com.equipe7.eductrack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private TextView tvName, tvRole;
    private EditText etUsername, etEmail, etPhone;
    private Button btnUpdateInfo, btnLogout, btnDeleteAccount;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profileImage);
        tvName = findViewById(R.id.tvName);
        tvRole = findViewById(R.id.tvRole);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnUpdateInfo = findViewById(R.id.btnUpdateInfo);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount); // ⚡ assure-toi que ce bouton existe dans ton XML

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        loadUserProfile();

        profileImage.setOnClickListener(v -> changeProfilePicture());
        btnUpdateInfo.setOnClickListener(v -> updateUserInfo());
        btnLogout.setOnClickListener(v -> logoutUser());
        btnDeleteAccount.setOnClickListener(v -> deleteAccount());
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    String username = documentSnapshot.getString("username");
                    String role = documentSnapshot.getString("role");
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    String imageUrl = documentSnapshot.getString("profileImageUrl");

                    tvName.setText(fullName != null ? fullName : "Nom inconnu");
                    etUsername.setText(username != null ? username : "");
                    tvRole.setText(role != null ? role : "Utilisateur");
                    etEmail.setText(email != null ? email : "");
                    etPhone.setText(phone != null ? phone : "");

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .into(profileImage);
                    }
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(ProfileActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void changeProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri);
            Toast.makeText(this, "Image sélectionnée, prête à être uploadée", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserInfo() {
        String newUsername = etUsername.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (newUsername.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid)
                    .update("username", newUsername, "email", newEmail, "phone", newPhone)
                    .addOnSuccessListener(aVoid -> {
                        currentUser.updateEmail(newEmail)
                                .addOnSuccessListener(aVoid1 ->
                                        Toast.makeText(ProfileActivity.this, "Infos mises à jour", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(ProfileActivity.this, "Erreur maj email: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ProfileActivity.this, "Erreur Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Déconnecté avec succès", Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void deleteAccount() {
        if (currentUser != null) {
            currentUser.delete().addOnSuccessListener(aVoid -> {
                Toast.makeText(ProfileActivity.this, "Compte supprimé", Toast.LENGTH_SHORT).show();
                goToLogin();
            }).addOnFailureListener(e ->
                    Toast.makeText(ProfileActivity.this, "Erreur suppression: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ⚡ Empêche retour arrière après logout/suppression
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
