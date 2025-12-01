package com.example.neurotrack.models;

public class CreateTaskInstanceRequest {

    private Long taskId;
    private Long childUserId;
    private String scheduledFor;
    private String plannedTime;
    private String status;
    private Integer pointsAwarded;
    private String recurrenceType;

    public CreateTaskInstanceRequest() {}

    public CreateTaskInstanceRequest(Long taskId, Long childUserId, String scheduledFor, String plannedTime) {
        this.taskId = taskId;
        this.childUserId = childUserId;
        this.scheduledFor = scheduledFor;
        this.plannedTime = plannedTime;
        this.status = "PENDING";
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getChildUserId() {
        return childUserId;
    }

    public void setChildUserId(Long childUserId) {
        this.childUserId = childUserId;
    }

    public String getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(String scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public String getPlannedTime() {
        return plannedTime;
    }

    public void setPlannedTime(String plannedTime) {
        this.plannedTime = plannedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(Integer pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }
}

