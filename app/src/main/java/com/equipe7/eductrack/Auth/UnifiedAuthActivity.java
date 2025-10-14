package com.equipe7.eductrack.Auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.equipe7.eductrack.Activity.AdminHomeActivityNew;
import com.equipe7.eductrack.Activity.ParentHomeActivity;
import com.equipe7.eductrack.Activity.TeacherHomeActivity;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.UserModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UnifiedAuthActivity extends AppCompatActivity {

    // UI Components
    private CardView loginCard, signupCard;
    private TextView tvSwitchToSignup, tvSwitchToLogin, tvWelcomeTitle, tvWelcomeSubtitle, btnForgotPassword;
    private Button btnLogin, btnSignup, btnAdminLogin;
    private Button btnTeacherSignup, btnParentSignup, btnAdminSignup;
    private LinearLayout roleSelectionButtons, roleButtons;
    private ProgressBar progressBar;

    // Login Form
    private TextInputLayout tilLoginEmail, tilLoginPassword;
    private TextInputEditText etLoginEmail, etLoginPassword;

    // Signup Form
    private TextInputLayout tilSignupName, tilSignupEmail, tilSignupPassword, tilSignupConfirmPassword, tilSignupPhone;
    private TextInputEditText etSignupName, etSignupEmail, etSignupPassword, etSignupConfirmPassword, etSignupPhone;
    private Spinner spinnerRole, spinnerSchool, spinnerClass;

    // State
    private boolean isLoginMode = true;
    private boolean isLoading = false;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_auth);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupSpinners();
        setupClickListeners();
        setupInitialState();
    }

    private void initializeViews() {
        // Cards
        loginCard = findViewById(R.id.loginCard);
        signupCard = findViewById(R.id.signupCard);

        // Headers
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        tvWelcomeSubtitle = findViewById(R.id.tvWelcomeSubtitle);

        // Switch buttons
        tvSwitchToSignup = findViewById(R.id.tvSwitchToSignup);
        tvSwitchToLogin = findViewById(R.id.tvSwitchToLogin);

        // Action buttons
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        
        // Role selection buttons (for signup)
        btnTeacherSignup = findViewById(R.id.btnTeacherSignup);
        btnParentSignup = findViewById(R.id.btnParentSignup);
        btnAdminSignup = findViewById(R.id.btnAdminSignup);
        roleSelectionButtons = findViewById(R.id.roleSelectionButtons);
        roleButtons = findViewById(R.id.roleButtons);
        
        progressBar = findViewById(R.id.progressBar);

        // Login form
        tilLoginEmail = findViewById(R.id.tilLoginEmail);
        tilLoginPassword = findViewById(R.id.tilLoginPassword);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        // Signup form
        tilSignupName = findViewById(R.id.tilSignupName);
        tilSignupEmail = findViewById(R.id.tilSignupEmail);
        tilSignupPassword = findViewById(R.id.tilSignupPassword);
        tilSignupConfirmPassword = findViewById(R.id.tilSignupConfirmPassword);
        tilSignupPhone = findViewById(R.id.tilSignupPhone);
        etSignupName = findViewById(R.id.etSignupName);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        etSignupConfirmPassword = findViewById(R.id.etSignupConfirmPassword);
        etSignupPhone = findViewById(R.id.etSignupPhone);

        // Spinners
        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        spinnerClass = findViewById(R.id.spinnerClass);
    }

    private void setupSpinners() {
        // Role spinner
        String[] roles = {"Select Role", "Teacher", "Parent", "Student", "Admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // School spinner
        String[] schools = {"Select School", "Eden Family School Kacyiru", "Eden Family School Gisozi", "Eden Family School Kimisagara"};
        ArrayAdapter<String> schoolAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, schools);
        schoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSchool.setAdapter(schoolAdapter);

        // Class spinner
        String[] classes = {"Select Class", "P1", "P2", "P3", "P4", "P5", "P6"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);
    }

    private void setupClickListeners() {
        tvSwitchToSignup.setOnClickListener(v -> switchToSignup());
        tvSwitchToLogin.setOnClickListener(v -> switchToLogin());
        btnLogin.setOnClickListener(v -> performLogin());
        btnSignup.setOnClickListener(v -> performSignup());
        btnForgotPassword.setOnClickListener(v -> openForgotPassword());
        btnAdminLogin.setOnClickListener(v -> openAdminLogin());
        
        // Role-specific signup buttons
        btnTeacherSignup.setOnClickListener(v -> customizeSignupForm("teacher"));
        btnParentSignup.setOnClickListener(v -> customizeSignupForm("parent"));
        btnAdminSignup.setOnClickListener(v -> customizeSignupForm("admin"));
    }

    private void setupInitialState() {
        signupCard.setVisibility(View.GONE);
        signupCard.setAlpha(0f);
        updateHeaderText();
    }

    private void switchToSignup() {
        if (isLoading) return;
        isLoginMode = false;
        animateCardTransition(loginCard, signupCard);
        updateHeaderText();
        showRoleSelectionButtons();
        hideAdminLoginButton();
    }

    private void switchToLogin() {
        if (isLoading) return;
        isLoginMode = true;
        animateCardTransition(signupCard, loginCard);
        updateHeaderText();
        hideRoleSelectionButtons();
        showAdminLoginButton();
    }

    private void animateCardTransition(View fromCard, View toCard) {
        // Fade out current card
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(fromCard, "alpha", 1f, 0f);
        fadeOut.setDuration(200);

        // Slide out current card
        ObjectAnimator slideOut = ObjectAnimator.ofFloat(fromCard, "translationX", 0f, -100f);
        slideOut.setDuration(200);

        // Prepare incoming card
        toCard.setVisibility(View.VISIBLE);
        toCard.setAlpha(0f);
        toCard.setTranslationX(100f);

        // Fade in new card
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(toCard, "alpha", 0f, 1f);
        fadeIn.setDuration(300);
        fadeIn.setStartDelay(150);

        // Slide in new card
        ObjectAnimator slideIn = ObjectAnimator.ofFloat(toCard, "translationX", 100f, 0f);
        slideIn.setDuration(300);
        slideIn.setStartDelay(150);

        // Create animator set
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeOut, slideOut);
        animatorSet.playTogether(fadeIn, slideIn);
        animatorSet.setInterpolator(new DecelerateInterpolator());

        // Hide the outgoing card when animation completes
        fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                fromCard.setVisibility(View.GONE);
            }
        });

        animatorSet.start();
    }

    private void updateHeaderText() {
        if (isLoginMode) {
            tvWelcomeTitle.setText("Welcome Back!");
            tvWelcomeSubtitle.setText("Sign in to your EduTrack account");
        } else {
            tvWelcomeTitle.setText("Join EduTrack!");
            tvWelcomeSubtitle.setText("Create your account to get started");
        }
    }

    private void performLogin() {
        if (isLoading) return;

        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (!validateLoginInput(email, password)) return;

        setLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if user needs email verification (for admin accounts)
                            checkEmailVerificationAndNavigate(user);
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Login failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        android.util.Log.e("UnifiedAuth", "Login failed", task.getException());
                    }
                });
    }

    private void performSignup() {
        if (isLoading) return;

        String name = etSignupName.getText().toString().trim();
        String email = etSignupEmail.getText().toString().trim();
        String password = etSignupPassword.getText().toString().trim();
        String confirmPassword = etSignupConfirmPassword.getText().toString().trim();
        String phone = etSignupPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        String school = spinnerSchool.getSelectedItem().toString();
        String classLevel = spinnerClass.getSelectedItem().toString();

        if (!validateSignupInput(name, email, password, confirmPassword, phone, role, school, classLevel)) return;

        setLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // For admin accounts, require email verification
                            if ("admin".equalsIgnoreCase(role)) {
                                sendEmailVerification(user, name, email, phone, role.toLowerCase(), school, classLevel);
                            } else {
                                createUserProfile(user.getUid(), name, email, phone, role.toLowerCase(), school, classLevel);
                            }
                        }
                    } else {
                        setLoading(false);
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Signup failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        android.util.Log.e("UnifiedAuth", "Signup failed", task.getException());
                    }
                });
    }

    private boolean validateLoginInput(String email, String password) {
        clearErrors();

        if (TextUtils.isEmpty(email)) {
            tilLoginEmail.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilLoginEmail.setError("Please enter a valid email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            tilLoginPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            tilLoginPassword.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private boolean validateSignupInput(String name, String email, String password, String confirmPassword, 
                                       String phone, String role, String school, String classLevel) {
        clearErrors();

        if (TextUtils.isEmpty(name)) {
            tilSignupName.setError("Name is required");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            tilSignupEmail.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilSignupEmail.setError("Please enter a valid email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            tilSignupPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            tilSignupPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            tilSignupConfirmPassword.setError("Passwords do not match");
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            tilSignupPhone.setError("Phone number is required");
            return false;
        }

        if ("Select Role".equals(role)) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return false;
        }

        if ("Select School".equals(school)) {
            Toast.makeText(this, "Please select a school", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void clearErrors() {
        tilLoginEmail.setError(null);
        tilLoginPassword.setError(null);
        tilSignupName.setError(null);
        tilSignupEmail.setError(null);
        tilSignupPassword.setError(null);
        tilSignupConfirmPassword.setError(null);
        tilSignupPhone.setError(null);
    }

    private void createUserProfile(String userId, String name, String email, String phone, 
                                  String role, String school, String classLevel) {
        UserModel user = new UserModel();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setSchool(school);
        user.setClassLevel(classLevel);
        user.setCreatedAt(new Date());
        user.setActive(true);

        // Use set with merge option to avoid update conflicts
        db.collection("users").document(userId)
                .set(user, com.google.firebase.firestore.SetOptions.merge())
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        navigateToHome(role);
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Failed to create profile: " + errorMsg, Toast.LENGTH_LONG).show();
                        // Log the error for debugging
                        android.util.Log.e("UnifiedAuth", "Profile creation failed", task.getException());
                    }
                });
    }

    private void checkEmailVerificationAndNavigate(FirebaseUser user) {
        // First check if user document exists and get role
        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        String role = task.getResult().getString("role");
                        
                        if (role != null) {
                            // For admin accounts, check email verification
                            if ("admin".equalsIgnoreCase(role)) {
                                if (user.isEmailVerified()) {
                                    navigateToHome(role);
                                } else {
                                    // Admin account not verified
                                    new androidx.appcompat.app.AlertDialog.Builder(this)
                                            .setTitle("Email Verification Required")
                                            .setMessage("Please verify your email address before accessing the admin portal. Check your email for verification link.")
                                            .setPositiveButton("Resend Email", (dialog, which) -> {
                                                user.sendEmailVerification();
                                                Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                                            })
                                            .setNegativeButton("OK", null)
                                            .show();
                                    mAuth.signOut();
                                }
                            } else {
                                // Non-admin accounts don't need email verification
                                navigateToHome(role);
                            }
                        } else {
                            // Role is null - this shouldn't happen for existing users
                            android.util.Log.w("UnifiedAuth", "User document exists but role is null for userId: " + user.getUid());
                            // Try to get role from email domain or create basic profile
                            createBasicUserProfile(user);
                        }
                    } else {
                        // Document doesn't exist - check if it's a legacy user or create new profile
                        android.util.Log.i("UnifiedAuth", "User document not found, checking if legacy user or creating profile for: " + user.getUid());
                        handleMissingUserDocument(user);
                    }
                });
    }

    private void handleMissingUserDocument(FirebaseUser user) {
        // For existing Firebase Auth users who don't have a Firestore document yet
        // This handles legacy users or users created outside our new system
        
        // Check if user is in unverified_users collection first
        db.collection("unverified_users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        // User exists in unverified collection
                        if (user.isEmailVerified()) {
                            // Move user from unverified to verified collection
                            moveUserToVerified(task.getResult().getData(), user.getUid());
                        } else {
                            Toast.makeText(this, "Please verify your email address first.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        // User not in unverified collection either - create basic profile
                        createBasicUserProfile(user);
                    }
                });
    }

    private void createBasicUserProfile(FirebaseUser user) {
        // Create a basic user profile for existing Firebase Auth users
        String email = user.getEmail();
        String name = user.getDisplayName() != null ? user.getDisplayName() : "User";
        
        // Try to guess role from email domain or default to parent
        String role = guessRoleFromEmail(email);
        
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setEmail(email);
        userModel.setRole(role);
        userModel.setCreatedAt(new Date());
        userModel.setActive(true);
        
        // Save to Firestore
        db.collection("users").document(user.getUid())
                .set(userModel, com.google.firebase.firestore.SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.i("UnifiedAuth", "Created basic profile for existing user: " + user.getUid());
                        navigateToHome(role);
                    } else {
                        android.util.Log.e("UnifiedAuth", "Failed to create basic profile", task.getException());
                        Toast.makeText(this, "Welcome! Please complete your profile setup.", Toast.LENGTH_LONG).show();
                        // Navigate anyway with default role
                        navigateToHome(role);
                    }
                });
    }

    private String guessRoleFromEmail(String email) {
        if (email == null) return "parent";
        
        String lowerEmail = email.toLowerCase();
        if (lowerEmail.contains("admin") || lowerEmail.contains("administrator")) {
            return "admin";
        } else if (lowerEmail.contains("teacher") || lowerEmail.contains("prof") || lowerEmail.contains("educator")) {
            return "teacher";
        } else if (lowerEmail.contains("student") || lowerEmail.contains("pupil")) {
            return "student";
        } else {
            return "parent"; // Default to parent
        }
    }

    private void checkUnverifiedUser(String userId) {
        db.collection("unverified_users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        // User exists in unverified collection, check if they verified email
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Move user from unverified to verified collection
                            moveUserToVerified(task.getResult().getData(), userId);
                        } else {
                            Toast.makeText(this, "Please verify your email address first.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        // User not found anywhere
                        Toast.makeText(this, "User profile not found. Please sign up again.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                });
    }

    private void moveUserToVerified(Map<String, Object> userData, String userId) {
        if (userData != null) {
            userData.put("active", true);
            userData.put("emailVerified", true);
            
            db.collection("users").document(userId)
                    .set(userData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Remove from unverified collection
                            db.collection("unverified_users").document(userId).delete();
                            
                            String role = (String) userData.get("role");
                            if (role != null) {
                                navigateToHome(role);
                            }
                        } else {
                            Toast.makeText(this, "Failed to activate account. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }


    private void navigateToHome(String role) {
        Intent intent;
        switch (role.toLowerCase()) {
            case "admin":
                intent = new Intent(this, AdminHomeActivityNew.class);
                break;
            case "teacher":
                intent = new Intent(this, TeacherHomeActivity.class);
                break;
            case "parent":
                intent = new Intent(this, ParentHomeActivity.class);
                break;
            case "student":
                // TODO: Create StudentHomeActivity
                Toast.makeText(this, "Student portal - Coming Soon", Toast.LENGTH_SHORT).show();
                return;
            default:
                Toast.makeText(this, "Unknown user role", Toast.LENGTH_SHORT).show();
                return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnSignup.setEnabled(!loading);
        tvSwitchToSignup.setEnabled(!loading);
        tvSwitchToLogin.setEnabled(!loading);
    }

    private void openForgotPassword() {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

    private void openAdminLogin() {
        startActivity(new Intent(this, AdminLoginActivity.class));
    }

    private void showRoleSelectionButtons() {
        roleSelectionButtons.setVisibility(View.VISIBLE);
        roleButtons.setVisibility(View.VISIBLE);
    }

    private void hideRoleSelectionButtons() {
        roleSelectionButtons.setVisibility(View.GONE);
        roleButtons.setVisibility(View.GONE);
    }

    private void showAdminLoginButton() {
        btnAdminLogin.setVisibility(View.VISIBLE);
    }

    private void hideAdminLoginButton() {
        btnAdminLogin.setVisibility(View.GONE);
    }

    private void customizeSignupForm(String role) {
        // Set the role in spinner
        String[] roles = {"Select Role", "Teacher", "Parent", "Student", "Admin"};
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].toLowerCase().equals(role)) {
                spinnerRole.setSelection(i);
                break;
            }
        }

        // Show helpful message
        String roleDisplay = role.substring(0, 1).toUpperCase() + role.substring(1);
        Toast.makeText(this, "Signup form customized for " + roleDisplay + "s", Toast.LENGTH_SHORT).show();

        // Focus on name field
        etSignupName.requestFocus();

        // Customize form based on role
        switch (role.toLowerCase()) {
            case "teacher":
                // Teachers need class and subject info
                spinnerClass.setVisibility(View.VISIBLE);
                break;
            case "parent":
                // Parents don't need class assignment
                spinnerClass.setVisibility(View.GONE);
                break;
            case "admin":
                // Admins don't need class assignment
                spinnerClass.setVisibility(View.GONE);
                Toast.makeText(this, "Admin accounts require email verification", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void sendEmailVerification(FirebaseUser user, String name, String email, String phone, 
                                     String role, String school, String classLevel) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        // Store user data temporarily until verification
                        storeUnverifiedUserData(user.getUid(), name, email, phone, role, school, classLevel);
                        
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Email Verification Required")
                                .setMessage("A verification email has been sent to " + email + 
                                          ". Please verify your email before logging in as an admin.")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    // Sign out the user until they verify
                                    mAuth.signOut();
                                    dialog.dismiss();
                                })
                                .setCancelable(false)
                                .show();
                    } else {
                        Toast.makeText(this, "Failed to send verification email: " + 
                                     task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void storeUnverifiedUserData(String userId, String name, String email, String phone,
                                       String role, String school, String classLevel) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("role", role);
        userData.put("school", school);
        userData.put("classLevel", classLevel);
        userData.put("createdAt", new Date());
        userData.put("active", false); // Not active until verified
        userData.put("emailVerified", false);

        db.collection("unverified_users").document(userId)
                .set(userData)
                .addOnFailureListener(e -> 
                    android.util.Log.e("UnifiedAuth", "Failed to store unverified user data", e));
    }
}
