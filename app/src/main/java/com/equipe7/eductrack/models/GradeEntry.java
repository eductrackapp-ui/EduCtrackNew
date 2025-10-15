package com.equipe7.eductrack.models;

import com.google.firebase.firestore.Exclude;
import java.util.Date;

public class GradeEntry {
    private String gradeId;
    private String exerciseId;
    private String studentId;
    private String studentName;
    private String teacherId;
    private String subject;
    private double score;
    private double maxScore;
    private String feedback;
    private Date gradedDate;
    private String status; // "pending", "graded", "reviewed"
    private boolean isLate;
    private Date submissionDate;
    private String exerciseTitle;
    private String exerciseType;
    
    public GradeEntry() {
        // Required empty constructor for Firebase
    }
    
    public GradeEntry(String exerciseId, String studentId, String teacherId, String subject) {
        this.exerciseId = exerciseId;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.subject = subject;
        this.gradedDate = new Date();
        this.status = "pending";
        this.isLate = false;
        this.maxScore = 100.0;
    }
    
    // Getters and Setters
    public String getGradeId() { return gradeId; }
    public void setGradeId(String gradeId) { this.gradeId = gradeId; }
    
    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public double getScore() { return score; }
    public void setScore(double score) { 
        this.score = score;
        if (this.status != null && this.status.equals("pending")) {
            this.status = "graded";
        }
    }
    
    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
    
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    
    public Date getGradedDate() { return gradedDate; }
    public void setGradedDate(Date gradedDate) { this.gradedDate = gradedDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public boolean isLate() { return isLate; }
    public void setLate(boolean late) { isLate = late; }
    
    public Date getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(Date submissionDate) { this.submissionDate = submissionDate; }
    
    public String getExerciseTitle() { return exerciseTitle; }
    public void setExerciseTitle(String exerciseTitle) { this.exerciseTitle = exerciseTitle; }
    
    public String getExerciseType() { return exerciseType; }
    public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }
    
    // Helper methods
    public double getPercentage() {
        return maxScore > 0 ? (score / maxScore) * 100 : 0;
    }
    
    public String getPercentageDisplay() {
        return String.format("%.1f%%", getPercentage());
    }
    
    public String getScoreDisplay() {
        return String.format("%.1f/%.0f", score, maxScore);
    }
    
    @Exclude
    public String getGradeColor() {
        double percentage = getPercentage();
        if (percentage >= 85) return "#4CAF50"; // Green
        if (percentage >= 70) return "#2196F3"; // Blue
        if (percentage >= 60) return "#FF9800"; // Orange
        return "#F44336"; // Red
    }
    
    public String getGradeLetter() {
        double percentage = getPercentage();
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }
    
    public String getPerformanceLevel() {
        double percentage = getPercentage();
        if (percentage >= 85) return "Excellent";
        if (percentage >= 70) return "Good";
        if (percentage >= 60) return "Fair";
        return "Needs Improvement";
    }
    
    @Exclude
    public String getStatusColor() {
        if (status == null) return "#9E9E9E";
        switch (status.toLowerCase()) {
            case "pending": return "#FF9800";
            case "graded": return "#4CAF50";
            case "published": return "#2196F3";
            default: return "#9E9E9E";
        }
    }
    
    public String getFormattedGradedDate() {
        if (gradedDate == null) return "Not graded";
        return android.text.format.DateFormat.format("MMM dd, yyyy", gradedDate).toString();
    }
    
    public boolean isPassing() {
        return getPercentage() >= 60;
    }
    
    public boolean isExcellent() {
        return getPercentage() >= 85;
    }
}
