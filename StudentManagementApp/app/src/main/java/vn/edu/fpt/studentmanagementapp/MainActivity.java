package vn.edu.fpt.studentmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Link to your activity_main.xml
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            // Redirect to StudentListActivity or other screens
            startActivity(new Intent(this, StudentListActivity.class));
        } else {
            // Redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish(); // Close MainActivity
    }
}