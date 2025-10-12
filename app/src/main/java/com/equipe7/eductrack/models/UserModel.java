package com.equipe7.eductrack.models;

public class UserModel {
    private String name;
    private String email;
    private String role; // admin, teacher, student, parent
    private String subject; // matière enseignée (si teacher)
    private String assignedClass; // classe assignée (ex: P1, P2...)
    private String code; // code unique (ex: EDF-TCH-001)

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
}
