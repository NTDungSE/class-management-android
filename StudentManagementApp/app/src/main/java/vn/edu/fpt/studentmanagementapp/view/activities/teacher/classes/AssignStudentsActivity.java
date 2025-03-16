package vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;
import vn.edu.fpt.studentmanagementapp.model.Student;
import vn.edu.fpt.studentmanagementapp.view.adapters.AssignStudentAdapter;

public class AssignStudentsActivity extends AppCompatActivity implements AssignStudentAdapter.AssignActionListener {
    private FirebaseFirestore db;
    private AssignStudentAdapter adapter;
    private String classId;
    private Set<String> assignedStudentIds = new HashSet<>(); // Changed to HashSet to prevent duplicates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_students);
        db = FirebaseFirestore.getInstance();

        classId = getIntent().getStringExtra("CLASS_ID");
        if (classId == null) {
            Toast.makeText(this, "Error: Class ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveAssignments());

        // Load current assignments right after setting up the RecyclerView
        loadCurrentAssignments();
    }

    private void setupRecyclerView() {
        RecyclerView rvStudents = findViewById(R.id.rv_students);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        Query query = db.collection("Students");
        FirestoreRecyclerOptions<Student> options = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();

        adapter = new AssignStudentAdapter(options, this);
        rvStudents.setAdapter(adapter);
    }

    private void loadCurrentAssignments() {
        db.collection("Classes").document(classId).get()
                .addOnSuccessListener(doc -> {
                    Class classData = doc.toObject(Class.class);
                    if (classData != null && classData.getStudentIds() != null) {
                        assignedStudentIds = new HashSet<>(classData.getStudentIds());
                        adapter.setAssignedStudentIds(new ArrayList<>(assignedStudentIds));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading class data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
        List<String> studentIdsList = new ArrayList<>(assignedStudentIds);

        db.collection("Classes").document(classId)
                .update("studentIds", studentIdsList)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Students assigned successfully", Toast.LENGTH_SHORT).show();
                    // Finish after a brief delay to allow the UI to update
                    new android.os.Handler().postDelayed(this::finish, 300);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}