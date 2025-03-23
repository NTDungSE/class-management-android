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

import vn.edu.fpt.studentmanagementapp.view.activities.DashboardActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            // Just redirect to the unified dashboard
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            // Redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}