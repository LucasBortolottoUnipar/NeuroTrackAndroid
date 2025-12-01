package com.example.neurotrack.models;


public class LoginResponse {
    private String token;
    private Long userId;
    private String fullName;
    private String role;

    public LoginResponse() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isGuardian() {
        return "GUARDIAN".equals(role);
    }

    public boolean isChild() {
        return "CHILD".equals(role);
    }
}

