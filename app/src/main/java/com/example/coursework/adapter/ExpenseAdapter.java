package com.example.coursework.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coursework.ExpenseActivity;
import com.example.coursework.R;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ExpenseEntity;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<ExpenseEntity> expenseList;
    private String projectName; // Dùng để truyền tên Project khi muốn sửa Expense

    public ExpenseAdapter(List<ExpenseEntity> expenseList, String projectName) {
        this.expenseList = expenseList;
        this.projectName = projectName;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = expenseList.get(position);

        holder.tvType.setText("Type: " + expense.getType());
        holder.tvAmount.setText("Amount: " + expense.getAmount() + " " + expense.getCurrency());
        holder.tvDate.setText("Date: " + expense.getDate());
        holder.tvClaimant.setText("Claimant: " + expense.getClaimant());
        holder.tvPaymentStatus.setText("Status: " + expense.getPaymentStatus());

        // CHỨC NĂNG SỬA KHOẢN CHI
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ExpenseActivity.class);
            intent.putExtra("PROJECT_ID", expense.getProjectId());
            intent.putExtra("PROJECT_NAME", projectName);
            intent.putExtra("EXPENSE_ID", expense.getExpenseId());
            // Bạn có thể truyền thêm các thông tin khác của Expense qua Intent để fill sẵn vào UI
            v.getContext().startActivity(intent);
        });

        // CHỨC NĂNG XÓA KHOẢN CHI
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        new Thread(() -> {
                            AppDatabase.getInstance(v.getContext()).appDao().deleteExpense(expense);
                            ((Activity) v.getContext()).runOnUiThread(() -> {
                                expenseList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, expenseList.size());
                            });
                        }).start();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvAmount, tvDate, tvClaimant, tvPaymentStatus;
        Button btnEdit, btnDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvExpenseType);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
            tvClaimant = itemView.findViewById(R.id.tvExpenseClaimant);
            tvPaymentStatus = itemView.findViewById(R.id.tvExpensePaymentStatus);

            btnEdit = itemView.findViewById(R.id.btnEditExpense);
            btnDelete = itemView.findViewById(R.id.btnDeleteExpense);
        }
    }
}