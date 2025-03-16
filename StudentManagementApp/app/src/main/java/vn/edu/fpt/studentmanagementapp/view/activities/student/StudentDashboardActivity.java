package vn.edu.fpt.studentmanagementapp.view.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Student;
import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;

public class StudentDashboardActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvWelcome, tvStudentInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        tvWelcome = findViewById(R.id.tv_welcome);
        tvStudentInfo = findViewById(R.id.tv_student_info); // Add to layout
        Button btnMyAssignments = findViewById(R.id.btn_my_assignments);
        Button btnMyClasses = findViewById(R.id.btn_my_classes);
        Button btnMyGrades = findViewById(R.id.btn_my_grades);
        Button btnMyProfile = findViewById(R.id.btn_my_profile);
        Button btnLogout = findViewById(R.id.btn_logout);

        if (currentUser != null) {
            tvWelcome.setText("Welcome, " + currentUser.getEmail());
            loadStudentData(currentUser.getUid());
        }

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(StudentDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void loadStudentData(String userId) {
        db.collection("Students").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Student student = documentSnapshot.toObject(Student.class);
                        tvStudentInfo.setText("Name: " + student.getName() + "\nClass: " + student.getClassName() + "\nCode: " + student.getStudentCode());
                    } else {
                        tvStudentInfo.setText("No student data found. Please contact your teacher.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading student data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}