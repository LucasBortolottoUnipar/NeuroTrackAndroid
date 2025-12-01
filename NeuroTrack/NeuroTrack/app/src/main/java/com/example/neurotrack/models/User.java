package com.example.neurotrack.models;

public class User {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String condition;

    public User() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isGuardian() {
        return "GUARDIAN".equals(role);
    }

    public boolean isChild() {
        return "CHILD".equals(role);
    }
}

