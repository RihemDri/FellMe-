package com.example.myapplication.model;

public class Comment {
    private String userId;
    private String name;
    private String comment;

    // Default constructor is required for Firebase
    public Comment() {
    }

    // Constructor
    public Comment(String userId, String name, String comment) {
        this.userId = userId;
        this.name = name;
        this.comment = comment;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
