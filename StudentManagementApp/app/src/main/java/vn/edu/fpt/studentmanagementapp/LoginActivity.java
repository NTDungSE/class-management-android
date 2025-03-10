package vn.edu.fpt.studentmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnGoToRegister = findViewById(R.id.btn_go_to_register);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading
            Toast.makeText(this, "Đang đăng nhập...", Toast.LENGTH_SHORT).show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            checkUserRoleAndRedirect();
                        } else {
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() :
                                    "Đăng nhập thất bại";
                            Toast.makeText(this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Chuyển sang màn hình đăng ký
        btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void checkUserRoleAndRedirect() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("teacher".equals(role)) {
                            startActivity(new Intent(LoginActivity.this, TeacherDashboardActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(LoginActivity.this, StudentDashboardActivity.class));
                            finish();
                        }
                    } else {
                        // Create user document if it doesn't exist (for users who registered before)
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", mAuth.getCurrentUser().getEmail());
                        user.put("role", "student"); // Default role

                        FirebaseFirestore.getInstance().collection("Users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(LoginActivity.this, "Created new user profile", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, StudentDashboardActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}