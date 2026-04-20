package com.example.coursework;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ProjectEntity;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    // Đã thêm các trường mới vào khai báo
    private EditText etProjectIdCode, etProjectName, etDestination;
    private EditText etStartDate, etEndDate, etBudget, etDescription;
    private EditText etOwner, etStatus;
    private SwitchMaterial switchRisk;
    private Button btnSaveProject, btnViewProjects;
    private AppDatabase db;

    // Biến để lưu ID nếu đang ở chế độ Sửa dự án
    private int currentProjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        // Ánh xạ giao diện (BẠN CẦN TẠO CÁC ID NÀY TRONG activity_main.xml)
        etProjectIdCode = findViewById(R.id.etProjectIdCode);
        etProjectName = findViewById(R.id.etProjectName);
        etDestination = findViewById(R.id.etDestination);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etBudget = findViewById(R.id.etBudget);
        etDescription = findViewById(R.id.etDescription);
        etOwner = findViewById(R.id.etOwner);
        etStatus = findViewById(R.id.etStatus);
        switchRisk = findViewById(R.id.switchRisk);
        btnSaveProject = findViewById(R.id.btnSaveProject);
        btnViewProjects = findViewById(R.id.btnViewProjects);

        // Kiểm tra xem có phải đang ở chế độ CHỈNH SỬA không
        if (getIntent().hasExtra("PROJECT_ID")) {
            currentProjectId = getIntent().getIntExtra("PROJECT_ID", -1);
            etProjectName.setText(getIntent().getStringExtra("PROJECT_NAME"));
            etDestination.setText(getIntent().getStringExtra("PROJECT_DESTINATION"));
            etBudget.setText(String.valueOf(getIntent().getDoubleExtra("PROJECT_BUDGET", 0)));
            // Điền tiếp các trường khác nếu cần thiết ở đây...

            btnSaveProject.setText("Update Project");
        }

        btnSaveProject.setOnClickListener(v -> handleSaveOrUpdate());

        btnViewProjects.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void handleSaveOrUpdate() {
        String idCode = etProjectIdCode != null ? etProjectIdCode.getText().toString().trim() : "";
        String name = etProjectName.getText().toString().trim();
        String destination = etDestination.getText().toString().trim();
        String startDate = etStartDate != null ? etStartDate.getText().toString().trim() : "";
        String endDate = etEndDate != null ? etEndDate.getText().toString().trim() : "";
        String budgetStr = etBudget.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String owner = etOwner != null ? etOwner.getText().toString().trim() : "";
        String status = etStatus != null ? etStatus.getText().toString().trim() : "Active";
        boolean requiresRisk = switchRisk.isChecked();

        // Validation bắt buộc
        if (name.isEmpty() || destination.isEmpty() || budgetStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required (*) fields", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            double budget = Double.parseDouble(budgetStr);

            // TẠO DIALOG XÁC NHẬN (CONFIRMATION)
            String confirmMessage = "Name: " + name + "\nDestination: " + destination + "\nBudget: $" + budget;

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Project Details")
                    .setMessage(confirmMessage)
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        // Nếu bấm Confirm thì lưu vào Database
                        saveToDatabase(idCode, name, destination, startDate, endDate, requiresRisk, description, budget, owner, status);
                    })
                    .setNegativeButton("Edit", (dialog, which) -> {
                        // Bấm Edit thì đóng Dialog để sửa tiếp
                        dialog.dismiss();
                    })
                    .show();

        } catch (NumberFormatException e) {
            etBudget.setError("Invalid budget format");
        }
    }

    private void saveToDatabase(String idCode, String name, String dest, String start, String end, boolean risk, String desc, double budget, String owner, String status) {
        new Thread(() -> {
            ProjectEntity project = new ProjectEntity(idCode, name, dest, start, end, risk, desc, budget, owner, status);

            if (currentProjectId == -1) {
                // Thêm mới
                long insertedId = db.appDao().insertProject(project);
                runOnUiThread(() -> {
                    if (insertedId > 0) {
                        Toast.makeText(MainActivity.this, "Project Saved Successfully!", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    }
                });
            } else {
                // Cập nhật (Sửa)
                project.setId(currentProjectId);
                db.appDao().updateProject(project);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Project Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng Activity và quay lại màn hình danh sách
                });
            }
        }).start();
    }

    private void clearInputs() {
        if(etProjectIdCode != null) etProjectIdCode.setText("");
        etProjectName.setText("");
        etDestination.setText("");
        if(etStartDate != null) etStartDate.setText("");
        if(etEndDate != null) etEndDate.setText("");
        etBudget.setText("");
        etDescription.setText("");
        if(etOwner != null) etOwner.setText("");
        if(etStatus != null) etStatus.setText("");
        switchRisk.setChecked(false);
        etProjectName.requestFocus();
    }
}