package com.equipe7.eductrack.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvRole;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize views
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);

        db = FirebaseFirestore.getInstance();

        // Get the username passed from AdminHomeActivity
        String userName = getIntent().getStringExtra("userName");

        if (userName != null && !userName.isEmpty()) {
            loadUserProfile(userName);
        } else {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserProfile(String name) {
        db.collection("users")
                .whereEqualTo("name", name) // search by NAME
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            UserModel user = doc.toObject(UserModel.class);

                            if (user != null) {
                                tvName.setText("Name: " + user.getName());
                                tvEmail.setText("Email: " + user.getEmail());
                                tvRole.setText("Role: " + user.getRole());
                            }
                        } else {
                            Toast.makeText(UserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }
}
