package com.example.coursework;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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
    private Button btnSyncCloud, btnResetDB; // Thêm nút Reset
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
        btnSyncCloud = findViewById(R.id.btnSyncCloud);
        btnResetDB = findViewById(R.id.btnResetDB); // Ánh xạ nút Reset
        recyclerView = findViewById(R.id.recyclerViewHome);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabCreateNew.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnSyncCloud.setOnClickListener(v -> syncToCloud());

        // XỬ LÝ NÚT RESET DATABASE
        btnResetDB.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa sạch")
                    .setMessage("Bạn có chắc muốn xóa TOÀN BỘ dữ liệu? Hành động này sẽ xóa sạch cả dự án và chi phí.")
                    .setPositiveButton("Xóa hết", (dialog, which) -> executeResetDatabase())
                    .setNegativeButton("Hủy", null)
                    .show();
        });

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
        String query = text.toLowerCase().trim();
        for (ProjectEntity project : allProjects) {
            if (project.getName().toLowerCase().contains(query) ||
                    (project.getDescription() != null && project.getDescription().toLowerCase().contains(query))) {
                filteredList.add(project);
            }
        }
        adapter.updateList(filteredList);
    }

    private void syncToCloud() {
        if (!isNetworkAvailable()) {
            new AlertDialog.Builder(this)
                    .setTitle("Không có kết nối")
                    .setMessage("Vui lòng kiểm tra Wifi/3G trước khi đồng bộ lên Cloud.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        if (allProjects == null || allProjects.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu để đồng bộ!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference database = FirebaseDatabase.getInstance("https://courseworkcloud-af10d-default-rtdb.firebaseio.com/").getReference("projects");
        database.setValue(allProjects)
                .addOnSuccessListener(aVoid -> Toast.makeText(HomeActivity.this, "Đã đồng bộ lên Cloud! ☁️", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Lỗi đồng bộ: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // HÀM THỰC THI RESET DATABASE
    private void executeResetDatabase() {
        new Thread(() -> {
            db.clearAllTables(); // Xóa sạch dữ liệu trong SQLite
            if (allProjects != null) allProjects.clear();
            runOnUiThread(() -> {
                if (adapter != null) adapter.updateList(new ArrayList<>());
                Toast.makeText(HomeActivity.this, "Đã Reset Database thành công!", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}