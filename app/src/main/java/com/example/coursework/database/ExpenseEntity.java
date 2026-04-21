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
    private String expenseCode;
    private String date;           // Mới thêm
    private String type;
    private double amount;
    private String currency;
    private String paymentMethod;  // Mới thêm
    private String description;
    private String location;    // Thêm mới
    private String claimant;       // Mới thêm
    private String paymentStatus;  // Mới thêm

    // Constructor đã được cập nhật
    public ExpenseEntity(int projectId,String expenseCode, String date, String type, double amount, String currency, String paymentMethod, String description, String location, String claimant, String paymentStatus) {
        this.projectId = projectId;
        this.expenseCode = expenseCode;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.location = location;
        this.claimant = claimant;
        this.paymentStatus = paymentStatus;
    }

    // Getters and Setters
    public int getExpenseId() { return expenseId; }

    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }
    public String getExpenseCode() { return expenseCode; }
    public void setExpenseCode(String expenseCode) { this.expenseCode = expenseCode; }

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getClaimant() { return claimant; }
    public void setClaimant(String claimant) { this.claimant = claimant; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}