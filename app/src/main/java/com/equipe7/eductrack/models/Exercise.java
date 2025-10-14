package com.equipe7.eductrack.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Exercise {
    private String exerciseId;
    private String teacherId;
    private String teacherName;
    private String subject;
    private String skill; // e.g., "Solving equations – Mathematics"
    private String title;
    private String description;
    private String type; // "exercise", "assignment", "exam", "quiz"
    private Date createdDate;
    private Date dueDate;
    private double maxPoints;
    private String difficulty; // "easy", "medium", "hard"
    private String className;
    private String gradeLevel;
    private List<String> targetStudents; // Student IDs
    private Map<String, Double> studentGrades; // StudentId -> Grade
    private boolean isPublished;
    private String instructions;
    private List<String> attachments; // File URLs
    private String status; // "draft", "published", "completed", "graded"
    
    public Exercise() {
        // Required empty constructor for Firebase
    }
    
    public Exercise(String teacherId, String subject, String title, String type) {
        this.teacherId = teacherId;
        this.subject = subject;
        this.title = title;
        this.type = type;
        this.createdDate = new Date();
        this.maxPoints = 100.0;
        this.difficulty = "medium";
        this.isPublished = false;
        this.status = "draft";
    }
    
    // Getters and Setters
    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }
    
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    
    public double getMaxPoints() { return maxPoints; }
    public void setMaxPoints(double maxPoints) { this.maxPoints = maxPoints; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
    
    public List<String> getTargetStudents() { return targetStudents; }
    public void setTargetStudents(List<String> targetStudents) { this.targetStudents = targetStudents; }
    
    public Map<String, Double> getStudentGrades() { return studentGrades; }
    public void setStudentGrades(Map<String, Double> studentGrades) { this.studentGrades = studentGrades; }
    
    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }
    
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    
    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // Helper methods
    public String getTypeIcon() {
        switch (type.toLowerCase()) {
            case "exam": return "📝";
            case "assignment": return "📋";
            case "exercise": return "✏️";
            case "quiz": return "❓";
            default: return "📚";
        }
    }
    
    public String getDifficultyColor() {
        switch (difficulty.toLowerCase()) {
            case "easy": return "#4CAF50";
            case "medium": return "#FF9800";
            case "hard": return "#F44336";
            default: return "#9E9E9E";
        }
    }
    
    public String getStatusColor() {
        switch (status.toLowerCase()) {
            case "draft": return "#9E9E9E";
            case "published": return "#2196F3";
            case "completed": return "#FF9800";
            case "graded": return "#4CAF50";
            default: return "#9E9E9E";
        }
    }
    
    public int getCompletionPercentage() {
        if (targetStudents == null || targetStudents.isEmpty()) return 0;
        if (studentGrades == null) return 0;
        
        int gradedCount = 0;
        for (String studentId : targetStudents) {
            if (studentGrades.containsKey(studentId)) {
                gradedCount++;
            }
        }
        return (int) ((gradedCount * 100.0) / targetStudents.size());
    }
    
    public double getClassAverage() {
        if (studentGrades == null || studentGrades.isEmpty()) return 0.0;
        
        double total = 0.0;
        int count = 0;
        for (Double grade : studentGrades.values()) {
            if (grade != null) {
                total += (grade / maxPoints) * 100;
                count++;
            }
        }
        return count > 0 ? total / count : 0.0;
    }
    
    public String getFormattedDueDate() {
        if (dueDate == null) return "No due date";
        return android.text.format.DateFormat.format("MMM dd, yyyy", dueDate).toString();
    }
}
