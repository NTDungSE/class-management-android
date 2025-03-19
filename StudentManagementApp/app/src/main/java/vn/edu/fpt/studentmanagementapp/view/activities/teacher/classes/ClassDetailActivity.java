package vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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
        toolbar.setTitle("Class Details");
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassStudentAdapter();
        rvStudents.setAdapter(adapter);

        // Set class name
        tvClassName.setText(className);

        // Load class and its students
        loadClassDetails();
    }

    private void loadClassDetails() {
        db.collection("Classes").document(classId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Class classData = documentSnapshot.toObject(Class.class);
                    if (classData == null || classData.getStudentIds() == null || classData.getStudentIds().isEmpty()) {
                        showNoStudentsView();
                        return;
                    }

                    // Update student count
                    tvStudentCount.setText(getString(R.string.student_count, classData.getStudentIds().size()));

                    // Fetch all students in this class
                    fetchStudentsInClass(classData.getStudentIds());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading class: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showNoStudentsView();
                });
    }

    private void fetchStudentsInClass(List<String> studentIds) {
        List<Student> students = new ArrayList<>();

        // Create a counter to track when all async operations are complete
        final int[] completedQueries = {0};
        final int totalQueries = studentIds.size();

        for (String studentId : studentIds) {
            db.collection("Students").document(studentId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        completedQueries[0]++;

                        if (documentSnapshot.exists()) {
                            Student student = documentSnapshot.toObject(Student.class);
                            if (student != null) {
                                students.add(student);
                            }
                        }

                        // Check if all queries are complete
                        if (completedQueries[0] == totalQueries) {
                            updateStudentsList(students);
                        }
                    })
                    .addOnFailureListener(e -> {
                        completedQueries[0]++;

                        // Check if all queries are complete
                        if (completedQueries[0] == totalQueries) {
                            updateStudentsList(students);
                        }
                    });
        }
    }

    private void updateStudentsList(List<Student> students) {
        if (students.isEmpty()) {
            showNoStudentsView();
        } else {
            tvNoStudents.setVisibility(View.GONE);
            rvStudents.setVisibility(View.VISIBLE);
            adapter.setStudents(students);
        }
    }

    private void showNoStudentsView() {
        tvNoStudents.setVisibility(View.VISIBLE);
        rvStudents.setVisibility(View.GONE);
        tvStudentCount.setText(getString(R.string.student_count, 0));
    }
}