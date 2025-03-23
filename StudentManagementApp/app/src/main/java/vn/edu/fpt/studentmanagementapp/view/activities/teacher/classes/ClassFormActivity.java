package vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import vn.edu.fpt.studentmanagementapp.R;

public class ClassFormActivity extends AppCompatActivity {
    private EditText etClassName;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String classId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_form);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etClassName = findViewById(R.id.et_class_name);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);

        // Check if in edit mode
        classId = getIntent().getStringExtra("CLASS_ID");
        isEditMode = (classId != null);

        if (isEditMode) {
            String className = getIntent().getStringExtra("CLASS_NAME");
            etClassName.setText(className);
            btnSave.setText(R.string.update);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Class");
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Create New Class");
            }
        }

        btnSave.setOnClickListener(v -> handleSave());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void handleSave() {
        String className = etClassName.getText().toString().trim();
        String teacherId = mAuth.getCurrentUser().getUid();

        if (className.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_class_name, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            updateClass(className);
        } else {
            createNewClass(className, teacherId);
        }
    }

    private void createNewClass(String className, String teacherId) {
        // Generate a unique class code (like Google Classroom)
        String classCode = generateClassCode();

        Map<String, Object> classData = new HashMap<>();
        classData.put("name", className);
        classData.put("teacherId", teacherId);
        classData.put("classCode", classCode);
        classData.put("enrolledStudents", new HashMap<String, String>());

        db.collection("Classes")
                .add(classData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Class created! Class code: " + classCode, Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateClass(String className) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", className);

        db.collection("Classes").document(classId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, R.string.class_updated, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Generate a Google Classroom style class code
    private String generateClassCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 7; i++) {
            codeBuilder.append(chars.charAt(random.nextInt(chars.length())));
        }

        return codeBuilder.toString();
    }
}