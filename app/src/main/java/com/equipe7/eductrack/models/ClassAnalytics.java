package com.equipe7.eductrack.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ClassAnalytics {
    private String analyticsId;
    private String teacherId;
    private String className;
    private String subject;
    private Date generatedDate;
    private String period; // "weekly", "monthly", "semester"
    
    // Class Performance Metrics
    private double classAverage;
    private double highestGrade;
    private double lowestGrade;
    private int totalStudents;
    private int strugglingStudents; // Students below 60%
    private int excellentStudents; // Students above 85%
    
    // Subject Performance
    private Map<String, Double> subjectAverages; // Subject -> Average
    private Map<String, Integer> subjectCounts; // Subject -> Number of assignments
    
    // Student Performance
    private Map<String, StudentPerformanceSummary> studentSummaries;
    private List<String> topPerformers; // Student IDs
    private List<String> needsAttention; // Student IDs who need help
    
    // Trends
    private String performanceTrend; // "improving", "declining", "stable"
    private double trendPercentage; // Percentage change
    
    public static class StudentPerformanceSummary {
        public String studentId;
        public String studentName;
        public double average;
        public int assignmentCount;
        public String trend;
        public String status; // "excellent", "good", "needs_attention"
        
        public StudentPerformanceSummary() {}
        
        public StudentPerformanceSummary(String studentId, String studentName, double average) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.average = average;
            this.assignmentCount = 0;
            this.trend = "stable";
            updateStatus();
        }
        
        private void updateStatus() {
            if (average >= 85) status = "excellent";
            else if (average >= 70) status = "good";
            else status = "needs_attention";
        }
        
        public String getStatusColor() {
            switch (status) {
                case "excellent": return "#4CAF50";
                case "good": return "#2196F3";
                case "needs_attention": return "#F44336";
                default: return "#9E9E9E";
            }
        }
        
        public String getGradeDisplay() {
            return String.format("%.1f%%", average);
        }
    }
    
    public ClassAnalytics() {
        // Required empty constructor for Firebase
    }
    
    public ClassAnalytics(String teacherId, String className, String subject, String period) {
        this.teacherId = teacherId;
        this.className = className;
        this.subject = subject;
        this.period = period;
        this.generatedDate = new Date();
        this.performanceTrend = "stable";
        this.trendPercentage = 0.0;
    }
    
    // Getters and Setters
    public String getAnalyticsId() { return analyticsId; }
    public void setAnalyticsId(String analyticsId) { this.analyticsId = analyticsId; }
    
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public Date getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(Date generatedDate) { this.generatedDate = generatedDate; }
    
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    
    public double getClassAverage() { return classAverage; }
    public void setClassAverage(double classAverage) { this.classAverage = classAverage; }
    
    public double getHighestGrade() { return highestGrade; }
    public void setHighestGrade(double highestGrade) { this.highestGrade = highestGrade; }
    
    public double getLowestGrade() { return lowestGrade; }
    public void setLowestGrade(double lowestGrade) { this.lowestGrade = lowestGrade; }
    
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    
    public int getStrugglingStudents() { return strugglingStudents; }
    public void setStrugglingStudents(int strugglingStudents) { this.strugglingStudents = strugglingStudents; }
    
    public int getExcellentStudents() { return excellentStudents; }
    public void setExcellentStudents(int excellentStudents) { this.excellentStudents = excellentStudents; }
    
    public Map<String, Double> getSubjectAverages() { return subjectAverages; }
    public void setSubjectAverages(Map<String, Double> subjectAverages) { this.subjectAverages = subjectAverages; }
    
    public Map<String, Integer> getSubjectCounts() { return subjectCounts; }
    public void setSubjectCounts(Map<String, Integer> subjectCounts) { this.subjectCounts = subjectCounts; }
    
    public Map<String, StudentPerformanceSummary> getStudentSummaries() { return studentSummaries; }
    public void setStudentSummaries(Map<String, StudentPerformanceSummary> studentSummaries) { this.studentSummaries = studentSummaries; }
    
    public List<String> getTopPerformers() { return topPerformers; }
    public void setTopPerformers(List<String> topPerformers) { this.topPerformers = topPerformers; }
    
    public List<String> getNeedsAttention() { return needsAttention; }
    public void setNeedsAttention(List<String> needsAttention) { this.needsAttention = needsAttention; }
    
    public String getPerformanceTrend() { return performanceTrend; }
    public void setPerformanceTrend(String performanceTrend) { this.performanceTrend = performanceTrend; }
    
    public double getTrendPercentage() { return trendPercentage; }
    public void setTrendPercentage(double trendPercentage) { this.trendPercentage = trendPercentage; }
    
    // Helper methods
    public String getClassAverageDisplay() {
        return String.format("%.1f%%", classAverage);
    }
    
    public String getTrendDisplay() {
        String sign = trendPercentage >= 0 ? "+" : "";
        return String.format("%s%.1f%%", sign, trendPercentage);
    }
    
    public String getTrendColor() {
        switch (performanceTrend) {
            case "improving": return "#4CAF50";
            case "declining": return "#F44336";
            case "stable": return "#2196F3";
            default: return "#9E9E9E";
        }
    }
    
    public String getPerformanceLevel() {
        if (classAverage >= 85) return "Excellent";
        if (classAverage >= 75) return "Good";
        if (classAverage >= 65) return "Fair";
        return "Needs Improvement";
    }
    
    public int getStrugglingPercentage() {
        return totalStudents > 0 ? (strugglingStudents * 100) / totalStudents : 0;
    }
    
    public int getExcellentPercentage() {
        return totalStudents > 0 ? (excellentStudents * 100) / totalStudents : 0;
    }
}
