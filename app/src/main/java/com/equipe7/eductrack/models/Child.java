package com.equipe7.eductrack.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Child implements Parcelable {
    private String childId;
    private String name;
    private String className;
    private String grade;
    private String profileImageUrl;
    private List<String> subjects;
    private String parentId;
    private String schoolYear;
    private Map<String, Double> currentGrades;
    private double overallAverage;
    private String status; // "excellent", "good", "needs_improvement"
    private String studentCode; // Unique randomized student code
    private String branch; // School branch (Kacyiru, Gisozi, Kimisagara)
    
    public Child() {
        // Required empty constructor for Firebase
    }
    
    public Child(String childId, String name, String className, String grade) {
        this.childId = childId;
        this.name = name;
        this.className = className;
        this.grade = grade;
        this.overallAverage = 0.0;
        this.status = "good";
    }
    
    // Getters and Setters
    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }
    
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    
    public String getSchoolYear() { return schoolYear; }
    public void setSchoolYear(String schoolYear) { this.schoolYear = schoolYear; }
    
    public Map<String, Double> getCurrentGrades() { return currentGrades; }
    public void setCurrentGrades(Map<String, Double> currentGrades) { this.currentGrades = currentGrades; }
    
    public double getOverallAverage() { return overallAverage; }
    public void setOverallAverage(double overallAverage) { 
        this.overallAverage = overallAverage;
        updateStatus();
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // Helper methods
    private void updateStatus() {
        if (overallAverage >= 85) {
            status = "excellent";
        } else if (overallAverage >= 70) {
            status = "good";
        } else {
            status = "needs_improvement";
        }
    }
    
    public String getStatusColor() {
        switch (status) {
            case "excellent": return "#4CAF50"; // Green
            case "good": return "#2196F3"; // Blue
            case "needs_improvement": return "#FF9800"; // Orange
            default: return "#9E9E9E"; // Gray
        }
    }
    
    public String getGradeDisplay() {
        return String.format("%.1f%%", overallAverage);
    }
    
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    // Parcelable implementation
    protected Child(Parcel in) {
        childId = in.readString();
        name = in.readString();
        className = in.readString();
        grade = in.readString();
        profileImageUrl = in.readString();
        subjects = in.createStringArrayList();
        parentId = in.readString();
        schoolYear = in.readString();
        
        // Read currentGrades map
        int gradesSize = in.readInt();
        currentGrades = new HashMap<>();
        for (int i = 0; i < gradesSize; i++) {
            String key = in.readString();
            Double value = in.readDouble();
            currentGrades.put(key, value);
        }
        
        overallAverage = in.readDouble();
        status = in.readString();
        studentCode = in.readString();
        branch = in.readString();
    }

    public static final Creator<Child> CREATOR = new Creator<Child>() {
        @Override
        public Child createFromParcel(Parcel in) {
            return new Child(in);
        }

        @Override
        public Child[] newArray(int size) {
            return new Child[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(childId);
        dest.writeString(name);
        dest.writeString(className);
        dest.writeString(grade);
        dest.writeString(profileImageUrl);
        dest.writeStringList(subjects);
        dest.writeString(parentId);
        dest.writeString(schoolYear);
        
        // Write currentGrades map
        if (currentGrades != null) {
            dest.writeInt(currentGrades.size());
            for (Map.Entry<String, Double> entry : currentGrades.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeDouble(entry.getValue());
            }
        } else {
            dest.writeInt(0);
        }
        
        dest.writeDouble(overallAverage);
        dest.writeString(status);
        dest.writeString(studentCode);
        dest.writeString(branch);
    }
}
