package com.example.coursework.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AppDao {

    // PROJECT

    @Insert
    long insertProject(ProjectEntity project);

    @Update
    void updateProject(ProjectEntity project);

    @Delete
    void deleteProject(ProjectEntity project);

    @Query("SELECT * FROM project_table ORDER BY id DESC")
    List<ProjectEntity> getAllProjects();


    // EXPENSE

    @Insert
    void insertExpense(ExpenseEntity expense);

    @Update
    void updateExpense(ExpenseEntity expense);

    @Delete
    void deleteExpense(ExpenseEntity expense);

    @Query("SELECT * FROM expense_table WHERE projectId = :projId ORDER BY expenseId DESC")
    List<ExpenseEntity> getExpensesForProject(int projId);

    @Query("SELECT * FROM expense_table")
    List<ExpenseEntity> getAllExpenses();
}