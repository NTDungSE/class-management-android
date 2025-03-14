package vn.edu.fpt.studentmanagementapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AssignStudentsActivity extends AppCompatActivity implements AssignStudentAdapter.AssignActionListener {
    private FirebaseFirestore db;
    private AssignStudentAdapter adapter;
    private String classId;
    private List<String> assignedStudentIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_students); // New layout needed
        db = FirebaseFirestore.getInstance();

        classId = getIntent().getStringExtra("CLASS_ID");

        RecyclerView rvStudents = findViewById(R.id.rv_students);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        Query query = db.collection("Students");
        FirestoreRecyclerOptions<Student> options = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();

        adapter = new AssignStudentAdapter(options, this);
        rvStudents.setAdapter(adapter);

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveAssignments());
    }

    @Override
    public void onToggleStudent(String studentId, boolean isAssigned) {
        if (isAssigned) {
            assignedStudentIds.add(studentId);
        } else {
            assignedStudentIds.remove(studentId);
        }
    }

    private void saveAssignments() {
        db.collection("Classes").document(classId)
                .update("studentIds", assignedStudentIds)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Students assigned successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        // Load current assignments
        db.collection("Classes").document(classId).get()
                .addOnSuccessListener(doc -> {
                    Class classData = doc.toObject(Class.class);
                    if (classData != null) {
                        assignedStudentIds = new ArrayList<>(classData.getStudentIds());
                        adapter.setAssignedStudentIds(assignedStudentIds);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}