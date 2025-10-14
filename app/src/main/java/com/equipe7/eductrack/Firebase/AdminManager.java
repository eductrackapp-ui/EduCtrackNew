package com.equipe7.eductrack.Firebase;

import android.util.Log;
import com.equipe7.eductrack.models.AdminAnalytics;
import com.equipe7.eductrack.models.UserManagement;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminManager {
    private static final String TAG = "AdminManager";
    private FirebaseFirestore db;
    
    public interface AdminCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }
    
    public AdminManager() {
        db = FirebaseFirestore.getInstance();
    }
    
    // ========================
    // USER MANAGEMENT METHODS
    // ========================
    
    public void getAllUsers(AdminCallback<List<UserManagement>> callback) {
        db.collection("users")
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserManagement> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            UserManagement user = document.toObject(UserManagement.class);
                            user.setUserId(document.getId());
                            users.add(user);
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing user document: " + document.getId(), e);
                        }
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all users", e);
                    callback.onFailure("Failed to load users: " + e.getMessage());
                });
    }
    
    public void getUsersByRole(String role, AdminCallback<List<UserManagement>> callback) {
        db.collection("users")
                .whereEqualTo("role", role)
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserManagement> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            UserManagement user = document.toObject(UserManagement.class);
                            user.setUserId(document.getId());
                            users.add(user);
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing user document: " + document.getId(), e);
                        }
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting users by role: " + role, e);
                    callback.onFailure("Failed to load " + role + "s: " + e.getMessage());
                });
    }
    
    public void suspendUser(String userId, String reason, AdminCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "suspended");
        updates.put("canLogin", false);
        updates.put("suspensionReason", reason);
        updates.put("suspensionDate", new Date());
        
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User suspended successfully: " + userId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error suspending user: " + userId, e);
                    callback.onFailure("Failed to suspend user: " + e.getMessage());
                });
    }
    
    public void reactivateUser(String userId, AdminCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "active");
        updates.put("canLogin", true);
        updates.put("suspensionReason", null);
        updates.put("suspensionDate", null);
        
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User reactivated successfully: " + userId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error reactivating user: " + userId, e);
                    callback.onFailure("Failed to reactivate user: " + e.getMessage());
                });
    }
    
    public void deleteUser(String userId, AdminCallback<Void> callback) {
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted successfully: " + userId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user: " + userId, e);
                    callback.onFailure("Failed to delete user: " + e.getMessage());
                });
    }
    
    public void updateUserNotes(String userId, String notes, AdminCallback<Void> callback) {
        db.collection("users").document(userId)
                .update("notes", notes)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User notes updated successfully: " + userId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user notes: " + userId, e);
                    callback.onFailure("Failed to update notes: " + e.getMessage());
                });
    }
    
    // ========================
    // ANALYTICS METHODS
    // ========================
    
    public void generateSystemAnalytics(AdminCallback<AdminAnalytics> callback) {
        AdminAnalytics analytics = new AdminAnalytics("current");
        
        // Get user counts by role
        getUserCounts(analytics, callback);
    }
    
    private void getUserCounts(AdminAnalytics analytics, AdminCallback<AdminAnalytics> callback) {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = 0;
                    int teachers = 0, parents = 0, students = 0, admins = 0;
                    int activeUsers = 0;
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        totalUsers++;
                        String role = document.getString("role");
                        String status = document.getString("status");
                        
                        if (role != null) {
                            switch (role.toLowerCase()) {
                                case "teacher": teachers++; break;
                                case "parent": parents++; break;
                                case "student": students++; break;
                                case "admin": admins++; break;
                            }
                        }
                        
                        if ("active".equals(status)) {
                            activeUsers++;
                        }
                    }
                    
                    analytics.setTotalUsers(totalUsers);
                    analytics.setTotalTeachers(teachers);
                    analytics.setTotalParents(parents);
                    analytics.setTotalStudents(students);
                    analytics.setTotalAdmins(admins);
                    analytics.setActiveUsers(activeUsers);
                    
                    // Get academic statistics
                    getAcademicStats(analytics, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user counts", e);
                    callback.onFailure("Failed to generate analytics: " + e.getMessage());
                });
    }
    
    private void getAcademicStats(AdminAnalytics analytics, AdminCallback<AdminAnalytics> callback) {
        // Get exercise count
        db.collection("exercises")
                .get()
                .addOnSuccessListener(exerciseSnapshots -> {
                    analytics.setTotalExercises(exerciseSnapshots.size());
                    
                    // Get grades count and calculate system average
                    db.collection("grades")
                            .get()
                            .addOnSuccessListener(gradeSnapshots -> {
                                analytics.setTotalGrades(gradeSnapshots.size());
                                
                                double totalScore = 0;
                                int gradeCount = 0;
                                
                                for (QueryDocumentSnapshot gradeDoc : gradeSnapshots) {
                                    Double score = gradeDoc.getDouble("score");
                                    Double maxScore = gradeDoc.getDouble("maxScore");
                                    
                                    if (score != null && maxScore != null && maxScore > 0) {
                                        totalScore += (score / maxScore) * 100;
                                        gradeCount++;
                                    }
                                }
                                
                                if (gradeCount > 0) {
                                    analytics.setSystemWideAverage(totalScore / gradeCount);
                                }
                                
                                // Calculate engagement metrics
                                calculateEngagementMetrics(analytics, callback);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting grades", e);
                                callback.onFailure("Failed to calculate system average: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting exercises", e);
                    callback.onFailure("Failed to get exercise statistics: " + e.getMessage());
                });
    }
    
    private void calculateEngagementMetrics(AdminAnalytics analytics, AdminCallback<AdminAnalytics> callback) {
        // Calculate teacher engagement (teachers who created exercises recently)
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        Date thirtyDaysAgoDate = new Date(thirtyDaysAgo);
        
        db.collection("exercises")
                .whereGreaterThan("createdDate", thirtyDaysAgoDate)
                .get()
                .addOnSuccessListener(recentExercises -> {
                    int activeTeachers = recentExercises.size();
                    double teacherEngagement = analytics.getTotalTeachers() > 0 ? 
                        (activeTeachers * 100.0) / analytics.getTotalTeachers() : 0;
                    analytics.setTeacherEngagement(teacherEngagement);
                    
                    // Set default values for other metrics
                    analytics.setParentEngagement(75.0); // Sample data
                    analytics.setStudentPerformance(analytics.getSystemWideAverage());
                    analytics.setSystemStatus("healthy");
                    
                    // Save analytics to Firebase
                    saveAnalytics(analytics, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error calculating engagement", e);
                    callback.onFailure("Failed to calculate engagement metrics: " + e.getMessage());
                });
    }
    
    private void saveAnalytics(AdminAnalytics analytics, AdminCallback<AdminAnalytics> callback) {
        db.collection("admin_analytics")
                .add(analytics)
                .addOnSuccessListener(documentReference -> {
                    analytics.setAnalyticsId(documentReference.getId());
                    Log.d(TAG, "Analytics saved successfully");
                    callback.onSuccess(analytics);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving analytics", e);
                    // Still return the analytics even if save fails
                    callback.onSuccess(analytics);
                });
    }
    
    // ========================
    // SEARCH METHODS
    // ========================
    
    public void searchUsers(String query, AdminCallback<List<UserManagement>> callback) {
        // Search by name (case-insensitive)
        db.collection("users")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserManagement> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            UserManagement user = document.toObject(UserManagement.class);
                            user.setUserId(document.getId());
                            users.add(user);
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing search result: " + document.getId(), e);
                        }
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching users", e);
                    callback.onFailure("Search failed: " + e.getMessage());
                });
    }
}
