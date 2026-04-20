package com.example.coursework.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AppDao {

    // ================= LỆNH CHO PROJECT =================

    // Trả về long (là ID của project vừa được tạo thành công)
    @Insert
    long insertProject(ProjectEntity project);

    // Lấy toàn bộ danh sách dự án, sắp xếp mới nhất lên đầu
    @Query("SELECT * FROM project_table ORDER BY id DESC")
    List<ProjectEntity> getAllProjects();


    // ================= LỆNH CHO EXPENSE =================

    @Insert
    void insertExpense(ExpenseEntity expense);

    // Lấy toàn bộ khoản chi tiêu của MỘT dự án cụ thể dựa vào projectId
    @Query("SELECT * FROM expense_table WHERE projectId = :projId ORDER BY expenseId DESC")
    List<ExpenseEntity> getExpensesForProject(int projId);
}
