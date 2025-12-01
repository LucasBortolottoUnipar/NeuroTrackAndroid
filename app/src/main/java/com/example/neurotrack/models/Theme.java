package com.example.neurotrack.models;

import com.google.gson.annotations.SerializedName;

public class Theme {
    @SerializedName("themeId")
    private Long id;
    private String code;
    private String name;
    private String bgUrl;
    private String iconBaseUrl;
    private String iconSuffix;
    private String stepBaseUrl;
    private String stepSuffix;
    private Integer assetsVersion;

    public Theme() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBgUrl() {
        return bgUrl;
    }

    public void setBgUrl(String bgUrl) {
        this.bgUrl = bgUrl;
    }

    public String getIconBaseUrl() {
        return iconBaseUrl;
    }

    public void setIconBaseUrl(String iconBaseUrl) {
        this.iconBaseUrl = iconBaseUrl;
    }

    public String getIconSuffix() {
        return iconSuffix;
    }

    public void setIconSuffix(String iconSuffix) {
        this.iconSuffix = iconSuffix;
    }

    public String getStepBaseUrl() {
        return stepBaseUrl;
    }

    public void setStepBaseUrl(String stepBaseUrl) {
        this.stepBaseUrl = stepBaseUrl;
    }

    public String getStepSuffix() {
        return stepSuffix;
    }

    public void setStepSuffix(String stepSuffix) {
        this.stepSuffix = stepSuffix;
    }

    public Integer getAssetsVersion() {
        return assetsVersion;
    }

    public void setAssetsVersion(Integer assetsVersion) {
        this.assetsVersion = assetsVersion;
    }
}

