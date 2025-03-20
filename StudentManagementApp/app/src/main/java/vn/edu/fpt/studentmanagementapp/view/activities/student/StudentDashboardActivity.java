package vn.edu.fpt.studentmanagementapp.view.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        tvStudentInfo = findViewById(R.id.tv_student_info);
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

        // Set up navigation buttons
        btnMyClasses.setOnClickListener(v -> {
            // TODO: Implement navigation to classes view
            Toast.makeText(this, "Classes view coming soon", Toast.LENGTH_SHORT).show();
        });

        btnMyAssignments.setOnClickListener(v -> {
            // TODO: Implement navigation to assignments view
            Toast.makeText(this, "Assignments view coming soon", Toast.LENGTH_SHORT).show();
        });

        btnMyGrades.setOnClickListener(v -> {
            // TODO: Implement navigation to grades view
            Toast.makeText(this, "Grades view coming soon", Toast.LENGTH_SHORT).show();
        });

        btnMyProfile.setOnClickListener(v -> {
            // TODO: Implement navigation to profile view
            Toast.makeText(this, "Profile view coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadStudentData(String userId) {
        db.collection("Students").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Student student = documentSnapshot.toObject(Student.class);
                        if (student != null) {
                            // Start displaying basic info
                            StringBuilder studentInfo = new StringBuilder();
                            studentInfo.append("Name: ").append(student.getName());

                            // Handle class information
                            if (student.getClassIds() != null && !student.getClassIds().isEmpty()) {
                                studentInfo.append("\nClasses: Loading...");
                                tvStudentInfo.setText(studentInfo.toString());

                                fetchClassNames(student.getClassIds(), classNames -> {
                                    // Update the student info with class names
                                    StringBuilder updatedInfo = new StringBuilder();
                                    updatedInfo.append("Name: ").append(student.getName())
                                            .append("\nClasses: ").append(classNames);
                                    tvStudentInfo.setText(updatedInfo.toString());
                                });
                            } else {
                                studentInfo.append("\nClasses: None");
                                tvStudentInfo.setText(studentInfo.toString());
                            }
                        }
                    } else {
                        tvStudentInfo.setText("No student data found. Please contact your teacher.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading student data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Function to fetch class names from class IDs
    private void fetchClassNames(List<String> classIds, ClassNamesCallback callback) {
        if (classIds == null || classIds.isEmpty()) {
            callback.onClassNamesLoaded("None");
            return;
        }

        List<String> classNames = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);

        for (String classId : classIds) {
            // Skip null or empty class IDs
            if (classId == null || classId.isEmpty()) {
                if (counter.incrementAndGet() == classIds.size()) {
                    String result = classNames.isEmpty() ? "None" : TextUtils.join(", ", classNames);
                    callback.onClassNamesLoaded(result);
                }
                continue;
            }

            db.collection("Classes").document(classId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String className = doc.getString("name");
                        if (className != null && !className.isEmpty()) {
                            classNames.add(className);
                        }

                        if (counter.incrementAndGet() == classIds.size()) {
                            // All classes fetched, call callback with joined class names
                            String joinedClassNames = classNames.isEmpty() ?
                                    "None" : TextUtils.join(", ", classNames);
                            callback.onClassNamesLoaded(joinedClassNames);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle error, increment counter to ensure callback is called
                        if (counter.incrementAndGet() == classIds.size()) {
                            String joinedClassNames = classNames.isEmpty() ?
                                    "None" : TextUtils.join(", ", classNames);
                            callback.onClassNamesLoaded(joinedClassNames);
                        }
                    });
        }
    }

    // Callback interface for class names
    interface ClassNamesCallback {
        void onClassNamesLoaded(String classNames);
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