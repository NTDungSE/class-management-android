package vn.edu.fpt.studentmanagementapp.view.activities.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes.ClassListActivity;
import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;

public class TeacherDashboardActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        tvWelcome = findViewById(R.id.tv_welcome);
        Button btnManageClasses = findViewById(R.id.btn_manage_classes);
        Button btnManageAssignments = findViewById(R.id.btn_manage_assignments);
        Button btnReviewSubmissions = findViewById(R.id.btn_review_submissions);
        Button btnLogout = findViewById(R.id.btn_logout);

        // Set welcome message with user's email
        if (currentUser != null) {
            tvWelcome.setText("Welcome, " + currentUser.getEmail());
        }

        // Set up button click listeners

        btnManageClasses.setOnClickListener(v -> {
            startActivity(new Intent(TeacherDashboardActivity.this, ClassListActivity.class));
        });

        btnManageAssignments.setOnClickListener(v -> {
            // Will implement in Step 3
            Toast.makeText(this, "Assignment management coming soon", Toast.LENGTH_SHORT).show();
        });

        btnReviewSubmissions.setOnClickListener(v -> {
            // Will implement in Step 4
            Toast.makeText(this, "Submission review coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(TeacherDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is still authenticated
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
