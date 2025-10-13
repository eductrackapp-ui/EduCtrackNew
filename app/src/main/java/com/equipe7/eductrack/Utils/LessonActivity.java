package com.equipe7.eductrack.Utils;

import java.util.List;

public class LessonActivity {
    private String id;
    private String classLevel;
    private String subject;
    private String title;
    private String description;
    private String teacherId;
    private String teacherName;
    private List<String> topics;
    private int duration; // in minutes
    private String status; // "active", "completed", "draft"
    private long createdAt;
    private long updatedAt;

    public LessonActivity() {
        // Empty constructor for Firestore
        this.status = "draft";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public LessonActivity(String classLevel, String subject, String title) {
        this();
        this.classLevel = classLevel;
        this.subject = subject;
        this.title = title;
    }

    public LessonActivity(String classLevel, String subject, String title, String description, String teacherId, String teacherName) {
        this(classLevel, subject, title);
        this.description = description;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
    }

    // Getters
    public String getId() {
        return id != null ? id : "";
    }

    public String getClassLevel() {
        return classLevel != null ? classLevel : "";
    }

    public String getSubject() {
        return subject != null ? subject : "";
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public String getTeacherId() {
        return teacherId != null ? teacherId : "";
    }

    public String getTeacherName() {
        return teacherName != null ? teacherName : "";
    }

    public List<String> getTopics() {
        return topics;
    }

    public int getDuration() {
        return duration;
    }

    public String getStatus() {
        return status != null ? status : "draft";
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setClassLevel(String classLevel) {
        this.classLevel = classLevel;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    public String getDisplayTitle() {
        return title != null && !title.isEmpty() ? title : subject + " - " + classLevel;
    }

    public String getDurationText() {
        if (duration <= 0) return "Duration not set";
        int hours = duration / 60;
        int minutes = duration % 60;
        
        if (hours > 0) {
            return hours + "h " + (minutes > 0 ? minutes + "m" : "");
        } else {
            return minutes + " minutes";
        }
    }

    public String getStatusColor() {
        switch (getStatus().toLowerCase()) {
            case "active":
                return "#4CAF50"; // Green
            case "completed":
                return "#2196F3"; // Blue
            case "draft":
            default:
                return "#FF9800"; // Orange
        }
    }

    public String getStatusText() {
        switch (getStatus().toLowerCase()) {
            case "active":
                return "Active";
            case "completed":
                return "Completed";
            case "draft":
            default:
                return "Draft";
        }
    }

    public int getTopicsCount() {
        return topics != null ? topics.size() : 0;
    }
}
