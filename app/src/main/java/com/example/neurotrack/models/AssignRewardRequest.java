package com.example.neurotrack.models;

public class AssignRewardRequest {

    private Long childUserId;
    private Long rewardId;

    public AssignRewardRequest() {}

    public AssignRewardRequest(Long childUserId, Long rewardId) {
        this.childUserId = childUserId;
        this.rewardId = rewardId;
    }

    public Long getChildUserId() {
        return childUserId;
    }

    public void setChildUserId(Long childUserId) {
        this.childUserId = childUserId;
    }

    public Long getRewardId() {
        return rewardId;
    }

    public void setRewardId(Long rewardId) {
        this.rewardId = rewardId;
    }
}

