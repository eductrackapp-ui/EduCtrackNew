package com.equipe7.eductrack.Utils;

import java.util.Date;

public class Teacher {
    private String teacherCode;   // ex: EDF-KA-P1-1234
    private String name;          // Nom complet
    private String classLevel;    // Classe affectée (P1...P6)
    private String email;         // Email du professeur
    private String phone;         // Numéro de téléphone
    private String subject;       // Matière enseignée
    private boolean isOnline;     // Statut en ligne
    private Date lastSeen;        // Dernière connexion
    private String profileImageUrl; // URL de l'image de profil

    public Teacher() {
        // Constructeur vide requis par Firestore
        this.isOnline = false;
        this.lastSeen = new Date();
    }

    public Teacher(String teacherCode, String name, String classLevel) {
        this.teacherCode = teacherCode;
        this.name = name;
        this.classLevel = classLevel;
        this.isOnline = false;
        this.lastSeen = new Date();
    }

    public Teacher(String teacherCode, String name, String classLevel, String email, String phone, String subject) {
        this.teacherCode = teacherCode;
        this.name = name;
        this.classLevel = classLevel;
        this.email = email;
        this.phone = phone;
        this.subject = subject;
        this.isOnline = false;
        this.lastSeen = new Date();
    }

    // Getters
    public String getTeacherCode() {
        return teacherCode != null ? teacherCode : "";
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getClassLevel() {
        return classLevel != null ? classLevel : "";
    }

    // Additional getters
    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getSubject() {
        return subject;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // Setters
    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClassLevel(String classLevel) {
        this.classLevel = classLevel;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // Utility methods
    public String getDisplayName() {
        return name != null ? name : "Unknown Teacher";
    }

    public String getStatusText() {
        return isOnline ? "Online" : "Offline";
    }

    public String getClassAndSubject() {
        StringBuilder sb = new StringBuilder();
        if (classLevel != null) {
            sb.append(classLevel);
        }
        if (subject != null) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(subject);
        }
        return sb.toString();
    }
}
