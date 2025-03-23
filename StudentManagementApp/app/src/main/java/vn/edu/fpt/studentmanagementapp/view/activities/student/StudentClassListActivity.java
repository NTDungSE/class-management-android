package vn.edu.fpt.studentmanagementapp.view.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;
import vn.edu.fpt.studentmanagementapp.model.Student;
import vn.edu.fpt.studentmanagementapp.view.adapters.StudentClassAdapter;

public class StudentClassListActivity extends AppCompatActivity implements StudentClassAdapter.ClassActionListener {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StudentClassAdapter adapter;
    private RecyclerView rvClasses;
    private TextView tvNoClasses;
    private List<Class> enrolledClasses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_class_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String studentId = mAuth.getCurrentUser().getUid();

        // Initialize UI components
        rvClasses = findViewById(R.id.rv_student_classes);
        tvNoClasses = findViewById(R.id.tv_no_classes);
        rvClasses.setLayoutManager(new LinearLayoutManager(this));

        // Setup adapter
        adapter = new StudentClassAdapter(enrolledClasses, this);
        rvClasses.setAdapter(adapter);

        // Setup FAB to join class
        ExtendedFloatingActionButton fabJoinClass = findViewById(R.id.fab_join_class);
        fabJoinClass.setOnClickListener(v -> {
            Intent intent = new Intent(this, JoinClassActivity.class);
            startActivity(intent);
        });

        // Load student's enrolled classes
        loadEnrolledClasses(studentId);
    }

    private void loadEnrolledClasses(String studentId) {
        db.collection("Students").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Student student = documentSnapshot.toObject(Student.class);
                    if (student != null && student.getEnrolledClasses() != null && !student.getEnrolledClasses().isEmpty()) {
                        // Get list of class IDs
                        List<String> classIds = new ArrayList<>(student.getEnrolledClasses().keySet());
                        fetchClassDetails(classIds);
                    } else {
                        // No classes enrolled
                        showNoClassesMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    showNoClassesMessage();
                });
    }

    private void fetchClassDetails(List<String> classIds) {
        enrolledClasses.clear();

        for (String classId : classIds) {
            db.collection("Classes").document(classId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Class classData = documentSnapshot.toObject(Class.class);
                        if (classData != null) {
                            // Set the class ID from document ID
                            classData.setClassId(documentSnapshot.getId());
                            enrolledClasses.add(classData);

                            // Notify adapter of changes
                            adapter.notifyDataSetChanged();

                            // If we have classes, hide the "no classes" message
                            if (!enrolledClasses.isEmpty()) {
                                tvNoClasses.setVisibility(View.GONE);
                                rvClasses.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }

    private void showNoClassesMessage() {
        tvNoClasses.setVisibility(View.VISIBLE);
        rvClasses.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when coming back to this activity
        if (mAuth.getCurrentUser() != null) {
            loadEnrolledClasses(mAuth.getCurrentUser().getUid());
        }
    }

    @Override
    public void onViewClassDetails(String classId, Class classData) {
        Intent intent = new Intent(this, StudentClassDetailActivity.class);
        intent.putExtra("CLASS_ID", classId);
        intent.putExtra("CLASS_NAME", classData.getName());
        startActivity(intent);
    }
}