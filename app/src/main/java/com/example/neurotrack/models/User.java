package com.example.neurotrack.models;

import com.google.gson.annotations.SerializedName;


public class User {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("condition")
    private String condition;

    @SerializedName("dateOfBirth")
    private String birthDate;

    @SerializedName("themeId")
    private Long themeId;

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

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public Long getThemeId() {
        return themeId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }

    public boolean isGuardian() {
        return "GUARDIAN".equals(role);
    }

    public boolean isChild() {
        return "CHILD".equals(role);
    }
}
