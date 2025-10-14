package com.equipe7.eductrack.models;

import java.util.Date;
import java.util.Map;

public class AdminAnalytics {
    private String analyticsId;
    private Date generatedDate;
    private String period; // "daily", "weekly", "monthly", "yearly"
    
    // User Statistics
    private int totalUsers;
    private int totalTeachers;
    private int totalParents;
    private int totalStudents;
    private int totalAdmins;
    private int activeUsers; // Users active in last 30 days
    private int newUsersThisPeriod;
    
    // Academic Statistics
    private int totalClasses;
    private int totalExercises;
    private int totalGrades;
    private double systemWideAverage;
    private int totalAssignments;
    private int completedAssignments;
    
    // Performance Metrics
    private double teacherEngagement; // Percentage of active teachers
    private double parentEngagement; // Percentage of active parents
    private double studentPerformance; // Overall student performance
    private Map<String, Integer> subjectDistribution; // Subject -> Count
    private Map<String, Double> classAverages; // Class -> Average
    
    // System Health
    private int totalNotifications;
    private int unreadNotifications;
    private int systemErrors;
    private String systemStatus; // "healthy", "warning", "critical"
    
    public AdminAnalytics() {
        // Required empty constructor for Firebase
    }
    
    public AdminAnalytics(String period) {
        this.period = period;
        this.generatedDate = new Date();
        this.systemStatus = "healthy";
    }
    
    // Getters and Setters
    public String getAnalyticsId() { return analyticsId; }
    public void setAnalyticsId(String analyticsId) { this.analyticsId = analyticsId; }
    
    public Date getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(Date generatedDate) { this.generatedDate = generatedDate; }
    
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    
    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
    
    public int getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(int totalTeachers) { this.totalTeachers = totalTeachers; }
    
    public int getTotalParents() { return totalParents; }
    public void setTotalParents(int totalParents) { this.totalParents = totalParents; }
    
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    
    public int getTotalAdmins() { return totalAdmins; }
    public void setTotalAdmins(int totalAdmins) { this.totalAdmins = totalAdmins; }
    
    public int getActiveUsers() { return activeUsers; }
    public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }
    
    public int getNewUsersThisPeriod() { return newUsersThisPeriod; }
    public void setNewUsersThisPeriod(int newUsersThisPeriod) { this.newUsersThisPeriod = newUsersThisPeriod; }
    
    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
    
    public int getTotalExercises() { return totalExercises; }
    public void setTotalExercises(int totalExercises) { this.totalExercises = totalExercises; }
    
    public int getTotalGrades() { return totalGrades; }
    public void setTotalGrades(int totalGrades) { this.totalGrades = totalGrades; }
    
    public double getSystemWideAverage() { return systemWideAverage; }
    public void setSystemWideAverage(double systemWideAverage) { this.systemWideAverage = systemWideAverage; }
    
    public int getTotalAssignments() { return totalAssignments; }
    public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }
    
    public int getCompletedAssignments() { return completedAssignments; }
    public void setCompletedAssignments(int completedAssignments) { this.completedAssignments = completedAssignments; }
    
    public double getTeacherEngagement() { return teacherEngagement; }
    public void setTeacherEngagement(double teacherEngagement) { this.teacherEngagement = teacherEngagement; }
    
    public double getParentEngagement() { return parentEngagement; }
    public void setParentEngagement(double parentEngagement) { this.parentEngagement = parentEngagement; }
    
    public double getStudentPerformance() { return studentPerformance; }
    public void setStudentPerformance(double studentPerformance) { this.studentPerformance = studentPerformance; }
    
    public Map<String, Integer> getSubjectDistribution() { return subjectDistribution; }
    public void setSubjectDistribution(Map<String, Integer> subjectDistribution) { this.subjectDistribution = subjectDistribution; }
    
    public Map<String, Double> getClassAverages() { return classAverages; }
    public void setClassAverages(Map<String, Double> classAverages) { this.classAverages = classAverages; }
    
    public int getTotalNotifications() { return totalNotifications; }
    public void setTotalNotifications(int totalNotifications) { this.totalNotifications = totalNotifications; }
    
    public int getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(int unreadNotifications) { this.unreadNotifications = unreadNotifications; }
    
    public int getSystemErrors() { return systemErrors; }
    public void setSystemErrors(int systemErrors) { this.systemErrors = systemErrors; }
    
    public String getSystemStatus() { return systemStatus; }
    public void setSystemStatus(String systemStatus) { this.systemStatus = systemStatus; }
    
    // Helper methods
    public String getSystemWideAverageDisplay() {
        return String.format("%.1f%%", systemWideAverage);
    }
    
    public String getTeacherEngagementDisplay() {
        return String.format("%.1f%%", teacherEngagement);
    }
    
    public String getParentEngagementDisplay() {
        return String.format("%.1f%%", parentEngagement);
    }
    
    public String getStudentPerformanceDisplay() {
        return String.format("%.1f%%", studentPerformance);
    }
    
    public int getCompletionRate() {
        return totalAssignments > 0 ? (completedAssignments * 100) / totalAssignments : 0;
    }
    
    public String getCompletionRateDisplay() {
        return getCompletionRate() + "%";
    }
    
    public String getSystemStatusColor() {
        switch (systemStatus.toLowerCase()) {
            case "healthy": return "#4CAF50";
            case "warning": return "#FF9800";
            case "critical": return "#F44336";
            default: return "#9E9E9E";
        }
    }
    
    public int getActiveUserPercentage() {
        return totalUsers > 0 ? (activeUsers * 100) / totalUsers : 0;
    }
    
    public String getFormattedDate() {
        if (generatedDate == null) return "Unknown";
        return android.text.format.DateFormat.format("MMM dd, yyyy", generatedDate).toString();
    }
}
