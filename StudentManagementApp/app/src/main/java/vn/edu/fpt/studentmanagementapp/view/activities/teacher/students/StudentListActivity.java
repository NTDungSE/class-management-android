package vn.edu.fpt.studentmanagementapp.view.activities.teacher.students;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FieldValue;

import java.util.List;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.view.adapters.StudentAdapter;
import vn.edu.fpt.studentmanagementapp.model.Student;
import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;

public class StudentListActivity extends AppCompatActivity implements StudentAdapter.StudentActionListener {
    private FirebaseFirestore db;
    private StudentAdapter adapter;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Configure RecyclerView
        RecyclerView rvStudents = findViewById(R.id.rv_students);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        // Query to fetch student list
        String currentUserId = mAuth.getCurrentUser().getUid();

        // Optional: Filter students by teacher's classes
        // This would require adding a teacherId field to students or querying classes first
        Query query = db.collection("Students");

        FirestoreRecyclerOptions<Student> options = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();

        adapter = new StudentAdapter(options, this);
        rvStudents.setAdapter(adapter);

        // Add new student
        ExtendedFloatingActionButton fabAddStudent = findViewById(R.id.fab_add_student);
        fabAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentFormActivity.class);
            startActivity(intent);
        });

        // Setup logout button
        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(StudentListActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    public void onEditStudent(String documentId, Student student) {
        // No need to get only first class - we'll load all classes in StudentFormActivity
        Intent intent = new Intent(this, StudentFormActivity.class);
        intent.putExtra("STUDENT_ID", documentId);
        intent.putExtra("STUDENT_NAME", student.getName());
        intent.putExtra("STUDENT_CODE", student.getStudentCode());
        intent.putExtra("USER_ID", student.getUserId());
        startActivity(intent);
    }

    @Override
    public void onDeleteStudent(String documentId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // First get the student to find classes to update
                    db.collection("Students").document(documentId)
                            .get()
                            .addOnSuccessListener(document -> {
                                Student student = document.toObject(Student.class);
                                if (student != null && student.getClassIds() != null) {
                                    // Remove student from classes
                                    for (String classId : student.getClassIds()) {
                                        db.collection("Classes").document(classId)
                                                .update("studentIds", FieldValue.arrayRemove(documentId));
                                    }
                                }

                                // Now delete the student
                                db.collection("Students").document(documentId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}