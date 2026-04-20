package com.example.coursework;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coursework.adapter.ExpenseAdapter;
import com.example.coursework.database.AppDatabase;
import com.example.coursework.database.ExpenseEntity;

import java.util.List;

public class ExpenseActivity extends AppCompatActivity {

    private TextView tvProjectTitle;
    private EditText etType, etAmount, etTime, etComment;
    private Button btnAddExpense;
    private RecyclerView recyclerView;

    private AppDatabase db;
    private int projectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        db = AppDatabase.getInstance(this);

        // Nhận ID từ ProjectAdapter truyền qua
        projectId = getIntent().getIntExtra("PROJECT_ID", -1);
        String projectName = getIntent().getStringExtra("PROJECT_NAME");

        tvProjectTitle = findViewById(R.id.tvProjectTitle);
        etType = findViewById(R.id.etExpenseType);
        etAmount = findViewById(R.id.etExpenseAmount);
        etTime = findViewById(R.id.etExpenseTime);
        etComment = findViewById(R.id.etExpenseComment);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        recyclerView = findViewById(R.id.recyclerViewExpenses);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (projectName != null) {
            tvProjectTitle.setText("Expenses for: " + projectName);
        }

        if (projectId == -1) {
            Toast.makeText(this, "Error: Invalid Project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadExpenses();

        btnAddExpense.setOnClickListener(v -> saveExpense());
    }

    private void saveExpense() {
        String type = etType.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String comment = etComment.getText().toString().trim();

        if (type.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            new Thread(() -> {
                ExpenseEntity expense = new ExpenseEntity(projectId, type, amount, time, comment);
                db.appDao().insertExpense(expense);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    loadExpenses();
                });
            }).start();
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid number");
        }
    }

    private void loadExpenses() {
        new Thread(() -> {
            List<ExpenseEntity> expenses = db.appDao().getExpensesForProject(projectId);
            runOnUiThread(() -> {
                ExpenseAdapter adapter = new ExpenseAdapter(expenses);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void clearFields() {
        etType.setText("");
        etAmount.setText("");
        etTime.setText("");
        etComment.setText("");
    }
}