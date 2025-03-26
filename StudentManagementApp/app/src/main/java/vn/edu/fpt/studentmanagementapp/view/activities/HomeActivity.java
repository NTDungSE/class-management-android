package vn.edu.fpt.studentmanagementapp.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            // Redirect to login if not authenticated
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return; // Exit onCreate early
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Check user role and redirect
        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        // Inside HomeActivity.java's onSuccessListener:
                        if ("teacher".equals(role)) {
                            Intent intent = new Intent(this, ClassListActivity.class);
                            intent.putExtra("USER_ROLE", "teacher");
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(this, ClassListActivity.class);
                            intent.putExtra("USER_ROLE", "student");
                            startActivity(intent);
                        }
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error detecting role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    finish();
                });
    }
}