package com.example.neurotrack.models;

public class CreateUserRequest {
    private String role;
    private String name;
    private String email;
    private String password;
    private String birthDate;
    private String condition;
    private Long themeId;
    private Long guardianUserId;

    public CreateUserRequest() {}

    public CreateUserRequest(String name, String email, String password, String role, String birthDate) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.birthDate = birthDate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Long getThemeId() {
        return themeId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }

    public Long getGuardianUserId() {
        return guardianUserId;
    }

    public void setGuardianUserId(Long guardianUserId) {
        this.guardianUserId = guardianUserId;
    }
}

