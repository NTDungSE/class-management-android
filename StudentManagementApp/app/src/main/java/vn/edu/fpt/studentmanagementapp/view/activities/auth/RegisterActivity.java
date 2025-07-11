package vn.edu.fpt.studentmanagementapp.view.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etName, etClass, etCode; // Added fields
    private RadioGroup rgRole;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etName = findViewById(R.id.et_name); // Add to layout
        rgRole = findViewById(R.id.rg_role);
        Button btnRegister = findViewById(R.id.btn_register);

        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            LinearLayout studentFields = findViewById(R.id.student_fields);
            if (checkedId == R.id.rb_student) {
                studentFields.setVisibility(View.VISIBLE);
            } else {
                studentFields.setVisibility(View.GONE);
            }
        });

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String name = etName.getText().toString().trim();
            int selectedRoleId = rgRole.getCheckedRadioButtonId();
            String role = selectedRoleId == R.id.rb_teacher ? "teacher" : "student";

            if (role.equals("student") && name.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register user
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                            // Save to Users collection
                            Map<String, Object> user = new HashMap<>();
                            user.put("email", email);
                            user.put("role", role);
                            db.collection("Users").document(userId).set(user);

                            // If student, save to Students collection
                            if (role.equals("student")) {
                                Map<String, Object> student = new HashMap<>();
                                student.put("name", name);
                                student.put("userId", userId);
                                student.put("email", email);
                                // Initialize with empty map for enrolled classes
                                student.put("enrolledClasses", new HashMap<String, String>());
                                db.collection("Students").document(userId) // Use userId as document ID
                                        .set(student)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error saving student data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            }
                        } else {
                            String error = "Registration failed";
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                error = "Email already registered";
                            } else if (task.getException() != null) {
                                error = task.getException().getMessage();
                            }
                            Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}