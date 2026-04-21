package com.example.coursework;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coursework.adapter.ExpenseAdapter;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ExpenseEntity;
import java.util.Calendar;
import java.util.List;

public class ExpenseActivity extends AppCompatActivity {

    private int projectId = -1;
    private String projectName = "";
    private TextView tvProjectName;
    private EditText etExpenseCode, etDate, etAmount, etCurrency, etDescription, etLocation, etClaimant;
    private Spinner spType, spPaymentMethod, spPaymentStatus;
    private Button btnSaveExpense;
    private AppDatabase db;

    // --- BỔ SUNG BIẾN CHO CHỨC NĂNG XEM VÀ SỬA ---
    private int currentExpenseId = -1; // -1: Thêm mới, khác -1: Chế độ Sửa
    private RecyclerView rvExpenses;
    private ExpenseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        db = AppDatabase.getInstance(this);

        if (getIntent().hasExtra("PROJECT_ID")) {
            projectId = getIntent().getIntExtra("PROJECT_ID", -1);
            projectName = getIntent().getStringExtra("PROJECT_NAME");
        }

        // 1. Ánh xạ toàn bộ trường chuẩn
        tvProjectName = findViewById(R.id.tvProjectName);
        etExpenseCode = findViewById(R.id.etExpenseCode);
        etDate = findViewById(R.id.etExpenseDate);
        spType = findViewById(R.id.spExpenseType);
        etAmount = findViewById(R.id.etExpenseAmount);
        etCurrency = findViewById(R.id.etExpenseCurrency);
        etDescription = findViewById(R.id.etExpenseDescription);
        etLocation = findViewById(R.id.etExpenseLocation);
        spPaymentMethod = findViewById(R.id.spPaymentMethod);
        etClaimant = findViewById(R.id.etExpenseClaimant);
        spPaymentStatus = findViewById(R.id.spPaymentStatus);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);

        // Ánh xạ RecyclerView (Chức năng Xem)
        rvExpenses = findViewById(R.id.rvExpenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));

        tvProjectName.setText("Project: " + projectName);

        // 2. Thiết lập Spinners
        setupSpinners();

        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> showDatePicker());

        // --- KIỂM TRA CHẾ ĐỘ SỬA ---
        if (getIntent().hasExtra("EXPENSE_ID")) {
            currentExpenseId = getIntent().getIntExtra("EXPENSE_ID", -1);
            loadExpenseDataForEdit(currentExpenseId);
            btnSaveExpense.setText("Update Expense");
        }

        // --- LOAD DANH SÁCH CHI PHÍ (Xem) ---
        loadExpensesList();

        btnSaveExpense.setOnClickListener(v -> handleSaveOrUpdate());
    }

    private void setupSpinners() {
        setSpinnerAdapter(spType, R.array.expense_types);
        setSpinnerAdapter(spPaymentMethod, R.array.payment_methods);
        setSpinnerAdapter(spPaymentStatus, R.array.payment_statuses);
    }

    private void setSpinnerAdapter(Spinner spinner, int arrayRes) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, arrayRes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etDate.setText(String.format("%02d/%02d/%d", day, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // --- HÀM LOAD DANH SÁCH CHI PHÍ ---
    private void loadExpensesList() {
        new Thread(() -> {
            List<ExpenseEntity> list = db.appDao().getExpensesForProject(projectId);
            runOnUiThread(() -> {
                adapter = new ExpenseAdapter(list, projectName);
                rvExpenses.setAdapter(adapter);
            });
        }).start();
    }

    // --- HÀM ĐIỀN DỮ LIỆU CŨ ĐỂ SỬA ---
    private void loadExpenseDataForEdit(int expenseId) {
        new Thread(() -> {
            // Tìm chi phí trong danh sách của project này
            List<ExpenseEntity> list = db.appDao().getExpensesForProject(projectId);
            for (ExpenseEntity e : list) {
                if (e.getExpenseId() == expenseId) {
                    runOnUiThread(() -> {
                        etExpenseCode.setText(e.getExpenseCode());
                        etDate.setText(e.getDate());
                        etAmount.setText(String.valueOf(e.getAmount()));
                        etCurrency.setText(e.getCurrency());
                        etDescription.setText(e.getDescription());
                        etLocation.setText(e.getLocation());
                        etClaimant.setText(e.getClaimant());
                        setSpinnerSelection(spType, e.getType(), R.array.expense_types);
                        setSpinnerSelection(spPaymentMethod, e.getPaymentMethod(), R.array.payment_methods);
                        setSpinnerSelection(spPaymentStatus, e.getPaymentStatus(), R.array.payment_statuses);
                    });
                    break;
                }
            }
        }).start();
    }

    private void setSpinnerSelection(Spinner spinner, String value, int arrayRes) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        if (adapter != null) {
            int pos = adapter.getPosition(value);
            if (pos >= 0) spinner.setSelection(pos);
        }
    }

    // --- HÀM XỬ LÝ LƯU HOẶC CẬP NHẬT ---
    private void handleSaveOrUpdate() {
        String code = etExpenseCode.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String currency = etCurrency.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (code.isEmpty() || date.isEmpty() || amountStr.isEmpty() || currency.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String type = spType.getSelectedItem().toString();
            String method = spPaymentMethod.getSelectedItem().toString();
            String status = spPaymentStatus.getSelectedItem().toString();
            String location = etLocation.getText().toString().trim();
            String claimant = etClaimant.getText().toString().trim();

            new AlertDialog.Builder(this)
                    .setTitle("Review Details")
                    .setMessage("Expense: " + code + "\nAmount: " + amount + " " + currency)
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        new Thread(() -> {
                            ExpenseEntity expense = new ExpenseEntity(
                                    projectId, code, date, type, amount, currency,
                                    method, description, location, claimant, status
                            );

                            if (currentExpenseId != -1) {
                                // CHẾ ĐỘ SỬA
                                expense.setExpenseId(currentExpenseId);
                                db.appDao().updateExpense(expense);
                            } else {
                                // CHẾ ĐỘ THÊM MỚI
                                db.appDao().insertExpense(expense);
                            }

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                                if (currentExpenseId != -1) {
                                    finish(); // Sửa xong thì quay về
                                } else {
                                    // Thêm xong thì dọn form và load lại danh sách bên dưới
                                    clearForm();
                                    loadExpensesList();
                                }
                            });
                        }).start();
                    })
                    .setNegativeButton("Back", null)
                    .show();

        } catch (NumberFormatException e) {
            etAmount.setError("Invalid number");
        }
    }

    private void clearForm() {
        etExpenseCode.setText("");
        etAmount.setText("");
        etDescription.setText("");
        etLocation.setText("");
        etClaimant.setText("");
        // Giữ lại date và currency cho tiện nhập tiếp
    }
}