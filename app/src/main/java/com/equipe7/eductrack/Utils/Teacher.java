package com.equipe7.eductrack.Utils;

public class Teacher {
    private String teacherCode;   // ex: EDF-KA-P1-1234
    private String name;          // Nom complet
    private String classLevel;    // Classe affectée (P1...P6)

    public Teacher() {
        // Constructeur vide requis par Firestore
    }

    public Teacher(String teacherCode, String name, String classLevel) {
        this.teacherCode = teacherCode;
        this.name = name;
        this.classLevel = classLevel;
    }

    // Getters
    public String getTeacherCode() {
        return teacherCode;
    }

    public String getName() {
        return name;
    }

    public String getClassLevel() {
        return classLevel;
    }

    // Setters (optionnels si tu veux modifier après création)
    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClassLevel(String classLevel) {
        this.classLevel = classLevel;
    }
}
