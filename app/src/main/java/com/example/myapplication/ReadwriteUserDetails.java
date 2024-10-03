package com.example.myapplication;

public class ReadwriteUserDetails {
    public  String fullName, activity, gender,dob, role ;

    public ReadwriteUserDetails() {
    }

    public ReadwriteUserDetails(String fullName, String activity , String gender, String dob, String role ) {
        this.fullName = fullName;
        this.activity = activity;
        this.gender = gender;
        this.dob = dob;
        this.role = role;

    }
}
