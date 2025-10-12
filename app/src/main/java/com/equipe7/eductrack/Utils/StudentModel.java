package com.equipe7.eductrack.Utils;

public class StudentModel {
    private String name;
    private String studentClass;
    private String school;
    private String studentCode;

    // 🔥 Constructeur vide requis par Firestore
    public StudentModel(String name, String studentClass, String code) {}

    // ✅ Constructeur complet
    public StudentModel(String name, String studentClass, String school, String studentCode) {
        this.name = name;
        this.studentClass = studentClass;
        this.school = school;
        this.studentCode = studentCode;
    }

    // --- Getters ---
    public String getName() { return name; }
    public String getStudentClass() { return studentClass; }
    public String getSchool() { return school; }
    public String getStudentCode() { return studentCode; }

    // --- Setters ---
    public void setName(String name) { this.name = name; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }
    public void setSchool(String school) { this.school = school; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
}
