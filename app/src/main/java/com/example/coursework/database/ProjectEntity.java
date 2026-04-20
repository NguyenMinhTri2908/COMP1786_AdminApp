package com.example.coursework.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// @Entity đánh dấu đây là một bảng trong SQLite.

@Entity(tableName = "project_table")
public class ProjectEntity {

    // @PrimaryKey(autoGenerate = true) giúp ID tự động tăng (1, 2, 3...)
    @PrimaryKey(autoGenerate = true)
    private int id;

    // Các cột dữ liệu dựa theo yêu cầu của Coursework
    private String name;
    private String destination;
    private String date;
    private boolean requiresRiskAssessment;
    private String description;
    private double budget;

    // Constructor: Hàm khởi tạo để tạo ra một dự án mới
    public ProjectEntity(String name, String destination, String date, boolean requiresRiskAssessment, String description, double budget) {
        this.name = name;
        this.destination = destination;
        this.date = date;
        this.requiresRiskAssessment = requiresRiskAssessment;
        this.description = description;
        this.budget = budget;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isRequiresRiskAssessment() {
        return requiresRiskAssessment;
    }

    public void setRequiresRiskAssessment(boolean requiresRiskAssessment) {
        this.requiresRiskAssessment = requiresRiskAssessment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

}
