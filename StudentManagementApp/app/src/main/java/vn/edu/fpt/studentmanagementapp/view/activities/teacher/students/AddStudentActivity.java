package vn.edu.fpt.studentmanagementapp.view.activities.teacher.students;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import vn.edu.fpt.studentmanagementapp.R;

public class AddStudentActivity extends AppCompatActivity {
    private EditText etName, etClass, etCode, etEmail; // Added email field
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_name);
        etClass = findViewById(R.id.et_class);
        etCode = findViewById(R.id.et_code);
        etEmail = findViewById(R.id.et_email); // Add to layout
        Button btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> {
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

            if (!email.isEmpty()) {
                // Look up userId by email (optional)
                db.collection("Users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                String userId = querySnapshot.getDocuments().get(0).getId();
                                student.put("userId", userId);
                                saveStudent(student, userId);
                            } else {
                                Toast.makeText(this, "No user found with this email", Toast.LENGTH_SHORT).show();
                                saveStudent(student, null); // Save without userId
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            saveStudent(student, null); // Save without userId
                        });
            } else {
                saveStudent(student, null); // Save without linking to a user
            }
        });
    }

    private void saveStudent(Map<String, Object> student, String userId) {
        if (userId != null) {
            db.collection("Students").document(userId).set(student)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            db.collection("Students").add(student)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}