package vn.edu.fpt.studentmanagementapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditStudentActivity extends AppCompatActivity {
    private EditText etName, etClass, etCode;
    private FirebaseFirestore db;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_name);
        etClass = findViewById(R.id.et_class);
        etCode = findViewById(R.id.et_code);
        Button btnUpdate = findViewById(R.id.btn_update);
        Button btnCancel = findViewById(R.id.btn_cancel);

        // Get student data from intent
        studentId = getIntent().getStringExtra("STUDENT_ID");
        String name = getIntent().getStringExtra("STUDENT_NAME");
        String className = getIntent().getStringExtra("STUDENT_CLASS");
        String code = getIntent().getStringExtra("STUDENT_CODE");

        // Fill form with student data
        etName.setText(name);
        etClass.setText(className);
        etCode.setText(code);

        btnUpdate.setOnClickListener(v -> {
            String updatedName = etName.getText().toString().trim();
            String updatedClassName = etClass.getText().toString().trim();
            String updatedCode = etCode.getText().toString().trim();

            if (updatedName.isEmpty() || updatedClassName.isEmpty() || updatedCode.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updatedStudent = new HashMap<>();
            updatedStudent.put("name", updatedName);
            updatedStudent.put("className", updatedClassName);
            updatedStudent.put("studentCode", updatedCode);

            db.collection("Students").document(studentId)
                    .update(updatedStudent)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}