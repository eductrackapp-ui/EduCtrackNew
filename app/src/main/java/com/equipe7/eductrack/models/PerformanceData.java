package com.equipe7.eductrack.models;

import java.util.Date;
import java.util.Map;

public class PerformanceData {
    private String performanceId;
    private String childId;
    private String subject;
    private double grade;
    private String type; // "exercise", "homework", "exam", "quiz"
    private Date date;
    private String week;
    private String month;
    private String semester;
    private String title;
    private String description;
    private double maxGrade;
    private String teacherComment;
    
    public PerformanceData() {
        // Required empty constructor for Firebase
    }
    
    public PerformanceData(String childId, String subject, double grade, String type) {
        this.childId = childId;
        this.subject = subject;
        this.grade = grade;
        this.type = type;
        this.date = new Date();
        this.maxGrade = 100.0;
    }
    
    // Getters and Setters
    public String getPerformanceId() { return performanceId; }
    public void setPerformanceId(String performanceId) { this.performanceId = performanceId; }
    
    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    
    public String getWeek() { return week; }
    public void setWeek(String week) { this.week = week; }
    
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getMaxGrade() { return maxGrade; }
    public void setMaxGrade(double maxGrade) { this.maxGrade = maxGrade; }
    
    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
    
    // Helper methods
    public double getPercentage() {
        return (grade / maxGrade) * 100;
    }
    
    public String getGradeDisplay() {
        return String.format("%.1f/%.0f", grade, maxGrade);
    }
    
    public String getPercentageDisplay() {
        return String.format("%.1f%%", getPercentage());
    }
    
    public String getGradeColor() {
        double percentage = getPercentage();
        if (percentage >= 85) return "#4CAF50"; // Green
        if (percentage >= 70) return "#2196F3"; // Blue
        if (percentage >= 60) return "#FF9800"; // Orange
        return "#F44336"; // Red
    }
    
    public String getTypeIcon() {
        switch (type.toLowerCase()) {
            case "exam": return "📝";
            case "homework": return "📚";
            case "exercise": return "✏️";
            case "quiz": return "❓";
            default: return "📋";
        }
    }
}
