package com.example.coursework;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ExpenseEntity;

public class ExpenseActivity extends AppCompatActivity {

    private int projectId = -1;
    private String projectName = "";

    private TextView tvProjectName;
    private EditText etDate, etType, etAmount, etCurrency, etPaymentMethod, etTime, etComment, etClaimant, etPaymentStatus;
    private Button btnSaveExpense;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        db = AppDatabase.getInstance(this);

        // Nhận dữ liệu dự án từ Intent được truyền qua từ ProjectAdapter
        if (getIntent().hasExtra("PROJECT_ID")) {
            projectId = getIntent().getIntExtra("PROJECT_ID", -1);
            projectName = getIntent().getStringExtra("PROJECT_NAME");
        }

        // Ánh xạ giao diện
        tvProjectName = findViewById(R.id.tvProjectName);
        etDate = findViewById(R.id.etExpenseDate);
        etType = findViewById(R.id.etExpenseType);
        etAmount = findViewById(R.id.etExpenseAmount);
        etCurrency = findViewById(R.id.etExpenseCurrency);
        etPaymentMethod = findViewById(R.id.etPaymentMethod);
        etTime = findViewById(R.id.etExpenseTime);
        etComment = findViewById(R.id.etExpenseComment);
        etClaimant = findViewById(R.id.etExpenseClaimant);
        etPaymentStatus = findViewById(R.id.etPaymentStatus);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);

        tvProjectName.setText("Add Expense for: " + projectName);

        btnSaveExpense.setOnClickListener(v -> saveExpense());
    }

    private void saveExpense() {
        String date = etDate.getText().toString().trim();
        String type = etType.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String currency = etCurrency.getText().toString().trim();
        String paymentMethod = etPaymentMethod.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String comment = etComment.getText().toString().trim();
        String claimant = etClaimant.getText().toString().trim();
        String paymentStatus = etPaymentStatus.getText().toString().trim();

        // Kiểm tra các trường bắt buộc
        if (type.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill required fields (Date, Type, Amount)", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            // Xác nhận trước khi lưu (Confirmation)
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Expense")
                    .setMessage("Add " + type + " expense of $" + amount + "?")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        new Thread(() -> {
                            // GỌI ĐÚNG CONSTRUCTOR 10 THAM SỐ ĐỂ HẾT LỖI
                            ExpenseEntity expense = new ExpenseEntity(
                                    projectId, date, type, amount, currency, paymentMethod, time, comment, claimant, paymentStatus
                            );
                            db.appDao().insertExpense(expense);

                            runOnUiThread(() -> {
                                Toast.makeText(ExpenseActivity.this, "Expense Added Successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // Đóng Activity và quay lại màn hình trước
                            });
                        }).start();
                    })
                    .setNegativeButton("Edit", null)
                    .show();

        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount format");
        }
    }
}