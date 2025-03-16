package vn.edu.fpt.studentmanagementapp.view.activities.teacher.students;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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

        // Cấu hình RecyclerView
        RecyclerView rvStudents = findViewById(R.id.rv_students);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        // Query để lấy danh sách học sinh
        Query query = db.collection("Students");
        FirestoreRecyclerOptions<Student> options = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();

        adapter = new StudentAdapter(options, this);
        rvStudents.setAdapter(adapter);

        // Thêm học sinh mới
        FloatingActionButton fabAddStudent = findViewById(R.id.fab_add_student);
        fabAddStudent.setOnClickListener(v ->
                startActivity(new Intent(this, AddStudentActivity.class)));

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
        Intent intent = new Intent(this, EditStudentActivity.class);
        intent.putExtra("STUDENT_ID", documentId);
        intent.putExtra("STUDENT_NAME", student.getName());
        intent.putExtra("STUDENT_CLASS", student.getClassName());
        intent.putExtra("STUDENT_CODE", student.getStudentCode());
        startActivity(intent);
    }

    @Override
    public void onDeleteStudent(String documentId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("Students").document(documentId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Success message
                            })
                            .addOnFailureListener(e -> {
                                // Error message
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