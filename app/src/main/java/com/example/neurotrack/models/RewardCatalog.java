package com.example.neurotrack.models;

public class RewardCatalog {
    private Long rewardId;
    private Long guardianUserId;
    private String name;
    private String description;
    private Integer costPoints;
    private String createdAt;

    public RewardCatalog() {}

    public RewardCatalog(String name, String description, Integer costPoints, Long guardianUserId) {
        this.name = name;
        this.description = description;
        this.costPoints = costPoints;
        this.guardianUserId = guardianUserId;
    }

    public Long getRewardId() {
        return rewardId;
    }

    public void setRewardId(Long rewardId) {
        this.rewardId = rewardId;
    }

    public Long getGuardianUserId() {
        return guardianUserId;
    }

    public void setGuardianUserId(Long guardianUserId) {
        this.guardianUserId = guardianUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCostPoints() {
        return costPoints;
    }

    public void setCostPoints(Integer costPoints) {
        this.costPoints = costPoints;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

