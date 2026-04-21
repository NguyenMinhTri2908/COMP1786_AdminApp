package com.example.coursework;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ExpenseEntity;
import java.util.Calendar;

public class ExpenseActivity extends AppCompatActivity {

    private int projectId = -1;
    private String projectName = "";
    private TextView tvProjectName;
    private EditText etExpenseCode, etDate, etAmount, etCurrency, etDescription, etLocation, etClaimant;
    private Spinner spType, spPaymentMethod, spPaymentStatus;
    private Button btnSaveExpense;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        db = AppDatabase.getInstance(this);

        if (getIntent().hasExtra("PROJECT_ID")) {
            projectId = getIntent().getIntExtra("PROJECT_ID", -1);
            projectName = getIntent().getStringExtra("PROJECT_NAME");
        }

        // 1. Ánh xạ toàn bộ trường
        tvProjectName = findViewById(R.id.tvProjectName);
        etExpenseCode = findViewById(R.id.etExpenseCode); // Mã chi phí
        etDate = findViewById(R.id.etExpenseDate);
        spType = findViewById(R.id.spExpenseType);
        etAmount = findViewById(R.id.etExpenseAmount);
        etCurrency = findViewById(R.id.etExpenseCurrency);
        etDescription = findViewById(R.id.etExpenseDescription); // Mô tả
        etLocation = findViewById(R.id.etExpenseLocation); // Địa điểm
        spPaymentMethod = findViewById(R.id.spPaymentMethod);
        etClaimant = findViewById(R.id.etExpenseClaimant);
        spPaymentStatus = findViewById(R.id.spPaymentStatus);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);

        tvProjectName.setText("Add Expense for: " + projectName);

        // 2. Thiết lập 3 Spinners
        setupSpinners();

        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> showDatePicker());

        btnSaveExpense.setOnClickListener(v -> saveExpense());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.expense_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> methodAdapter = ArrayAdapter.createFromResource(this, R.array.payment_methods, android.R.layout.simple_spinner_item);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPaymentMethod.setAdapter(methodAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.payment_statuses, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPaymentStatus.setAdapter(statusAdapter);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etDate.setText(String.format("%02d/%02d/%d", day, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }


    private void saveExpense() {
        // 5. Lấy dữ liệu từ các ô nhập liệu
        String code = etExpenseCode.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String currency = etCurrency.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        String type = spType.getSelectedItem().toString();
        String method = spPaymentMethod.getSelectedItem().toString();
        String status = spPaymentStatus.getSelectedItem().toString();
        String location = etLocation.getText().toString().trim();
        String claimant = etClaimant.getText().toString().trim();

        // 6. Kiểm tra các trường bắt buộc (Code, Date, Amount, Currency, Description)
        if (code.isEmpty()) { etExpenseCode.setError("Expense Code required!"); return; }
        if (date.isEmpty()) { Toast.makeText(this, "Please select Date!", Toast.LENGTH_SHORT).show(); return; }
        if (amountStr.isEmpty()) { etAmount.setError("Amount required!"); return; }
        if (currency.isEmpty()) { etCurrency.setError("Currency required!"); return; }
        if (description.isEmpty()) { etDescription.setError("Description required!"); return; }

        try {
            double amount = Double.parseDouble(amountStr);

            new AlertDialog.Builder(this)
                    .setTitle("Review Expense Details")
                    .setMessage("ID: " + code + "\nType: " + type + "\nAmount: " + amount + " " + currency)
                    .setPositiveButton("Confirm & Save", (dialog, which) -> {
                        new Thread(() -> {
                            // 7. Gọi ĐÚNG constructor 11 tham số mà bạn đã định nghĩa trong Entity
                            ExpenseEntity expense = new ExpenseEntity(
                                    projectId,
                                    code,
                                    date,
                                    type,
                                    amount,
                                    currency,
                                    method,
                                    description,
                                    location,
                                    claimant,
                                    status
                            );
                            db.appDao().insertExpense(expense);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Expense Saved Successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }).start();
                    })
                    .setNegativeButton("Edit", null)
                    .show();

        } catch (NumberFormatException e) {
            etAmount.setError("Invalid number format");
        }
    }
}