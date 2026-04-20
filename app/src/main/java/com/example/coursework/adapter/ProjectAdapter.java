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
import com.example.coursework.MainActivity;
import com.example.coursework.R;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ProjectEntity;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<ProjectEntity> projectList;

    public ProjectAdapter(List<ProjectEntity> projectList) {
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectEntity project = projectList.get(position);
        holder.tvName.setText(project.getName());
        holder.tvDestination.setText("Destination: " + project.getDestination());
        holder.tvDate.setText("Start Date: " + project.getStartDate()); // Sửa lại thành Start Date
        holder.tvBudget.setText("Budget: $" + project.getBudget());

        // Chuyển sang màn hình Expense khi bấm vào toàn bộ item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), com.example.coursework.ExpenseActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            intent.putExtra("PROJECT_NAME", project.getName());
            v.getContext().startActivity(intent);
        });

        // 1. CHỨC NĂNG SỬA (EDIT)
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            intent.putExtra("PROJECT_NAME", project.getName());
            intent.putExtra("PROJECT_DESTINATION", project.getDestination());
            intent.putExtra("PROJECT_BUDGET", project.getBudget());
            v.getContext().startActivity(intent);
        });

        // 2. CHỨC NĂNG XÓA (DELETE)
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Project")
                    .setMessage("Are you sure you want to delete this project? All related expenses will also be deleted.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        new Thread(() -> {
                            AppDatabase.getInstance(v.getContext()).appDao().deleteProject(project);
                            ((Activity) v.getContext()).runOnUiThread(() -> {
                                projectList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, projectList.size());
                            });
                        }).start();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDestination, tvDate, tvBudget;
        Button btnEdit, btnDelete; // Đã thêm 2 nút này

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDestination = itemView.findViewById(R.id.tvItemDestination);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvBudget = itemView.findViewById(R.id.tvItemBudget);

            // LƯU Ý: BẠN CẦN THÊM 2 NÚT NÀY VÀO item_project.xml
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public void updateList(List<ProjectEntity> filteredList) {
        this.projectList = filteredList;
        notifyDataSetChanged();
    }
}