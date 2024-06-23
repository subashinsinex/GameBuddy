package com.GameBuddy.gb;

public class User {
    private String username;
    private String email;
    private String phone;
    private String status;

    public User(String username, String email, String phone, String status) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }
}