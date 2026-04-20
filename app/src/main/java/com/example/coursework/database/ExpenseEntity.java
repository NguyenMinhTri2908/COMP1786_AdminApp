package com.example.coursework.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

// @Entity định nghĩa bảng expense_table
// Phần foreignKeys thiết lập liên kết: Xóa một Project thì toàn bộ Expense của Project đó cũng bị xóa theo (CASCADE)
@Entity(tableName = "expense_table",
        foreignKeys = @ForeignKey(entity = ProjectEntity.class,
                parentColumns = "id",
                childColumns = "projectId",
                onDelete = ForeignKey.CASCADE))
public class ExpenseEntity {

    @PrimaryKey(autoGenerate = true)
    private int expenseId;

    // Đây chính là Khóa ngoại liên kết với id của bảng project_table
    private int projectId;

    private String type; // Ví dụ: Travel, Food, Equipment
    private double amount;
    private String time;
    private String comment;

    // Constructor
    public ExpenseEntity(int projectId, String type, double amount, String time, String comment) {
        this.projectId = projectId;
        this.type = type;
        this.amount = amount;
        this.time = time;
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }
}