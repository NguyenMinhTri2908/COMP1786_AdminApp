package com.example.coursework;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coursework.adapter.ProjectAdapter;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ProjectEntity;
import java.util.List;

public class ProjectListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        recyclerView = findViewById(R.id.recyclerViewProjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getInstance(this);

        loadProjects();
    }

    private void loadProjects() {
        // Tải dữ liệu từ Room DB trên Background Thread
        new Thread(() -> {
            List<ProjectEntity> projects = db.appDao().getAllProjects();

            // Đẩy dữ liệu ra màn hình trên Main Thread
            runOnUiThread(() -> {
                ProjectAdapter adapter = new ProjectAdapter(projects);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}