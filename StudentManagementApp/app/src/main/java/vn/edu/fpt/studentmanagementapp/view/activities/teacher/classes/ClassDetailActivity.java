package vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;
import vn.edu.fpt.studentmanagementapp.model.Student;
import vn.edu.fpt.studentmanagementapp.view.adapters.ClassStudentAdapter;

public class ClassDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView rvStudents;
    private TextView tvNoStudents;
    private TextView tvClassName;
    private TextView tvStudentCount;
    private ClassStudentAdapter adapter;
    private String classId;
    private String className;
    private static final String TAG = "ClassDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);
        db = FirebaseFirestore.getInstance();

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        rvStudents = findViewById(R.id.rv_students);
        tvNoStudents = findViewById(R.id.tv_no_students);
        tvClassName = findViewById(R.id.tv_class_name);
        tvStudentCount = findViewById(R.id.tv_student_count);

        // Get class ID from intent
        classId = getIntent().getStringExtra("CLASS_ID");
        className = getIntent().getStringExtra("CLASS_NAME");

        if (classId == null) {
            Toast.makeText(this, "Error: Class ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Class Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassStudentAdapter();
        rvStudents.setAdapter(adapter);

        // Set class name
        tvClassName.setText(className);

        // Load class and its students
        loadClassDetails();

        FloatingActionButton fabInvite = findViewById(R.id.fab_invite);
        fabInvite.setOnClickListener(v -> {
            Intent intent = new Intent(ClassDetailActivity.this, ClassInviteActivity.class);
            intent.putExtra("CLASS_ID", classId);
            intent.putExtra("CLASS_NAME", className);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from other activities
        loadClassDetails();
    }

    private void loadClassDetails() {
        db.collection("Classes").document(classId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Class classData = documentSnapshot.toObject(Class.class);
                    if (classData == null || classData.getEnrolledStudents() == null ||
                            classData.getEnrolledStudents().isEmpty()) {
                        showNoStudentsView();
                        return;
                    }

                    // Update student count with enrolled and invited counts
                    int enrolledCount = classData.getEnrolledStudentCount();
                    int invitedCount = classData.getInvitedStudentCount();
                    int totalCount = enrolledCount + invitedCount;

                    String countText = getString(R.string.student_count_detail,
                            totalCount, enrolledCount, invitedCount);
                    tvStudentCount.setText(countText);

                    // Fetch all students in this class
                    fetchStudentsInClass(classData.getEnrolledStudents());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading class: " + e.getMessage(), e);
                    Toast.makeText(this, "Error loading class: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showNoStudentsView();
                });
    }

    private void fetchStudentsInClass(Map<String, String> enrolledStudents) {
        if (enrolledStudents.isEmpty()) {
            showNoStudentsView();
            return;
        }

        List<ClassStudentAdapter.StudentWithStatus> studentsWithStatus = new ArrayList<>();
        final int[] completedQueries = {0};
        final int totalQueries = enrolledStudents.size();

        for (Map.Entry<String, String> entry : enrolledStudents.entrySet()) {
            String identifier = entry.getKey();
            String status = entry.getValue();

            // Determine if this is an email or userId
            if (identifier.contains("@")) {
                // Query by email
                db.collection("Students")
                        .whereEqualTo("email", identifier)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            completedQueries[0]++;

                            if (!querySnapshot.isEmpty()) {
                                Student student = querySnapshot.getDocuments().get(0).toObject(Student.class);
                                if (student != null) {
                                    studentsWithStatus.add(new ClassStudentAdapter.StudentWithStatus(student, status));
                                }
                            } else {
                                // Create a placeholder student if not found
                                Student placeholderStudent = new Student("Invited User", identifier);
                                studentsWithStatus.add(new ClassStudentAdapter.StudentWithStatus(placeholderStudent, status));
                            }

                            checkIfComplete(completedQueries[0], totalQueries, studentsWithStatus);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error fetching student by email: " + e.getMessage(), e);
                            completedQueries[0]++;
                            checkIfComplete(completedQueries[0], totalQueries, studentsWithStatus);
                        });
            } else {
                // Query by userId
                db.collection("Students")
                        .document(identifier)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            completedQueries[0]++;

                            Student student = documentSnapshot.toObject(Student.class);
                            if (student != null) {
                                studentsWithStatus.add(new ClassStudentAdapter.StudentWithStatus(student, status));
                            }

                            checkIfComplete(completedQueries[0], totalQueries, studentsWithStatus);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error fetching student by ID: " + e.getMessage(), e);
                            completedQueries[0]++;
                            checkIfComplete(completedQueries[0], totalQueries, studentsWithStatus);
                        });
            }
        }
    }

    private void checkIfComplete(int completed, int total, List<ClassStudentAdapter.StudentWithStatus> students) {
        if (completed == total) {
            updateStudentsList(students);
        }
    }

    private void updateStudentsList(List<ClassStudentAdapter.StudentWithStatus> students) {
        if (students.isEmpty()) {
            showNoStudentsView();
        } else {
            tvNoStudents.setVisibility(View.GONE);
            rvStudents.setVisibility(View.VISIBLE);
            adapter.setStudentsWithStatus(students);
            adapter.setOnStudentRemovedListener(this::handleStudentRemoval);
        }
    }

    private void handleStudentRemoval(String identifier) {
        db.collection("Classes").document(classId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Class classData = documentSnapshot.toObject(Class.class);
                    if (classData != null) {
                        Map<String, String> students = classData.getEnrolledStudents();
                        students.remove(identifier);

                        // Update enrolled counts
                        db.collection("Classes").document(classId)
                                .update("enrolledStudents", students)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Student removed successfully", Toast.LENGTH_SHORT).show();
                                    loadClassDetails();
                                    removeFromStudentDocument(identifier);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing student from class: " + e.getMessage(), e);
                                    Toast.makeText(this, "Error removing student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    private void removeFromStudentDocument(String identifier) {
        // Determine if identifier is email or userId
        if (identifier.contains("@")) {
            // Look up by email
            db.collection("Students")
                    .whereEqualTo("email", identifier)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String studentId = querySnapshot.getDocuments().get(0).getId();
                            updateStudentEnrollment(studentId);
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error finding student by email: " + e.getMessage(), e));
        } else {
            // Look up by userId - direct document access
            updateStudentEnrollment(identifier);
        }
    }

    private void updateStudentEnrollment(String studentId) {
        db.collection("Students").document(studentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Student student = documentSnapshot.toObject(Student.class);
                    if (student != null) {
                        Map<String, String> enrolledClasses = student.getEnrolledClasses() != null ?
                                new HashMap<>(student.getEnrolledClasses()) : new HashMap<>();

                        // Remove the class from student's enrollment
                        enrolledClasses.remove(classId);

                        // Update Firestore document
                        db.collection("Students").document(studentId)
                                .update("enrolledClasses", enrolledClasses)
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error updating student enrollment", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error getting student document", e));
    }

    private void showNoStudentsView() {
        tvNoStudents.setVisibility(View.VISIBLE);
        rvStudents.setVisibility(View.GONE);
        tvStudentCount.setText(getString(R.string.student_count, 0));
    }
}