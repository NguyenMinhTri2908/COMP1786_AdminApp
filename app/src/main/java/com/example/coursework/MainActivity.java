package com.example.coursework;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ProjectEntity;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    private EditText etProjectName, etDestination, etDate, etBudget, etDescription;
    private SwitchMaterial switchRisk;
    private Button btnSaveProject, btnViewProjects;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Database
        db = AppDatabase.getInstance(this);

        // Ánh xạ giao diện
        etProjectName = findViewById(R.id.etProjectName);
        etDestination = findViewById(R.id.etDestination);
        etDate = findViewById(R.id.etDate);
        etBudget = findViewById(R.id.etBudget);
        etDescription = findViewById(R.id.etDescription);
        switchRisk = findViewById(R.id.switchRisk);
        btnSaveProject = findViewById(R.id.btnSaveProject);
        btnViewProjects = findViewById(R.id.btnViewProjects);

        // Bấm nút Save
        btnSaveProject.setOnClickListener(v -> saveProjectToDatabase());

        // Bấm nút View (Sẽ bị báo đỏ ProjectListActivity.class vì mình chưa tạo ở Bước 10)
        btnViewProjects.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProjectListActivity.class);
            startActivity(intent);
        });
    }

    private void saveProjectToDatabase() {
        String name = etProjectName.getText().toString().trim();
        String destination = etDestination.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String budgetStr = etBudget.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean requiresRisk = switchRisk.isChecked();

        // Validation (Bắt buộc theo yêu cầu Coursework)
        if (name.isEmpty() || destination.isEmpty() || date.isEmpty() || budgetStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required (*) fields", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            double budget = Double.parseDouble(budgetStr);

            // Lưu vào Database (chạy trên Background Thread)
            new Thread(() -> {
                ProjectEntity newProject = new ProjectEntity(name, destination, date, requiresRisk, description, budget);
                long insertedId = db.appDao().insertProject(newProject);

                runOnUiThread(() -> {
                    if (insertedId > 0) {
                        Toast.makeText(MainActivity.this, "Project Saved Successfully!", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to save project.", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();

        } catch (NumberFormatException e) {
            etBudget.setError("Invalid budget format");
        }
    }

    private void clearInputs() {
        etProjectName.setText("");
        etDestination.setText("");
        etDate.setText("");
        etBudget.setText("");
        etDescription.setText("");
        switchRisk.setChecked(false);
        etProjectName.requestFocus();
    }
}