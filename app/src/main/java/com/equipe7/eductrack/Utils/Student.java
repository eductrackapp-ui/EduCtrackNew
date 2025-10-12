package com.equipe7.eductrack.Utils;

public class Student {
    private String name;
    private String studentClass;
    private String school;
    private String studentCode;

    // Constructeur vide requis par Firebase
    public Student() {
    }

    // Constructeur complet
    public Student(String name, String studentClass, String school, String studentCode) {
        this.name = name;
        this.studentClass = studentClass;
        this.school = school;
        this.studentCode = studentCode;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public String getSchool() {
        return school;
    }

    public String getStudentCode() {
        return studentCode;
    }

    // Setters (optionnels si tu veux modifier après création)
    public void setName(String name) {
        this.name = name;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }
}
