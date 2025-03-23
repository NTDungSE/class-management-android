package vn.edu.fpt.studentmanagementapp.view.activities.student;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.service.JoinClassService;

public class JoinClassActivity extends AppCompatActivity {
    private EditText etClassCode;
    private TextInputLayout tilClassCode;
    private Button btnJoinClass;
    private ProgressBar progressBar;
    private JoinClassService joinClassService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_class);

        // Initialize service
        joinClassService = new JoinClassService();

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Join Class");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        etClassCode = findViewById(R.id.et_class_code);
        tilClassCode = findViewById(R.id.til_class_code);
        btnJoinClass = findViewById(R.id.btn_join_class);
        progressBar = findViewById(R.id.progress_bar);

        // Set up button click listener
        btnJoinClass.setOnClickListener(v -> attemptJoinClass());
    }

    private void attemptJoinClass() {
        String classCode = etClassCode.getText().toString().trim();

        // Validate input
        if (classCode.isEmpty()) {
            tilClassCode.setError("Please enter a class code");
            return;
        } else {
            tilClassCode.setError(null);
        }

        // Show progress and disable button
        showProgress(true);

        // Try to join the class
        joinClassService.joinClassWithCode(classCode, new JoinClassService.JoinClassCallback() {
            @Override
            public void onSuccess(String className) {
                showProgress(false);
                Toast.makeText(JoinClassActivity.this,
                        "Successfully joined " + className, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                showProgress(false);
                Toast.makeText(JoinClassActivity.this,
                        errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClassNotFound() {
                showProgress(false);
                tilClassCode.setError("Invalid class code. Please check and try again.");
            }

            @Override
            public void onAlreadyJoined() {
                showProgress(false);
                Toast.makeText(JoinClassActivity.this,
                        "You are already enrolled in this class", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnJoinClass.setEnabled(!show);
    }
}