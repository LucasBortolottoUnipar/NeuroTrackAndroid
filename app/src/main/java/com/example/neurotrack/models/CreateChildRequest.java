package com.example.neurotrack.models;

public class CreateChildRequest {
    
    private String fullName;
    private String birthDate;
    private String condition;
    private String avatar;
    private Long themeId;
    
    public CreateChildRequest() {}
    
    public CreateChildRequest(String fullName, String birthDate, String condition, String avatar, Long themeId) {
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.condition = condition;
        this.avatar = avatar;
        this.themeId = themeId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
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
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public Long getThemeId() {
        return themeId;
    }
    
    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }
}

