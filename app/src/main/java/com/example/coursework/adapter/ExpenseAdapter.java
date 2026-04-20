package com.example.coursework.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coursework.R;
import com.example.coursework.database.ExpenseEntity;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<ExpenseEntity> expenseList;

    public ExpenseAdapter(List<ExpenseEntity> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = expenseList.get(position);
        holder.tvType.setText(expense.getType());
        holder.tvAmount.setText("$" + expense.getAmount());
        holder.tvTime.setText(expense.getTime());
        holder.tvComment.setText(expense.getComment());
    }

    @Override
    public int getItemCount() { return expenseList.size(); }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvAmount, tvTime, tvComment;
        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvExpenseType);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvTime = itemView.findViewById(R.id.tvExpenseTime);
            tvComment = itemView.findViewById(R.id.tvExpenseComment);
        }
    }
}