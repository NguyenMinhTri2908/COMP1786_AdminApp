package com.example.coursework;

import com.example.coursework.database.ExpenseEntity;
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

        // Ánh xạ View (Phải đúng ID đã đặt ở XML)
        etSearch = findViewById(R.id.etSearch);
        btnSyncCloud = findViewById(R.id.btnSyncCloud);
        btnResetDB = findViewById(R.id.btnResetDB);
        recyclerView = findViewById(R.id.recyclerViewHome);
        fabCreateNew = findViewById(R.id.fabCreateNew);

        // NÚT MỚI: Kích hoạt Advanced Search
        Button btnOpenAdvancedSearch = findViewById(R.id.btnOpenAdvancedSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khôi phục chức năng Search cơ bản
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Khi gõ vào ô search, gọi hàm lọc cũ của bạn
                filterProjects(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Gán sự kiện cho nút Filter (Nâng cao)
        btnOpenAdvancedSearch.setOnClickListener(v -> showAdvancedSearchDialog());

        // Các nút khác giữ nguyên
        fabCreateNew.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnSyncCloud.setOnClickListener(v -> syncToCloud());
        btnResetDB.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Reset all data?")
                    .setPositiveButton("Yes", (dialog, which) -> executeResetDatabase())
                    .setNegativeButton("No", null)
                    .show();
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
        // 1. Kiểm tra mạng
        if (!isNetworkAvailable()) {
            new AlertDialog.Builder(this)
                    .setTitle("Connection error")
                    .setMessage("Please turn on Wifi/3G to sync data.")
                    .show();
            return;
        }

        // 2. Hiện thông báo đang xử lý để người dùng chờ
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Syncing Projects & Expenses to Cloud...");
        progressDialog.show();

        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://courseworkcloud-af10d-default-rtdb.firebaseio.com/").getReference();

        new Thread(() -> {
            // Lấy dữ liệu từ SQLite
            List<ProjectEntity> projects = db.appDao().getAllProjects();
            List<ExpenseEntity> expenses = db.appDao().getAllExpenses(); // Lệnh mới thêm ở Bước 1

            // Đẩy lên Firebase vào 2 mục riêng biệt
            // Cách làm này tương tự như cách bạn đẩy Project, chỉ là thêm 1 dòng cho Expense
            dbRef.child("projects").setValue(projects);
            dbRef.child("expenses").setValue(expenses).addOnCompleteListener(task -> {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Full Cloud Sync Complete!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Sync failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }).start();
    }

    // HÀM THỰC THI RESET DATABASE
    private void executeResetDatabase() {
        new Thread(() -> {
            // 1. Xóa sạch SQLite
            db.clearAllTables();
            if (allProjects != null) allProjects.clear();

            runOnUiThread(() -> {
                if (adapter != null) adapter.updateList(new ArrayList<>());

                // 2. Xóa sạch cả Project và Expense trên Cloud để đồng bộ
                DatabaseReference dbRef = FirebaseDatabase.getInstance("https://courseworkcloud-af10d-default-rtdb.firebaseio.com/").getReference();
                dbRef.removeValue(); // Xóa sạch toàn bộ Node gốc

                Toast.makeText(HomeActivity.this, "Device and Cloud cleared successfully!", Toast.LENGTH_LONG).show();
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
    private void showAdvancedSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Advanced Search");

        // Tạo Layout chứa các thành phần
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 1. Nhập Owner
        final EditText inputOwner = new EditText(this);
        inputOwner.setHint("Owner Name");
        layout.addView(inputOwner);

        // 2. Chọn Ngày (Dùng DatePicker)
        final EditText inputDate = new EditText(this);
        inputDate.setHint("Select Date (Click to choose)");
        inputDate.setFocusable(false); // Không cho gõ, chỉ cho bấm
        inputDate.setOnClickListener(v -> showDatePicker(inputDate));
        layout.addView(inputDate);

        // 3. Tiêu đề nhỏ cho Spinner
        android.widget.TextView tvLabel = new android.widget.TextView(this);
        tvLabel.setText("Select Status:");
        tvLabel.setPadding(10, 20, 0, 5);
        layout.addView(tvLabel);

        // 4. Spinner chọn Status (Dropdown)
        final android.widget.Spinner spStatus = new android.widget.Spinner(this);
        // Sử dụng lại project_status_array từ strings.xml của bạn
        android.widget.ArrayAdapter<CharSequence> statusAdapter = android.widget.ArrayAdapter.createFromResource(this,
                R.array.project_status_array, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);        spStatus.setAdapter(statusAdapter);
        layout.addView(spStatus);

        builder.setView(layout);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String owner = inputOwner.getText().toString();
            String date = inputDate.getText().toString();
            // Lấy giá trị từ Spinner
            String status = spStatus.getSelectedItem().toString();

            // Gọi hàm lọc với 3 tiêu chí
            advancedFilter(etSearch.getText().toString(), status, date, owner);
        });

        builder.setNegativeButton("Clear Filter", (dialog, which) -> loadProjects());

        builder.show();
    }
    private void advancedFilter(String keyword, String status, String date, String owner) {
        if (allProjects == null || adapter == null) return;

        List<ProjectEntity> filteredList = new ArrayList<>();
        String query = keyword.toLowerCase().trim();

        for (ProjectEntity project : allProjects) {
            // 1. Kiểm tra Name hoặc Description
            boolean matchKeyword = project.getName().toLowerCase().contains(query) ||
                    (project.getDescription() != null && project.getDescription().toLowerCase().contains(query));

            // 2. Kiểm tra Status (nếu có chọn)
            boolean matchStatus = (status == null || status.isEmpty() ||
                    (project.getStatus() != null && project.getStatus().equalsIgnoreCase(status)));

            // 3. Kiểm tra Date (nếu có chọn)
            boolean matchDate = (date == null || date.isEmpty() ||
                    (project.getStartDate() != null && project.getStartDate().contains(date)));

            // 4. Kiểm tra Owner
            boolean matchOwner = (owner == null || owner.isEmpty() ||
                    (project.getOwner() != null && project.getOwner().toLowerCase().contains(owner.toLowerCase())));

            // Nếu tất cả thỏa mãn thì thêm vào danh sách
            if (matchKeyword && matchStatus && matchDate && matchOwner) {
                filteredList.add(project);
            }
        }
        adapter.updateList(filteredList);
    }
    private void showDatePicker(EditText editText) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Định dạng ngày thành DD/MM/YYYY (khớp với định dạng bạn lưu trong DB)
                    String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    editText.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

}