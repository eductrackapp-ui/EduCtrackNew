package com.equipe7.eductrack.models;

import java.util.Date;

public class UserModel {
    private String name;
    private String email;
    private String role; // admin, teacher, student, parent
    private String subject; // matière enseignée (si teacher)
    private String assignedClass; // classe assignée (ex: P1, P2...)
    private String code; // code unique (ex: EDF-TCH-001)
    
    // Additional fields for enhanced auth
    private String phone;
    private String school;
    private String classLevel;
    private Date createdAt;
    private boolean active;

    // 🔥 Obligatoire pour Firestore
    public UserModel() {}

    // Constructeur complet
    public UserModel(String name, String email, String role, String subject, String assignedClass, String code) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.subject = subject;
        this.assignedClass = assignedClass;
        this.code = code;
    }

    // --- Getters et Setters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getAssignedClass() { return assignedClass; }
    public void setAssignedClass(String assignedClass) { this.assignedClass = assignedClass; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    // Additional getters and setters
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    
    public String getClassLevel() { return classLevel; }
    public void setClassLevel(String classLevel) { this.classLevel = classLevel; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
