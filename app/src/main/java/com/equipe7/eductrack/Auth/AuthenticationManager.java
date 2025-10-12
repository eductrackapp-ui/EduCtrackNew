package com.equipe7.eductrack.Auth;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Centralized Authentication Manager for all user types
 * Handles registration, login, role assignment, and validation
 */
public class AuthenticationManager {
    
    private static final String TAG = "AuthenticationManager";
    private static AuthenticationManager instance;
    
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseFunctions functions;
    private Context context;
    
    // User types
    public enum UserType {
        PARENT("parent", "users"),
        TEACHER("teacher", "teachers"),
        STUDENT("student", "students"),
        ADMIN("admin", "admins");
        
        private final String role;
        private final String collection;
        
        UserType(String role, String collection) {
            this.role = role;
            this.collection = collection;
        }
        
        public String getRole() { return role; }
        public String getCollection() { return collection; }
    }
    
    // Interfaces for callbacks
    public interface AuthCallback {
        void onSuccess(String message, String userId);
        void onFailure(String error);
        void onProgress(String message);
    }
    
    public interface ValidationCallback {
        void onValid();
        void onInvalid(String error);
    }
    
    private AuthenticationManager(Context context) {
        this.context = context.getApplicationContext();
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.functions = FirebaseFunctions.getInstance();
    }
    
    public static synchronized AuthenticationManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthenticationManager(context);
        }
        return instance;
    }
    
    // ========================================================================================
    // REGISTRATION METHODS
    // ========================================================================================
    
    /**
     * Register a new user with comprehensive validation and role assignment
     */
    public void registerUser(UserType userType, Map<String, Object> userData, 
                           String email, String password, AuthCallback callback) {
        
        callback.onProgress("Validating user data...");
        
        // Validate input data
        validateUserData(userType, userData, email, password, new ValidationCallback() {
            @Override
            public void onValid() {
                callback.onProgress("Checking email availability...");
                checkEmailAvailability(email, new ValidationCallback() {
                    @Override
                    public void onValid() {
                        callback.onProgress("Creating user account...");
                        createUserAccount(userType, userData, email, password, callback);
                    }
                    
                    @Override
                    public void onInvalid(String error) {
                        callback.onFailure(error);
                    }
                });
            }
            
            @Override
            public void onInvalid(String error) {
                callback.onFailure(error);
            }
        });
    }
    
    /**
     * Create user account in Firebase Auth and Firestore
     */
    private void createUserAccount(UserType userType, Map<String, Object> userData, 
                                 String email, String password, AuthCallback callback) {
        
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        String uid = user.getUid();
                        callback.onProgress("Saving user profile...");
                        
                        // Prepare user data with required fields
                        Map<String, Object> completeUserData = prepareUserData(userType, userData, uid, email);
                        
                        // Save to Firestore
                        saveUserToFirestore(userType, uid, completeUserData, callback);
                    } else {
                        callback.onFailure("Failed to create user account");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Auth creation failed", e);
                    callback.onFailure("Registration failed: " + e.getMessage());
                });
    }
    
    /**
     * Save user data to appropriate Firestore collection
     */
    private void saveUserToFirestore(UserType userType, String uid, 
                                   Map<String, Object> userData, AuthCallback callback) {
        
        db.collection(userType.getCollection())
                .document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    callback.onProgress("Assigning user role...");
                    
                    // Assign role via Cloud Function for teachers and admins
                    if (userType == UserType.TEACHER || userType == UserType.ADMIN) {
                        assignUserRole(uid, userType.getRole(), callback);
                    } else {
                        callback.onSuccess("Registration successful!", uid);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore save failed", e);
                    // Clean up auth account if Firestore fails
                    FirebaseUser currentUser = auth.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.delete();
                    }
                    callback.onFailure("Failed to save user profile: " + e.getMessage());
                });
    }
    
    /**
     * Assign user role via Cloud Function
     */
    private void assignUserRole(String uid, String role, AuthCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("role", role);
        
        functions.getHttpsCallable("assignUserRole")
                .call(data)
                .addOnSuccessListener(result -> {
                    Log.d(TAG, "Role assigned successfully: " + role);
                    callback.onSuccess("Registration successful! Role: " + role, uid);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Role assignment failed, but user created", e);
                    // Don't fail registration if role assignment fails
                    callback.onSuccess("Registration successful! (Role assignment pending)", uid);
                });
    }
    
    // ========================================================================================
    // LOGIN METHODS
    // ========================================================================================
    
    /**
     * Sign in user with email and password
     */
    public void signInUser(String email, String password, AuthCallback callback) {
        callback.onProgress("Signing in...");
        
        // Validate input
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onFailure("Please enter a valid email address");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            callback.onFailure("Please enter your password");
            return;
        }
        
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        callback.onProgress("Verifying user role...");
                        verifyUserRole(user.getUid(), callback);
                    } else {
                        callback.onFailure("Login failed: User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Sign in failed", e);
                    callback.onFailure("Login failed: " + e.getMessage());
                });
    }
    
    /**
     * Verify user role and update last login
     */
    private void verifyUserRole(String uid, AuthCallback callback) {
        // Check in all user collections to find the user
        checkUserInCollection("users", uid, callback, () -> 
            checkUserInCollection("teachers", uid, callback, () -> 
                checkUserInCollection("students", uid, callback, () -> 
                    checkUserInCollection("admins", uid, callback, () -> 
                        callback.onFailure("User profile not found. Please contact support.")
                    )
                )
            )
        );
    }
    
    /**
     * Check user in specific collection
     */
    private void checkUserInCollection(String collection, String uid, 
                                     AuthCallback callback, Runnable onNotFound) {
        db.collection(collection).document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String role = document.getString("role");
                        updateLastLogin(collection, uid);
                        callback.onSuccess("Login successful! Role: " + role, uid);
                    } else {
                        onNotFound.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error checking " + collection, e);
                    onNotFound.run();
                });
    }
    
    /**
     * Update last login timestamp
     */
    private void updateLastLogin(String collection, String uid) {
        db.collection(collection).document(uid)
                .update("lastLogin", System.currentTimeMillis())
                .addOnFailureListener(e -> Log.w(TAG, "Failed to update last login", e));
    }
    
    // ========================================================================================
    // VALIDATION METHODS
    // ========================================================================================
    
    /**
     * Validate user data based on user type
     */
    private void validateUserData(UserType userType, Map<String, Object> userData, 
                                String email, String password, ValidationCallback callback) {
        
        // Common validations
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onInvalid("Please enter a valid email address");
            return;
        }
        
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            callback.onInvalid("Password must be at least 6 characters long");
            return;
        }
        
        String firstName = (String) userData.get("firstName");
        String lastName = (String) userData.get("lastName");
        
        if (TextUtils.isEmpty(firstName)) {
            callback.onInvalid("Please enter first name");
            return;
        }
        
        if (TextUtils.isEmpty(lastName)) {
            callback.onInvalid("Please enter last name");
            return;
        }
        
        // Type-specific validations
        switch (userType) {
            case PARENT:
                validateParentData(userData, callback);
                break;
            case TEACHER:
                validateTeacherData(userData, callback);
                break;
            case STUDENT:
                validateStudentData(userData, callback);
                break;
            case ADMIN:
                validateAdminData(userData, callback);
                break;
            default:
                callback.onInvalid("Invalid user type");
        }
    }
    
    private void validateParentData(Map<String, Object> userData, ValidationCallback callback) {
        String relation = (String) userData.get("relation");
        String school = (String) userData.get("school");
        
        if (TextUtils.isEmpty(relation) || "Select relationship".equals(relation)) {
            callback.onInvalid("Please select your relationship to the student");
            return;
        }
        
        if (TextUtils.isEmpty(school) || "Select school".equals(school)) {
            callback.onInvalid("Please select a school");
            return;
        }
        
        callback.onValid();
    }
    
    private void validateTeacherData(Map<String, Object> userData, ValidationCallback callback) {
        String gender = (String) userData.get("gender");
        String school = (String) userData.get("school");
        String classLevel = (String) userData.get("classLevel");
        String position = (String) userData.get("position");
        
        if (TextUtils.isEmpty(gender)) {
            callback.onInvalid("Please select gender");
            return;
        }
        
        if (TextUtils.isEmpty(school)) {
            callback.onInvalid("Please select school");
            return;
        }
        
        if (TextUtils.isEmpty(classLevel)) {
            callback.onInvalid("Please select class level");
            return;
        }
        
        if (TextUtils.isEmpty(position)) {
            callback.onInvalid("Please select position");
            return;
        }
        
        callback.onValid();
    }
    
    private void validateStudentData(Map<String, Object> userData, ValidationCallback callback) {
        String school = (String) userData.get("school");
        String classLevel = (String) userData.get("classLevel");
        
        if (TextUtils.isEmpty(school)) {
            callback.onInvalid("Please select school");
            return;
        }
        
        if (TextUtils.isEmpty(classLevel)) {
            callback.onInvalid("Please select class level");
            return;
        }
        
        callback.onValid();
    }
    
    private void validateAdminData(Map<String, Object> userData, ValidationCallback callback) {
        String department = (String) userData.get("department");
        
        if (TextUtils.isEmpty(department)) {
            callback.onInvalid("Please select department");
            return;
        }
        
        callback.onValid();
    }
    
    /**
     * Check if email is already registered
     */
    private void checkEmailAvailability(String email, ValidationCallback callback) {
        auth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(result -> {
                    if (result.getSignInMethods() != null && !result.getSignInMethods().isEmpty()) {
                        callback.onInvalid("Email is already registered. Please use a different email or sign in.");
                    } else {
                        callback.onValid();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Email check failed", e);
                    callback.onInvalid("Failed to verify email availability. Please try again.");
                });
    }
    
    // ========================================================================================
    // UTILITY METHODS
    // ========================================================================================
    
    /**
     * Prepare complete user data with required fields
     */
    private Map<String, Object> prepareUserData(UserType userType, Map<String, Object> userData, 
                                               String uid, String email) {
        Map<String, Object> completeData = new HashMap<>(userData);
        
        // Add common required fields
        completeData.put("uid", uid);
        completeData.put("email", email);
        completeData.put("role", userType.getRole());
        completeData.put("isActive", true);
        completeData.put("createdAt", System.currentTimeMillis());
        completeData.put("lastLogin", null);
        completeData.put("updatedAt", System.currentTimeMillis());
        
        // Add type-specific fields
        switch (userType) {
            case TEACHER:
                completeData.put("teacherCode", generateTeacherCode(
                    (String) userData.get("school"), 
                    (String) userData.get("classLevel")
                ));
                completeData.put("isOnline", false);
                break;
            case STUDENT:
                completeData.put("studentCode", generateStudentCode());
                break;
            case PARENT:
                completeData.put("parentCode", generateParentCode());
                break;
            case ADMIN:
                completeData.put("adminCode", generateAdminCode());
                break;
        }
        
        return completeData;
    }
    
    /**
     * Generate unique teacher code
     */
    private String generateTeacherCode(String school, String classLevel) {
        String schoolPrefix = getSchoolPrefix(school);
        String classSuffix = getClassSuffix(classLevel);
        int randomDigits = 1000 + new Random().nextInt(9000);
        return "EDF-" + schoolPrefix + "-" + classSuffix + "-" + randomDigits;
    }
    
    /**
     * Generate unique student code
     */
    private String generateStudentCode() {
        int code = 100000000 + new Random().nextInt(900000000);
        return "STU-" + code;
    }
    
    /**
     * Generate unique parent code
     */
    private String generateParentCode() {
        int code = 10000000 + new Random().nextInt(90000000);
        return "PAR-" + code;
    }
    
    /**
     * Generate unique admin code
     */
    private String generateAdminCode() {
        int code = 1000 + new Random().nextInt(9000);
        return "ADM-" + code;
    }
    
    private String getSchoolPrefix(String school) {
        if (school == null) return "XX";
        String s = school.trim().toLowerCase();
        if (s.contains("kacyiru")) return "KA";
        if (s.contains("gisozi")) return "GI";
        if (s.contains("kimisagara")) return "KI";
        return "XX";
    }
    
    private String getClassSuffix(String classLevel) {
        if (classLevel == null) return "P0";
        String c = classLevel.trim().toLowerCase();
        if (c.contains("p1") || c.contains("class a")) return "P1";
        if (c.contains("p2") || c.contains("class b")) return "P2";
        if (c.contains("p3") || c.contains("class c")) return "P3";
        if (c.contains("p4") || c.contains("class d")) return "P4";
        if (c.contains("p5") || c.contains("class e")) return "P5";
        if (c.contains("p6") || c.contains("class f")) return "P6";
        return "P0";
    }
    
    // ========================================================================================
    // SESSION MANAGEMENT
    // ========================================================================================
    
    /**
     * Get current user
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
    
    /**
     * Sign out current user
     */
    public void signOut() {
        auth.signOut();
    }
    
    /**
     * Check if user is signed in
     */
    public boolean isUserSignedIn() {
        return auth.getCurrentUser() != null;
    }
}
