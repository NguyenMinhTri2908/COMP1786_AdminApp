package com.example.coursework.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "expense_table",
        foreignKeys = @ForeignKey(entity = ProjectEntity.class,
                parentColumns = "id",
                childColumns = "projectId",
                onDelete = ForeignKey.CASCADE))
public class ExpenseEntity {

    @PrimaryKey(autoGenerate = true)
    private int expenseId;
    private int projectId;

    private String date;           // Mới thêm
    private String type;
    private double amount;
    private String currency;       // Mới thêm
    private String paymentMethod;  // Mới thêm
    private String time;
    private String comment;
    private String claimant;       // Mới thêm
    private String paymentStatus;  // Mới thêm

    // Constructor đã được cập nhật
    public ExpenseEntity(int projectId, String date, String type, double amount, String currency, String paymentMethod, String time, String comment, String claimant, String paymentStatus) {
        this.projectId = projectId;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.time = time;
        this.comment = comment;
        this.claimant = claimant;
        this.paymentStatus = paymentStatus;
    }

    // Getters and Setters
    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getClaimant() { return claimant; }
    public void setClaimant(String claimant) { this.claimant = claimant; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}