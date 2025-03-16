package vn.edu.fpt.studentmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.student.StudentDashboardActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.TeacherDashboardActivity;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Link to your activity_main.xml
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            // Check user role and redirect accordingly, just like in LoginActivity
            checkUserRoleAndRedirect();
        } else {
            // Redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Close MainActivity
        }
    }

    private void checkUserRoleAndRedirect() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("teacher".equals(role)) {
                            startActivity(new Intent(MainActivity.this, TeacherDashboardActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(MainActivity.this, StudentDashboardActivity.class));
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
                                    Toast.makeText(MainActivity.this, "Created new user profile", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, StudentDashboardActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}