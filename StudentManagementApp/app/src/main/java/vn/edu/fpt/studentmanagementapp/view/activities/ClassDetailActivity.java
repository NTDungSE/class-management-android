package vn.edu.fpt.studentmanagementapp.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;
import vn.edu.fpt.studentmanagementapp.model.Student;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.assignments.AssignmentListActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes.ClassInviteActivity;
import vn.edu.fpt.studentmanagementapp.view.adapters.DetailClassStudentAdapter;

public class ClassDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView rvStudents;
    private TextView tvNoStudents, tvClassName, tvStudentCount, tvClassCode;
    private DetailClassStudentAdapter adapter;
    private String classId;
    private boolean isTeacher;
    private static final String TAG = "ClassDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);
        db = FirebaseFirestore.getInstance();

        // Get intent extras
        classId = getIntent().getStringExtra("CLASS_ID");
        isTeacher = getIntent().getBooleanExtra("IS_TEACHER", false);

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        rvStudents = findViewById(R.id.rv_students);
        tvNoStudents = findViewById(R.id.tv_no_students);
        tvClassName = findViewById(R.id.tv_class_name);
        tvStudentCount = findViewById(R.id.tv_student_count);
        tvClassCode = findViewById(R.id.tv_class_code);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isTeacher ? "Class Details" : "Class Information");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Role-based UI setup
        if (isTeacher) {
            setupTeacherUI();
        } else {
            setupStudentUI();
        }

        loadClassDetails();
    }

    private void setupTeacherUI() {
        // Show teacher elements
        findViewById(R.id.tv_students_list_title).setVisibility(View.VISIBLE);
        rvStudents.setVisibility(View.VISIBLE);
        findViewById(R.id.fab_invite).setVisibility(View.VISIBLE);

        // Setup RecyclerView
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DetailClassStudentAdapter();
        rvStudents.setAdapter(adapter);

        // Setup FAB
        FloatingActionButton fabInvite = findViewById(R.id.fab_invite);
        fabInvite.bringToFront();
        fabInvite.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassInviteActivity.class);
            intent.putExtra("CLASS_ID", classId);
            startActivity(intent);
        });

        ImageButton btnAssignments = findViewById(R.id.btn_assignments);
        btnAssignments.setVisibility(View.VISIBLE);
        if (btnAssignments != null) {
            btnAssignments.setVisibility(View.VISIBLE);
            btnAssignments.setOnClickListener(v -> {
                Intent intent = new Intent(this, AssignmentListActivity.class);
                intent.putExtra("CLASS_ID", classId);
                intent.putExtra("IS_TEACHER", true);
                startActivity(intent);
            });
        }
        adapter.setTeacherRole(true);
    }

    private void setupStudentUI() {
        // Show student elements
        tvClassCode.setVisibility(View.VISIBLE);
        MaterialCardView card = findViewById(R.id.card_class_info);
        card.setCardElevation(8f);

        ImageButton btnAssignments = findViewById(R.id.btn_assignments);
        btnAssignments.setVisibility(View.VISIBLE);
        btnAssignments.setOnClickListener(v -> {
            Intent intent = new Intent(this, AssignmentListActivity.class);
            intent.putExtra("CLASS_ID", classId);
            intent.putExtra("IS_TEACHER", false);
            startActivity(intent);
        });
    }

    private void loadClassDetails() {
        db.collection("Classes").document(classId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Class classData = documentSnapshot.toObject(Class.class);
                    if (classData != null) {
                        updateCommonUI(classData);
                        if (isTeacher) {
                            updateTeacherUI(classData);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading class: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateCommonUI(Class classData) {
        tvClassName.setText(classData.getName());
        if (!isTeacher) {
            tvClassCode.setText("Class Code: " + classData.getClassCode());
            tvStudentCount.setText("Students: " + classData.getEnrolledStudentCount());
        }
    }

    private void updateTeacherUI(Class classData) {
        int enrolledCount = classData.getEnrolledStudentCount();
        int invitedCount = classData.getInvitedStudentCount();
        tvStudentCount.setText(String.format("Students: %d (Enrolled: %d, Invited: %d)",
                enrolledCount + invitedCount, enrolledCount, invitedCount));

        if (classData.getEnrolledStudents() != null && !classData.getEnrolledStudents().isEmpty()) {
            fetchStudents(classData.getEnrolledStudents());
        } else {
            showNoStudentsView();
        }
    }

    private void fetchStudents(Map<String, String> enrolledStudents) {
        if (enrolledStudents.isEmpty()) {
            showNoStudentsView();
            return;
        }

        List<DetailClassStudentAdapter.StudentWithStatus> studentsWithStatus = new ArrayList<>();
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
                                    studentsWithStatus.add(new DetailClassStudentAdapter.StudentWithStatus(student, status));
                                }
                            } else {
                                // Create a placeholder student if not found
                                Student placeholderStudent = new Student("Invited User", identifier);
                                studentsWithStatus.add(new DetailClassStudentAdapter.StudentWithStatus(placeholderStudent, status));
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
                                studentsWithStatus.add(new DetailClassStudentAdapter.StudentWithStatus(student, status));
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

    private void checkIfComplete(int completed, int total, List<DetailClassStudentAdapter.StudentWithStatus> students) {
        if (completed == total) {
            updateStudentsList(students);
        }
    }

    private void updateStudentsList(List<DetailClassStudentAdapter.StudentWithStatus> students) {
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

    public void viewAssignments(View view) {
        Intent intent = new Intent(this, AssignmentListActivity.class);
        intent.putExtra("CLASS_ID", classId);
        startActivity(intent);
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
                        } else {
                            Log.d(TAG, "Invited student not registered: " + identifier);
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
    @Override
    protected void onResume() {
        super.onResume();
        if (isTeacher) loadClassDetails();
    }
}