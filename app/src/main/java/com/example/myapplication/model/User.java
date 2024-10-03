package com.example.myapplication.model;

public class User {
    private String fullName;
    private String activity;
    private String gender;
    private String dob; // Date of birth as a String, e.g., "yyyy-MM-dd"
    private String role;

    // Default constructor is required for Firebase
    public User() {
    }

    // Constructor
    public User(String fullName, String activity, String gender, String dob, String role) {
        this.fullName = fullName;
        this.activity = activity;
        this.gender = gender;
        this.dob = dob;
        this.role = role;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getActivity() {
        return activity;
    }

    public String getGender() {
        return gender;
    }

    public String getDob() {
        return dob;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", activity='" + activity + '\'' +
                ", gender='" + gender + '\'' +
                ", dob='" + dob + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}

