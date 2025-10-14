package com.equipe7.eductrack.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.adapters.UserManagementAdapter;
import com.equipe7.eductrack.models.UserModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminUserManagementActivityEnhanced extends AppCompatActivity {

    private ImageView btnBack;
    private TextInputEditText etSearch;
    private Chip chipAll, chipTeachers, chipParents, chipStudents;
    private TextView tvUserCount;
    private RecyclerView rvUsers;
    
    private FirebaseFirestore db;
    private UserManagementAdapter adapter;
    private List<UserModel> allUsers = new ArrayList<>();
    private List<UserModel> filteredUsers = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management_enhanced);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup listeners
        setupListeners();
        
        // Load users
        loadUsers();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        chipAll = findViewById(R.id.chipAll);
        chipTeachers = findViewById(R.id.chipTeachers);
        chipParents = findViewById(R.id.chipParents);
        chipStudents = findViewById(R.id.chipStudents);
        tvUserCount = findViewById(R.id.tvUserCount);
        rvUsers = findViewById(R.id.rvUsers);
    }

    private void setupRecyclerView() {
        adapter = new UserManagementAdapter(filteredUsers, this, new UserManagementAdapter.UserActionListener() {
            @Override
            public void onUserClick(UserModel user) {
                showUserDetails(user);
            }

            @Override
            public void onDeleteUser(UserModel user) {
                confirmDeleteUser(user);
            }

            @Override
            public void onEditUser(UserModel user) {
                showEditUserDialog(user);
            }
        });
        
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());
        
        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Filter chips
        chipAll.setOnClickListener(v -> {
            currentFilter = "all";
            filterUsers(etSearch.getText().toString());
        });
        
        chipTeachers.setOnClickListener(v -> {
            currentFilter = "teacher";
            filterUsers(etSearch.getText().toString());
        });
        
        chipParents.setOnClickListener(v -> {
            currentFilter = "parent";
            filterUsers(etSearch.getText().toString());
        });
        
        chipStudents.setOnClickListener(v -> {
            currentFilter = "student";
            filterUsers(etSearch.getText().toString());
        });
    }

    private void loadUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                allUsers.clear();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    UserModel user = new UserModel();
                    user.setName(document.getString("name"));
                    user.setEmail(document.getString("email"));
                    user.setRole(document.getString("role"));
                    user.setSubject(document.getString("subject"));
                    user.setAssignedClass(document.getString("assignedClass"));
                    user.setCode(document.getString("code"));
                    
                    // Store document ID for deletion
                    user.setEmail(document.getString("email")); // Use email as ID
                    
                    allUsers.add(user);
                }
                
                filterUsers("");
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load users: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        
        for (UserModel user : allUsers) {
            // Apply role filter
            boolean matchesRole = currentFilter.equals("all") || 
                (user.getRole() != null && user.getRole().equalsIgnoreCase(currentFilter));
            
            // Apply search query
            boolean matchesQuery = query.isEmpty() || 
                (user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase())) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(query.toLowerCase()));
            
            if (matchesRole && matchesQuery) {
                filteredUsers.add(user);
            }
        }
        
        adapter.notifyDataSetChanged();
        tvUserCount.setText("Total: " + filteredUsers.size() + " users");
    }

    private void showUserDetails(UserModel user) {
        String details = "Name: " + (user.getName() != null ? user.getName() : "N/A") + "\n" +
                        "Email: " + (user.getEmail() != null ? user.getEmail() : "N/A") + "\n" +
                        "Role: " + (user.getRole() != null ? user.getRole() : "N/A") + "\n" +
                        "Subject: " + (user.getSubject() != null ? user.getSubject() : "N/A") + "\n" +
                        "Class: " + (user.getAssignedClass() != null ? user.getAssignedClass() : "N/A") + "\n" +
                        "Code: " + (user.getCode() != null ? user.getCode() : "N/A");
        
        new AlertDialog.Builder(this)
            .setTitle("User Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .setNeutralButton("Delete", (dialog, which) -> confirmDeleteUser(user))
            .show();
    }

    private void confirmDeleteUser(UserModel user) {
        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete " + user.getName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteUser(UserModel user) {
        if (user.getEmail() == null) {
            Toast.makeText(this, "Cannot delete user: No email", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Find and delete user by email
        db.collection("users")
            .whereEqualTo("email", user.getEmail())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    document.getReference().delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                            loadUsers(); // Reload list
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to delete: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to find user: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void showEditUserDialog(UserModel user) {
        Toast.makeText(this, "Edit functionality - Coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement edit user dialog
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }
}
