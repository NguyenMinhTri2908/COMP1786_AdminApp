package com.example.coursework;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coursework.adapter.ProjectAdapter;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ProjectEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private EditText etSearch;
    private FloatingActionButton fabCreateNew;
    private Button btnSyncCloud; // Nút đồng bộ mới
    private RecyclerView recyclerView;

    private AppDatabase db;
    private ProjectAdapter adapter;
    private List<ProjectEntity> allProjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = AppDatabase.getInstance(this);

        etSearch = findViewById(R.id.etSearch);
        fabCreateNew = findViewById(R.id.fabCreateNew);
        btnSyncCloud = findViewById(R.id.btnSyncCloud); // Ánh xạ nút
        recyclerView = findViewById(R.id.recyclerViewHome);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Nút dấu + mở form thêm Dự án (MainActivity)
        fabCreateNew.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Xử lý sự kiện nhấn nút Đồng bộ Firebase
        btnSyncCloud.setOnClickListener(v -> syncToCloud());

        // Xử lý sự kiện Tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProjects(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }

    private void loadProjects() {
        new Thread(() -> {
            allProjects = db.appDao().getAllProjects();

            runOnUiThread(() -> {
                adapter = new ProjectAdapter(allProjects);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void filterProjects(String text) {
        if (allProjects == null || adapter == null) return;

        List<ProjectEntity> filteredList = new ArrayList<>();
        for (ProjectEntity project : allProjects) {
            if (project.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(project);
            }
        }
        adapter.updateList(filteredList);
    }

    // --- HÀM MỚI: ĐẨY DỮ LIỆU LÊN FIREBASE CLOUD ---
    private void syncToCloud() {
        if (allProjects == null || allProjects.isEmpty()) {
            Toast.makeText(this, "No projects to sync!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kết nối tới Database trên Firebase, tạo bảng tên là "projects"
        DatabaseReference database = FirebaseDatabase.getInstance("https://courseworkcloud-af10d-default-rtdb.firebaseio.com/").getReference("projects");
        // Đẩy toàn bộ danh sách dự án lên Cloud
        database.setValue(allProjects)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HomeActivity.this, "Synced to Cloud Successfully! ☁️", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Sync Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}