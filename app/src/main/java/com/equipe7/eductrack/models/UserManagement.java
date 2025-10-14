package com.equipe7.eductrack.models;

import java.util.Date;
import java.util.List;

public class UserManagement {
    private String userId;
    private String name;
    private String email;
    private String role; // "admin", "teacher", "parent", "student"
    private String status; // "active", "inactive", "suspended", "pending"
    private Date createdDate;
    private Date lastLoginDate;
    private String profileImageUrl;
    
    // Role-specific information
    private String assignedClass; // For teachers and students
    private String subject; // For teachers
    private String parentId; // For students
    private List<String> childrenIds; // For parents
    private String institutionId;
    private String phoneNumber;
    private String address;
    
    // Activity tracking
    private int loginCount;
    private boolean isOnline;
    private Date lastActivityDate;
    private String deviceInfo;
    
    // Performance metrics (for teachers and students)
    private double performanceScore;
    private int totalAssignments; // Created by teacher or assigned to student
    private int completedTasks;
    private String performanceLevel; // "excellent", "good", "average", "needs_improvement"
    
    // Admin control fields
    private boolean canLogin;
    private boolean emailVerified;
    private String suspensionReason;
    private Date suspensionDate;
    private String notes; // Admin notes about the user
    
    public UserManagement() {
        // Required empty constructor for Firebase
    }
    
    public UserManagement(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = "active";
        this.createdDate = new Date();
        this.canLogin = true;
        this.emailVerified = false;
        this.isOnline = false;
        this.loginCount = 0;
        this.performanceScore = 0.0;
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    
    public Date getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(Date lastLoginDate) { this.lastLoginDate = lastLoginDate; }
    
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    public String getAssignedClass() { return assignedClass; }
    public void setAssignedClass(String assignedClass) { this.assignedClass = assignedClass; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    
    public List<String> getChildrenIds() { return childrenIds; }
    public void setChildrenIds(List<String> childrenIds) { this.childrenIds = childrenIds; }
    
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public int getLoginCount() { return loginCount; }
    public void setLoginCount(int loginCount) { this.loginCount = loginCount; }
    
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
    
    public Date getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(Date lastActivityDate) { this.lastActivityDate = lastActivityDate; }
    
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    
    public double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(double performanceScore) { 
        this.performanceScore = performanceScore;
        updatePerformanceLevel();
    }
    
    public int getTotalAssignments() { return totalAssignments; }
    public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }
    
    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
    
    public String getPerformanceLevel() { return performanceLevel; }
    public void setPerformanceLevel(String performanceLevel) { this.performanceLevel = performanceLevel; }
    
    public boolean isCanLogin() { return canLogin; }
    public void setCanLogin(boolean canLogin) { this.canLogin = canLogin; }
    
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public String getSuspensionReason() { return suspensionReason; }
    public void setSuspensionReason(String suspensionReason) { this.suspensionReason = suspensionReason; }
    
    public Date getSuspensionDate() { return suspensionDate; }
    public void setSuspensionDate(Date suspensionDate) { this.suspensionDate = suspensionDate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Helper methods
    private void updatePerformanceLevel() {
        if (performanceScore >= 85) performanceLevel = "excellent";
        else if (performanceScore >= 70) performanceLevel = "good";
        else if (performanceScore >= 60) performanceLevel = "average";
        else performanceLevel = "needs_improvement";
    }
    
    public String getRoleColor() {
        switch (role.toLowerCase()) {
            case "admin": return "#F44336";
            case "teacher": return "#2196F3";
            case "parent": return "#4CAF50";
            case "student": return "#FF9800";
            default: return "#9E9E9E";
        }
    }
    
    public String getStatusColor() {
        switch (status.toLowerCase()) {
            case "active": return "#4CAF50";
            case "inactive": return "#9E9E9E";
            case "suspended": return "#F44336";
            case "pending": return "#FF9800";
            default: return "#9E9E9E";
        }
    }
    
    public String getPerformanceColor() {
        switch (performanceLevel) {
            case "excellent": return "#4CAF50";
            case "good": return "#2196F3";
            case "average": return "#FF9800";
            case "needs_improvement": return "#F44336";
            default: return "#9E9E9E";
        }
    }
    
    public String getFormattedCreatedDate() {
        if (createdDate == null) return "Unknown";
        return android.text.format.DateFormat.format("MMM dd, yyyy", createdDate).toString();
    }
    
    public String getFormattedLastLogin() {
        if (lastLoginDate == null) return "Never";
        return android.text.format.DateFormat.format("MMM dd, yyyy HH:mm", lastLoginDate).toString();
    }
    
    public boolean isActive() {
        return "active".equals(status) && canLogin;
    }
    
    public int getCompletionRate() {
        return totalAssignments > 0 ? (completedTasks * 100) / totalAssignments : 0;
    }
    
    public String getPerformanceDisplay() {
        return String.format("%.1f%%", performanceScore);
    }
    
    public String getRoleDisplayName() {
        return role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
    }
}
