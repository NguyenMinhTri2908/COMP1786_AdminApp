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
            // Hiện thông báo yêu cầu kiểm tra mạng (Yêu cầu Part e)
            new AlertDialog.Builder(this).setTitle("Connection error").setMessage("Please turn on Wifi/3G.").show();
            return;
        }

        DatabaseReference database = FirebaseDatabase.getInstance("https://courseworkcloud-af10d-default-rtdb.firebaseio.com/")
                .getReference("projects");

        // BƯỚC 1: XÓA SẠCH CLOUD TRƯỚC
        database.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // BƯỚC 2: NẾU MÁY CÓ DỮ LIỆU THÌ ĐẨY LÊN
                if (allProjects != null && !allProjects.isEmpty()) {
                    database.setValue(allProjects);
                }
                Toast.makeText(this, "Fully synchronized !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // HÀM THỰC THI RESET DATABASE
    private void executeResetDatabase() {
        new Thread(() -> {
            // 1. Xóa sạch SQLite
            db.clearAllTables();
            if (allProjects != null) allProjects.clear();

            runOnUiThread(() -> {
                if (adapter != null) adapter.updateList(new ArrayList<>());

                // 2. THAY ĐỔI THÔNG BÁO Ở ĐÂY
                Toast.makeText(HomeActivity.this,
                        "The device has been clean. Please click 'Sync Cloud' to wipe all data on the Cloud!",
                        Toast.LENGTH_LONG).show();
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