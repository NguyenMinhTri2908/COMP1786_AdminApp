package com.example.coursework.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "project_table")
public class ProjectEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String projectIdCode; // Mới thêm
    private String name;
    private String destination;
    private String startDate;     // Mới thêm
    private String endDate;       // Mới thêm
    private boolean requiresRiskAssessment;
    private String description;
    private double budget;
    private String owner;         // Mới thêm
    private String status;        // Mới thêm (Active, Completed, On Hold)

    // Constructor đã được cập nhật
    public ProjectEntity(String projectIdCode, String name, String destination, String startDate, String endDate, boolean requiresRiskAssessment, String description, double budget, String owner, String status) {
        this.projectIdCode = projectIdCode;
        this.name = name;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.requiresRiskAssessment = requiresRiskAssessment;
        this.description = description;
        this.budget = budget;
        this.owner = owner;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProjectIdCode() { return projectIdCode; }
    public void setProjectIdCode(String projectIdCode) { this.projectIdCode = projectIdCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isRequiresRiskAssessment() { return requiresRiskAssessment; }
    public void setRequiresRiskAssessment(boolean requiresRiskAssessment) { this.requiresRiskAssessment = requiresRiskAssessment; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}