package com.example.coursework;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ProjectEntity;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.Calendar; // Import thư viện lịch

public class MainActivity extends AppCompatActivity {

    private EditText etProjectIdCode, etProjectName, etDestination, etStartDate, etEndDate;
    private EditText etBudget, etDescription, etOwner, etSpecial, etClient;
    private Spinner spStatus;
    private SwitchMaterial switchRisk;
    private Button btnSaveProject, btnViewProjects;
    private AppDatabase db;
    private int currentProjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        // 1. Ánh xạ toàn bộ 11 ô nhập liệu
        etProjectIdCode = findViewById(R.id.etProjectIdCode);
        etProjectName = findViewById(R.id.etProjectName);
        etDestination = findViewById(R.id.etDestination);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etBudget = findViewById(R.id.etBudget);
        etDescription = findViewById(R.id.etDescription);
        etOwner = findViewById(R.id.etOwner);

        spStatus = findViewById(R.id.spStatus);
        // Tạo danh sách chọn cho Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.project_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(adapter);
        etSpecial = findViewById(R.id.etSpecialRequirements);
        etClient = findViewById(R.id.etClientInfo);

        switchRisk = findViewById(R.id.switchRisk);
        btnSaveProject = findViewById(R.id.btnSaveProject);
        btnViewProjects = findViewById(R.id.btnViewProjects);

        // 2. Thiết lập chọn ngày
        etStartDate.setFocusable(false); // Không cho hiện bàn phím
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));

        etEndDate.setFocusable(false);   // Không cho hiện bàn phím
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        // 3. Load dữ liệu Edit
        if (getIntent().hasExtra("PROJECT_ID")) {
            currentProjectId = getIntent().getIntExtra("PROJECT_ID", -1);
            etProjectIdCode.setText(getIntent().getStringExtra("PROJECT_CODE"));
            etProjectName.setText(getIntent().getStringExtra("PROJECT_NAME"));
            etDestination.setText(getIntent().getStringExtra("PROJECT_DESTINATION"));
            etStartDate.setText(getIntent().getStringExtra("PROJECT_START"));
            etEndDate.setText(getIntent().getStringExtra("PROJECT_END"));
            etBudget.setText(String.valueOf(getIntent().getDoubleExtra("PROJECT_BUDGET", 0)));
            etOwner.setText(getIntent().getStringExtra("PROJECT_OWNER"));
            setSpinnerValue(spStatus, getIntent().getStringExtra("PROJECT_STATUS"));
            etDescription.setText(getIntent().getStringExtra("PROJECT_DESC"));
            etSpecial.setText(getIntent().getStringExtra("PROJECT_SPECIAL"));
            etClient.setText(getIntent().getStringExtra("PROJECT_CLIENT"));
            switchRisk.setChecked(getIntent().getBooleanExtra("PROJECT_RISK", false));

            btnSaveProject.setText("Update Project");
        }

        btnSaveProject.setOnClickListener(v -> handleSaveOrUpdate());
        btnViewProjects.setOnClickListener(v -> finish());
    }

    // Hàm hiển thị cuốn lịch
    private void showDatePickerDialog(final EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthSelected, dayOfMonthSelected) -> {
                    // Định dạng ngày thành dd/mm/yyyy
                    String date = String.format("%02d/%02d/%d", dayOfMonthSelected, monthSelected + 1, yearSelected);
                    dateField.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void handleSaveOrUpdate() {
        // Lấy dữ liệu từ các ô nhập
        String idCode = etProjectIdCode.getText().toString().trim();
        String name = etProjectName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String start = etStartDate.getText().toString().trim();
        String end = etEndDate.getText().toString().trim();
        String owner = etOwner.getText().toString().trim();
        String status = spStatus.getSelectedItem().toString();
        String budgetStr = etBudget.getText().toString().trim();

        // Các trường tùy chọn (Optional)
        String dest = etDestination.getText().toString().trim();
        String special = etSpecial.getText().toString().trim();
        String client = etClient.getText().toString().trim();

        // VALIDATION -

        if (idCode.isEmpty()) {
            etProjectIdCode.setError("Project ID/Code is required!");
            etProjectIdCode.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            etProjectName.setError("Project Name is required!");
            etProjectName.requestFocus();
            return;
        }

        if (desc.isEmpty()) {
            etDescription.setError("Project Description is required!");
            etDescription.requestFocus();
            return;
        }

        if (start.isEmpty()) {
            Toast.makeText(this, "Please select a Start Date!", Toast.LENGTH_SHORT).show();
            etStartDate.performClick(); // Tự động mở lịch nếu quên chọn
            return;
        }

        if (end.isEmpty()) {
            Toast.makeText(this, "Please select an End Date!", Toast.LENGTH_SHORT).show();
            etEndDate.performClick();
            return;
        }

        if (owner.isEmpty()) {
            etOwner.setError("Project Manager/Owner is required!");
            etOwner.requestFocus();
            return;
        }


        if (budgetStr.isEmpty()) {
            etBudget.setError("Project Budget is required!");
            etBudget.requestFocus();
            return;
        }

        // --- KẾT THÚC KIỂM TRA ---

        try {
            double budget = Double.parseDouble(budgetStr);

            // CONFIRMATION - PART A
            // Hiển thị  thông tin để  Review trước  lưu
            StringBuilder confirmMessage = new StringBuilder();
            confirmMessage.append("ID/Code: ").append(idCode).append("\n");
            confirmMessage.append("Name: ").append(name).append("\n");
            confirmMessage.append("Destination: ").append(dest).append("\n");
            confirmMessage.append("Description: ").append(desc).append("\n");
            confirmMessage.append("Duration: ").append(start).append(" - ").append(end).append("\n");
            confirmMessage.append("Manager: ").append(owner).append("\n");
            confirmMessage.append("Status: ").append(status).append("\n");
            confirmMessage.append("Budget: $").append(budget).append("\n");
            confirmMessage.append("Special Req: ").append(special.isEmpty() ? "None" : special).append("\n");
            confirmMessage.append("Client Info: ").append(client.isEmpty() ? "None" : client);

            new AlertDialog.Builder(this)
                    .setTitle("Review & Confirm Details")
                    .setMessage(confirmMessage.toString())
                    .setPositiveButton("Confirm & Save", (dialog, which) -> {
                        saveToDatabase(idCode, name, dest, start, end, switchRisk.isChecked(),
                                desc, budget, owner, status, special, client);
                    })
                    .setNegativeButton("Go Back to Edit", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();

        } catch (NumberFormatException e) {
            etBudget.setError("Invalid budget format (Example: 1500.50)");
            etBudget.requestFocus();
        }
    }

    private void saveToDatabase(String idCode, String name, String dest, String start, String end,
                                boolean risk, String desc, double budget, String owner, String status,
                                String special, String client) {
        new Thread(() -> {
            ProjectEntity project = new ProjectEntity(idCode, name, dest, start, end, risk, desc, budget, owner, status, special, client);

            if (currentProjectId != -1) {
                project.setId(currentProjectId);
                db.appDao().updateProject(project);
            } else {
                db.appDao().insertProject(project);
            }

            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this,
                        "Lưu thành công! Đừng quên bấm 'Sync Cloud' để cập nhật lên đám mây.",
                        Toast.LENGTH_LONG).show();
                finish();
            });
        }).start();

    }
    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        if (adapter != null && value != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }
}