package com.example.coursework;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private TextView tvProjectName, tvFormTitle;
    private EditText etExpenseCode, etDate, etAmount, etCurrency, etDescription, etLocation, etClaimant;
    private Spinner spType, spPaymentMethod, spPaymentStatus;
    private Button btnSaveExpense;
    private AppDatabase db;

    // Biến cho chức năng Thu gọn/Mở rộng Form và Danh sách
    private LinearLayout layoutExpandableForm, layoutHeaderToggle;
    private ImageView imgToggle;
    private int currentExpenseId = -1; // -1: Thêm mới, khác -1: Chế độ Sửa
    private RecyclerView rvExpenses;
    private ExpenseAdapter adapter;
    private TextView tvTotalExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        db = AppDatabase.getInstance(this);

        // Nhận thông tin Project từ Intent
        if (getIntent().hasExtra("PROJECT_ID")) {
            projectId = getIntent().getIntExtra("PROJECT_ID", -1);
            projectName = getIntent().getStringExtra("PROJECT_NAME");
        }

        // 1. Ánh xạ các thành phần điều khiển đóng/mở thẻ
        tvProjectName = findViewById(R.id.tvProjectName);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        imgToggle = findViewById(R.id.imgToggle);
        layoutExpandableForm = findViewById(R.id.layoutExpandableForm);
        layoutHeaderToggle = findViewById(R.id.layoutHeaderToggle);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        // 2. Ánh xạ 12 trường dữ liệu chuẩn
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

        // 3. Thiết lập RecyclerView hiển thị danh sách
        rvExpenses = findViewById(R.id.rvExpenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));

        tvProjectName.setText("Project: " + projectName);

        // 4. Thiết lập Spinners
        setupSpinners();

        // 5. Logic Đóng/Mở Form khi nhấn vào tiêu đề thẻ
        layoutHeaderToggle.setOnClickListener(v -> toggleForm());

        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> showDatePicker());

        // 6. KIỂM TRA CHẾ ĐỘ SỬA: Nếu nhấn từ nút Edit ở Adapter
        if (getIntent().hasExtra("EXPENSE_ID")) {
            currentExpenseId = getIntent().getIntExtra("EXPENSE_ID", -1);
            expandForm(); // Mở thẻ nhập liệu ra để sửa
            tvFormTitle.setText("Editing Expense");
            btnSaveExpense.setText("Update Expense");
            loadExpenseDataForEdit(currentExpenseId);
        }

        btnSaveExpense.setOnClickListener(v -> handleSaveOrUpdate());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // QUAN TRỌNG: Load lại danh sách tại đây để giải quyết lỗi "hiện thông tin cũ"
        loadExpensesList();
    }

    private void toggleForm() {
        if (layoutExpandableForm.getVisibility() == View.VISIBLE) {
            collapseForm();
        } else {
            expandForm();
        }
    }

    private void expandForm() {
        layoutExpandableForm.setVisibility(View.VISIBLE);
        imgToggle.setImageResource(android.R.drawable.arrow_up_float);
    }

    private void collapseForm() {
        layoutExpandableForm.setVisibility(View.GONE);
        imgToggle.setImageResource(android.R.drawable.arrow_down_float);
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

    // GỘP CHUNG THÀNH 1 HÀM LOAD DUY NHẤT VÀ TÍNH TỔNG TIỀN
    private void loadExpensesList() {
        new Thread(() -> {
            List<ExpenseEntity> list = db.appDao().getExpensesForProject(projectId);
            runOnUiThread(() -> {
                // Sử dụng rvExpenses đã khai báo ở trên
                adapter = new ExpenseAdapter(list, projectName);
                rvExpenses.setAdapter(adapter);

                // Gọi hàm tính tổng tiền
                calculateTotal(list);
            });
        }).start();
    }

    // HÀM TÍNH TỔNG TIỀN
    private void calculateTotal(List<ExpenseEntity> list) {
        double total = 0;
        for (ExpenseEntity e : list) {
            total += e.getAmount();
        }
        if (tvTotalExpense != null) {
            tvTotalExpense.setText("Total Expense: $" + String.format("%.2f", total));
        }
    }

    private void loadExpenseDataForEdit(int expenseId) {
        new Thread(() -> {
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
        if (adapter != null && value != null) {
            int pos = adapter.getPosition(value);
            if (pos >= 0) spinner.setSelection(pos);
        }
    }

    private void handleSaveOrUpdate() {
        String code = etExpenseCode.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String currency = etCurrency.getText().toString().trim();
        String claimant = etClaimant.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validation
        if (code.isEmpty() || date.isEmpty() || amountStr.isEmpty() || currency.isEmpty() || claimant.isEmpty()) {
            Toast.makeText(this, "Please fill required fields (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String type = spType.getSelectedItem().toString();
            String method = spPaymentMethod.getSelectedItem().toString();
            String status = spPaymentStatus.getSelectedItem().toString();
            String location = etLocation.getText().toString().trim();


            new AlertDialog.Builder(this)
                    .setTitle("Review Details")
                    .setMessage("Please review before saving:\n\n" +
                            "• ID: " + code + "\n" +
                            "• Date: " + date + "\n" +
                            "• Type: " + type + "\n" +
                            "• Amount: " + amount + " " + currency + "\n" +
                            "• Status: " + status + "\n" +
                            "• Claimant: " + claimant)
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        new Thread(() -> {
                            // Tạo Entity dựa trên 11 tham số chuẩn
                            ExpenseEntity expense = new ExpenseEntity(
                                    projectId, code, date, type, amount, currency,
                                    method, description, location, claimant, status
                            );

                            if (currentExpenseId != -1) {
                                expense.setExpenseId(currentExpenseId);
                                db.appDao().updateExpense(expense); // Cập nhật
                            } else {
                                db.appDao().insertExpense(expense); // Thêm mới
                            }

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Operation Successful!", Toast.LENGTH_SHORT).show();
                                loadExpensesList(); // Làm mới danh sách và tính lại tổng tiền
                                collapseForm();     // Thu gọn form sau khi lưu
                                clearForm();        // Xóa trắng form cho lần nhập sau

                                if (currentExpenseId != -1) {
                                    // Reset trạng thái sau khi Update
                                    currentExpenseId = -1;
                                    tvFormTitle.setText("Add New Expense");
                                    btnSaveExpense.setText("Save Expense");
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
        // Không xóa Date và Currency để thuận tiện nhập nhiều khoản cùng lúc
    }
}