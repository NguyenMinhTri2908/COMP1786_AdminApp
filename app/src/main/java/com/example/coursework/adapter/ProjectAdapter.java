package com.example.coursework.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coursework.R;
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
        holder.tvDate.setText("Date: " + project.getDate());
        holder.tvBudget.setText("Budget: $" + project.getBudget());

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.coursework.ExpenseActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            intent.putExtra("PROJECT_NAME", project.getName());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDestination, tvDate, tvBudget;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDestination = itemView.findViewById(R.id.tvItemDestination);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvBudget = itemView.findViewById(R.id.tvItemBudget);
        }
    }
    public void updateList(List<ProjectEntity> filteredList) {
        this.projectList = filteredList;
        notifyDataSetChanged(); // Báo cho giao diện biết dữ liệu đã thay đổi để vẽ lại
    }
}
