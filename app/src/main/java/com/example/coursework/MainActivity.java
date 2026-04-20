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

    private EditText etProjectIdCode, etProjectName, etDestination, etStartDate, etEndDate;
    private EditText etBudget, etDescription, etOwner, etStatus, etSpecial, etClient;
    private SwitchMaterial switchRisk;
    private Button btnSaveProject, btnViewProjects;
    private AppDatabase db;
    private int currentProjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        // Ánh xạ toàn bộ 11 ô nhập liệu
        etProjectIdCode = findViewById(R.id.etProjectIdCode);
        etProjectName = findViewById(R.id.etProjectName);
        etDestination = findViewById(R.id.etDestination);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etBudget = findViewById(R.id.etBudget);
        etDescription = findViewById(R.id.etDescription);
        etOwner = findViewById(R.id.etOwner);
        etStatus = findViewById(R.id.etStatus);
        etSpecial = findViewById(R.id.etSpecialRequirements); // Mới
        etClient = findViewById(R.id.etClientInfo);           // Mới

        switchRisk = findViewById(R.id.switchRisk);
        btnSaveProject = findViewById(R.id.btnSaveProject);
        btnViewProjects = findViewById(R.id.btnViewProjects);

        // Load dữ liệu nếu là chế độ Sửa (Edit)
        if (getIntent().hasExtra("PROJECT_ID")) {
            currentProjectId = getIntent().getIntExtra("PROJECT_ID", -1);
            etProjectIdCode.setText(getIntent().getStringExtra("PROJECT_CODE"));
            etProjectName.setText(getIntent().getStringExtra("PROJECT_NAME"));
            etDestination.setText(getIntent().getStringExtra("PROJECT_DESTINATION"));
            etStartDate.setText(getIntent().getStringExtra("PROJECT_START"));
            etEndDate.setText(getIntent().getStringExtra("PROJECT_END"));
            etBudget.setText(String.valueOf(getIntent().getDoubleExtra("PROJECT_BUDGET", 0)));
            etOwner.setText(getIntent().getStringExtra("PROJECT_OWNER"));
            etStatus.setText(getIntent().getStringExtra("PROJECT_STATUS"));
            etDescription.setText(getIntent().getStringExtra("PROJECT_DESC"));
            etSpecial.setText(getIntent().getStringExtra("PROJECT_SPECIAL"));
            etClient.setText(getIntent().getStringExtra("PROJECT_CLIENT"));
            switchRisk.setChecked(getIntent().getBooleanExtra("PROJECT_RISK", false));

            btnSaveProject.setText("Update Project");
        }

        btnSaveProject.setOnClickListener(v -> handleSaveOrUpdate());
        btnViewProjects.setOnClickListener(v -> finish());
    }

    private void handleSaveOrUpdate() {
        String idCode = etProjectIdCode.getText().toString().trim();
        String name = etProjectName.getText().toString().trim();
        String dest = etDestination.getText().toString().trim();
        String budgetStr = etBudget.getText().toString().trim();

        if (name.isEmpty() || dest.isEmpty() || budgetStr.isEmpty()) {
            Toast.makeText(this, "Please fill in required (*) fields", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            double budget = Double.parseDouble(budgetStr);

            // Lấy thêm dữ liệu từ 2 ô mới
            String special = etSpecial.getText().toString().trim();
            String client = etClient.getText().toString().trim();

            // DIALOG XÁC NHẬN CHI TIẾT (Yêu cầu Phần a)
            String confirmMessage = "Name: " + name + "\nBudget: $" + budget +
                    "\nSpecial Req: " + (special.isEmpty() ? "None" : special);

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Project Details")
                    .setMessage(confirmMessage)
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        saveToDatabase(idCode, name, dest, etStartDate.getText().toString(),
                                etEndDate.getText().toString(), switchRisk.isChecked(),
                                etDescription.getText().toString(), budget,
                                etOwner.getText().toString(), etStatus.getText().toString(),
                                special, client);
                    })
                    .setNegativeButton("Edit", null)
                    .show();

        } catch (NumberFormatException e) {
            etBudget.setError("Invalid budget format");
        }
    }

    private void saveToDatabase(String idCode, String name, String dest, String start, String end,
                                boolean risk, String desc, double budget, String owner, String status,
                                String special, String client) {
        new Thread(() -> {
            // Truyền đủ 12 tham số vào ProjectEntity
            ProjectEntity project = new ProjectEntity(idCode, name, dest, start, end, risk, desc, budget, owner, status, special, client);

            if (currentProjectId != -1) {
                project.setId(currentProjectId);
                db.appDao().updateProject(project);
            } else {
                db.appDao().insertProject(project);
            }

            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}