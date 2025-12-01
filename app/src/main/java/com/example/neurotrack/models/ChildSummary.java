package com.example.neurotrack.models;

import com.google.gson.annotations.SerializedName;

public class ChildSummary {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("totalPoints")
    private Integer totalPoints;

    @SerializedName("tasksCompletedToday")
    private Integer tasksCompletedToday;

    @SerializedName("totalTasksToday")
    private Integer totalTasksToday;

    public ChildSummary() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    
    public Integer getTotalPoints() {
        return totalPoints != null ? totalPoints : 0;
    }

    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    
    public Integer getTasksCompletedToday() {
        return tasksCompletedToday != null ? tasksCompletedToday : 0;
    }

    public void setTasksCompletedToday(Integer tasksCompletedToday) { this.tasksCompletedToday = tasksCompletedToday; }

    
    public Integer getTotalTasksToday() {
        return totalTasksToday != null ? totalTasksToday : 0;
    }

    public void setTotalTasksToday(Integer totalTasksToday) { this.totalTasksToday = totalTasksToday; }
}

