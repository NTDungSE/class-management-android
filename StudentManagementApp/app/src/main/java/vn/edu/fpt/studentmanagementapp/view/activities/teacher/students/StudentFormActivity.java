package vn.edu.fpt.studentmanagementapp.view.activities.teacher.students;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import vn.edu.fpt.studentmanagementapp.R;

public class StudentFormActivity extends AppCompatActivity {
    private EditText etName, etCode, etEmail;
    private AutoCompleteTextView etClass;
    private Button btnSave, btnCancel;
    private TextView tvTitle;
    private FirebaseFirestore db;

    // Variables to track if we're editing
    private String studentId;
    private boolean isEditMode = false;
    private String originalUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_form);
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvTitle = findViewById(R.id.tv_form_title);
        etName = findViewById(R.id.et_name);
        etClass = findViewById(R.id.et_class);
        etCode = findViewById(R.id.et_code);
        etEmail = findViewById(R.id.et_email);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        // Check if we're in edit mode
        studentId = getIntent().getStringExtra("STUDENT_ID");
        isEditMode = (studentId != null);

        // Configure UI based on mode
        if (isEditMode) {
            tvTitle.setText(R.string.edit_student);
            btnSave.setText(R.string.update);
            btnCancel.setVisibility(View.VISIBLE);

            // Fill form with student data
            String name = getIntent().getStringExtra("STUDENT_NAME");
            String className = getIntent().getStringExtra("STUDENT_CLASS");
            String code = getIntent().getStringExtra("STUDENT_CODE");
            originalUserId = getIntent().getStringExtra("USER_ID");

            etName.setText(name);
            etClass.setText(className);
            etCode.setText(code);

            // Load email if user ID exists
            if (originalUserId != null && !originalUserId.isEmpty()) {
                db.collection("Users").document(originalUserId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String email = documentSnapshot.getString("email");
                                if (email != null) {
                                    etEmail.setText(email);
                                }
                            }
                        });
            }
        } else {
            tvTitle.setText(R.string.add_student);
            btnSave.setText(R.string.save);
            btnCancel.setVisibility(View.GONE);
        }

        // Load available classes for dropdown
        loadClassesForDropdown();

        // Set up button listeners
        btnSave.setOnClickListener(v -> saveStudent());

        if (btnCancel.getVisibility() == View.VISIBLE) {
            btnCancel.setOnClickListener(v -> finish());
        }
    }

    private void saveStudent() {
        String name = etName.getText().toString().trim();
        String className = etClass.getText().toString().trim();
        String studentCode = etCode.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.isEmpty() || className.isEmpty() || studentCode.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> student = new HashMap<>();
        student.put("name", name);
        student.put("className", className);
        student.put("studentCode", studentCode);

        // If in edit mode and we already have a userId, preserve it
        if (isEditMode && originalUserId != null && !originalUserId.isEmpty()) {
            student.put("userId", originalUserId);
            updateStudent(student);
            return;
        }

        // If email provided, look up or update userId
        if (!email.isEmpty()) {
            db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String userId = querySnapshot.getDocuments().get(0).getId();
                            student.put("userId", userId);
                            if (isEditMode) {
                                updateStudent(student);
                            } else {
                                createStudent(student, userId);
                            }
                        } else {
                            Toast.makeText(this, "No user found with this email", Toast.LENGTH_SHORT).show();
                            if (isEditMode) {
                                // If editing, keep the original userId if no new user found
                                if (originalUserId != null) {
                                    student.put("userId", originalUserId);
                                }
                                updateStudent(student);
                            } else {
                                createStudent(student, null);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        if (isEditMode) {
                            updateStudent(student);
                        } else {
                            createStudent(student, null);
                        }
                    });
        } else {
            // No email provided
            if (isEditMode) {
                // Remove userId if email field is cleared
                updateStudent(student);
            } else {
                createStudent(student, null);
            }
        }
    }

    private void createStudent(Map<String, Object> student, String userId) {
        if (userId != null) {
            // Use userId as document ID if available
            db.collection("Students").document(userId).set(student)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Let Firestore generate document ID
            db.collection("Students").add(student)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateStudent(Map<String, Object> student) {
        db.collection("Students").document(studentId)
                .update(student)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadClassesForDropdown() {
        ArrayList<String> classNames = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, classNames);

        etClass.setAdapter(adapter);

        // Get the current user's ID (teacher)
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Only fetch classes where this teacher is the owner
        db.collection("Classes")
                .whereEqualTo("teacherId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    classNames.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String className = doc.getString("name");
                        if (className != null) {
                            classNames.add(className);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (classNames.isEmpty() && !isEditMode) {
                        Toast.makeText(StudentFormActivity.this,
                                "No classes found. Create a class first.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading classes: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}