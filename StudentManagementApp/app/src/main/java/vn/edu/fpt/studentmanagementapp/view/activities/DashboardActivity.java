package vn.edu.fpt.studentmanagementapp.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import vn.edu.fpt.studentmanagementapp.view.activities.student.JoinClassActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.student.StudentClassListActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.student.assignments.AssignmentDetailActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.assignments.CreateAssignmentActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.assignments.SubmissionListActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes.ClassListActivity;

public class DashboardActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvWelcome, tvStudentInfo;
    private LinearLayout studentLayout, teacherLayout;
    private String userRole;

    // ActivityResultLauncher for the JoinClass activity
    private final ActivityResultLauncher<Intent> joinClassLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Refresh student data after joining a class
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null && "student".equals(userRole)) {
                        loadStudentData(currentUser.getUid());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize UI components
        tvWelcome = findViewById(R.id.tv_welcome);
        studentLayout = findViewById(R.id.student_layout);
        teacherLayout = findViewById(R.id.teacher_layout);
        tvStudentInfo = findViewById(R.id.tv_student_info);
        Button btnLogout = findViewById(R.id.btn_logout);

        // Set welcome message with user's email
        if (currentUser != null) {
            tvWelcome.setText("Welcome, " + currentUser.getEmail());
            checkUserRoleAndSetupUI(currentUser.getUid());
        }

        // Handle logout button
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        });

        // Setup student buttons
        setupStudentButtons();

        // Setup teacher buttons
        setupTeacherButtons();
    }

    private void checkUserRoleAndSetupUI(String userId) {
        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRole = documentSnapshot.getString("role");
                        if ("teacher".equals(userRole)) {
                            // Show teacher UI, hide student UI
                            teacherLayout.setVisibility(View.VISIBLE);
                            studentLayout.setVisibility(View.GONE);
                            tvStudentInfo.setVisibility(View.GONE);
                        } else {
                            // Show student UI, hide teacher UI
                            teacherLayout.setVisibility(View.GONE);
                            studentLayout.setVisibility(View.VISIBLE);
                            tvStudentInfo.setVisibility(View.VISIBLE);
                            loadStudentData(userId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupStudentButtons() {
        Button btnMyAssignments = findViewById(R.id.btn_my_assignments);
        Button btnMyClasses = findViewById(R.id.btn_join_class);
        Button btnMyGrades = findViewById(R.id.btn_my_grades);
        Button btnMyProfile = findViewById(R.id.btn_my_profile);

        btnMyClasses.setOnClickListener(v -> {
            // Navigate to the StudentClassListActivity instead of JoinClassActivity
            Intent intent = new Intent(DashboardActivity.this, StudentClassListActivity.class);
            startActivity(intent);
        });

        btnMyAssignments.setOnClickListener(v -> {
            // TODO: Implement navigation to assignments view
            // Toast.makeText(this, "Assignments view coming soon", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DashboardActivity.this, AssignmentDetailActivity.class));
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

    private void setupTeacherButtons() {
        Button btnManageClasses = findViewById(R.id.btn_manage_classes);
        Button btnManageAssignments = findViewById(R.id.btn_manage_assignments);
        Button btnReviewSubmissions = findViewById(R.id.btn_review_submissions);
        Button btnManageStudents = findViewById(R.id.btn_manage_students);

        btnManageStudents.setOnClickListener(v -> {
            Toast.makeText(this, "Student management coming soon", Toast.LENGTH_SHORT).show();
        });

        btnManageClasses.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, ClassListActivity.class));
        });

        btnManageAssignments.setOnClickListener(v -> {
            //Toast.makeText(this, "Assignment management coming soon", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DashboardActivity.this, CreateAssignmentActivity.class));
        });

        btnReviewSubmissions.setOnClickListener(v -> {
            //Toast.makeText(this, "Submission review coming soon", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DashboardActivity.this, SubmissionListActivity.class));
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

                            // Use enrolledClasses from Student model instead of legacy classIds
                            if (student.getEnrolledClasses() != null && !student.getEnrolledClasses().isEmpty()) {
                                studentInfo.append("\nClasses: Loading...");
                                tvStudentInfo.setText(studentInfo.toString());

                                List<String> classIds = new ArrayList<>(student.getEnrolledClasses().keySet());
                                fetchClassNames(classIds, classNames -> {
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