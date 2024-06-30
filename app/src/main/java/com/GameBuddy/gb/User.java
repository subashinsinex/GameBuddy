package com.GameBuddy.gb;

public class User {
    private String uid;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String profile_pic;
    private String status;

    public User(String uid, String username, String name, String email, String phone, String status, String profile_pic) {
        this.uid = uid;
        this.username = username;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.profile_pic = profile_pic;
    }

    public User() {
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public String getStatus() {
        return status;
    }
}
