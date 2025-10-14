package com.equipe7.eductrack.Activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.UserManagement;
import com.equipe7.eductrack.Firebase.AdminManager;
import com.equipe7.eductrack.Adapter.UserManagementAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminUserManagementActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView recyclerUsers;
    private SearchView searchView;
    private UserManagementAdapter userAdapter;
    private AdminManager adminManager;
    
    private List<UserManagement> allUsers = new ArrayList<>();
    private List<UserManagement> filteredUsers = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        adminManager = new AdminManager();
        
        initializeViews();
        setupTabs();
        setupRecyclerView();
        setupSearch();
        loadAllUsers();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayout);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        searchView = findViewById(R.id.searchView);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All Users"));
        tabLayout.addTab(tabLayout.newTab().setText("Teachers"));
        tabLayout.addTab(tabLayout.newTab().setText("Parents"));
        tabLayout.addTab(tabLayout.newTab().setText("Students"));
        tabLayout.addTab(tabLayout.newTab().setText("Admins"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: filterUsers("all"); break;
                    case 1: filterUsers("teacher"); break;
                    case 2: filterUsers("parent"); break;
                    case 3: filterUsers("student"); break;
                    case 4: filterUsers("admin"); break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        userAdapter = new UserManagementAdapter(this, filteredUsers, new UserManagementAdapter.UserActionListener() {
            @Override
            public void onSuspendUser(UserManagement user) {
                suspendUser(user);
            }

            @Override
            public void onReactivateUser(UserManagement user) {
                reactivateUser(user);
            }

            @Override
            public void onViewDetails(UserManagement user) {
                viewUserDetails(user);
            }

            @Override
            public void onDeleteUser(UserManagement user) {
                deleteUser(user);
            }
        });
        
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(userAdapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    filterUsers(currentFilter);
                } else {
                    searchUsers(newText);
                }
                return true;
            }
        });
    }

    private void loadAllUsers() {
        adminManager.getAllUsers(new AdminManager.AdminCallback<List<UserManagement>>() {
            @Override
            public void onSuccess(List<UserManagement> users) {
                allUsers.clear();
                allUsers.addAll(users);
                filterUsers("all");
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminUserManagementActivity.this, "Failed to load users: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String role) {
        currentFilter = role;
        filteredUsers.clear();
        
        if ("all".equals(role)) {
            filteredUsers.addAll(allUsers);
        } else {
            for (UserManagement user : allUsers) {
                if (role.equals(user.getRole())) {
                    filteredUsers.add(user);
                }
            }
        }
        
        userAdapter.notifyDataSetChanged();
    }

    private void searchUsers(String query) {
        filteredUsers.clear();
        String lowerQuery = query.toLowerCase();
        
        for (UserManagement user : allUsers) {
            if (user.getName().toLowerCase().contains(lowerQuery) ||
                user.getEmail().toLowerCase().contains(lowerQuery) ||
                user.getRole().toLowerCase().contains(lowerQuery)) {
                filteredUsers.add(user);
            }
        }
        
        userAdapter.notifyDataSetChanged();
    }

    private void suspendUser(UserManagement user) {
        adminManager.suspendUser(user.getUserId(), "Suspended by admin", new AdminManager.AdminCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdminUserManagementActivity.this, "User suspended successfully", Toast.LENGTH_SHORT).show();
                user.setStatus("suspended");
                user.setCanLogin(false);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminUserManagementActivity.this, "Failed to suspend user: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reactivateUser(UserManagement user) {
        adminManager.reactivateUser(user.getUserId(), new AdminManager.AdminCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdminUserManagementActivity.this, "User reactivated successfully", Toast.LENGTH_SHORT).show();
                user.setStatus("active");
                user.setCanLogin(true);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminUserManagementActivity.this, "Failed to reactivate user: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void viewUserDetails(UserManagement user) {
        // Placeholder for user details
        Toast.makeText(this, "User Details: " + user.getName() + " (" + user.getRole() + ")", Toast.LENGTH_LONG).show();
    }

    private void deleteUser(UserManagement user) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to permanently delete " + user.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    adminManager.deleteUser(user.getUserId(), new AdminManager.AdminCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(AdminUserManagementActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                            allUsers.remove(user);
                            filteredUsers.remove(user);
                            userAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(AdminUserManagementActivity.this, "Failed to delete user: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
