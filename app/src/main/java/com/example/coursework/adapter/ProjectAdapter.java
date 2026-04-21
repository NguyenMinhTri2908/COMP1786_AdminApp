package com.example.coursework.adapter;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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
        holder.tvDate.setText("Start Date: " + project.getStartDate());
        holder.tvBudget.setText("Budget: $" + project.getBudget());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), com.example.coursework.ExpenseActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            intent.putExtra("PROJECT_NAME", project.getName());
            v.getContext().startActivity(intent);
        });

        // 1. GIỮ NGUYÊN LOGIC EDIT: Như bạn yêu cầu (đã ổn định)
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            intent.putExtra("PROJECT_CODE", project.getProjectIdCode());
            intent.putExtra("PROJECT_NAME", project.getName());
            intent.putExtra("PROJECT_DESTINATION", project.getDestination());
            intent.putExtra("PROJECT_START", project.getStartDate());
            intent.putExtra("PROJECT_END", project.getEndDate());
            intent.putExtra("PROJECT_BUDGET", project.getBudget());
            intent.putExtra("PROJECT_OWNER", project.getOwner());
            intent.putExtra("PROJECT_STATUS", project.getStatus());
            intent.putExtra("PROJECT_DESC", project.getDescription());
            intent.putExtra("PROJECT_SPECIAL", project.getSpecialRequirements());
            intent.putExtra("PROJECT_CLIENT", project.getClientInfo());
            intent.putExtra("PROJECT_RISK", project.isRequiresRiskAssessment());
            v.getContext().startActivity(intent);
        });

        // 2. TỐI ƯU LOGIC DELETE: Chống văng app tuyệt đối
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Project")
                    .setMessage("Are you sure you want to delete this project?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Lấy vị trí ngay tại thời điểm bấm nút
                        int currentPos = holder.getBindingAdapterPosition();

                        // Kiểm tra an toàn: nếu item không còn tồn tại trong list thì thoát
                        if (currentPos == RecyclerView.NO_POSITION || currentPos >= projectList.size()) return;

                        new Thread(() -> {
                            // Xóa trong Database
                            AppDatabase.getInstance(v.getContext()).appDao().deleteProject(project);

                            // Cập nhật UI bằng Handler (An toàn hơn runOnUiThread trong Adapter)
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (currentPos < projectList.size()) {
                                    projectList.remove(currentPos);
                                    notifyItemRemoved(currentPos);
                                    // Báo cho list biết các mục còn lại đã thay đổi vị trí
                                    notifyItemRangeChanged(currentPos, projectList.size());
                                    Toast.makeText(v.getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).start();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return projectList == null ? 0 : projectList.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDestination, tvDate, tvBudget;
        Button btnEdit, btnDelete;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDestination = itemView.findViewById(R.id.tvItemDestination);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvBudget = itemView.findViewById(R.id.tvItemBudget);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public void updateList(List<ProjectEntity> filteredList) {
        this.projectList = filteredList;
        notifyDataSetChanged();
    }
}