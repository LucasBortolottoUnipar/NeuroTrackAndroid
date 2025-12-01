package com.example.neurotrack.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskInstance {
    private Long taskInstanceId;
    private Long taskId;
    private String scheduledFor;
    private String status;
    private String plannedTime;
    private String completedAt;
    private Integer pointsAwarded;
    

    private String taskName;
    private String description;
    private String iconCode;
    

    private Long childUserId;
    private String childName;

    public TaskInstance() {}

    public Long getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(Long taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(String scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlannedTime() {
        return plannedTime;
    }

    public void setPlannedTime(String plannedTime) {
        this.plannedTime = plannedTime;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(Integer pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getIconCode() {
        return iconCode;
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }

    public Long getChildUserId() {
        return childUserId;
    }

    public void setChildUserId(Long childUserId) {
        this.childUserId = childUserId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }



    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    
    public boolean isMissedByNow() {
        if ("COMPLETED".equals(status)) {
            return false;
        }

        if ("MISSED".equals(status)) {
            return true;
        }

        if (scheduledFor == null || plannedTime == null ||
                scheduledFor.isEmpty() || plannedTime.isEmpty()) {
            return false;
        }

        try {

            String dateTimeStr = scheduledFor + " " + plannedTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date scheduled = sdf.parse(dateTimeStr);
            if (scheduled == null) return false;

            Calendar limit = Calendar.getInstance();
            limit.setTime(scheduled);
            limit.add(Calendar.MINUTE, 10); // tolerância de 10 minutos

            Date now = new Date();
            return now.after(limit.getTime());
        } catch (Exception e) {
            return false;
        }
    }

    public String getTitle() {
        return taskName;
    }
    
    public Integer getPoints() {
        return pointsAwarded;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}

