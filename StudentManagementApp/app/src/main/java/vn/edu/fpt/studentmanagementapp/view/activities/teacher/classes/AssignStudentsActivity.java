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
        List<String> removedStudentIds = new ArrayList<>();

        // Get previously assigned students
        db.collection("Classes").document(classId).get()
                .addOnSuccessListener(doc -> {
                    Class classData = doc.toObject(Class.class);
                    if (classData != null && classData.getStudentIds() != null) {
                        List<String> currentStudentIds = classData.getStudentIds();

                        // Find removed students
                        for (String studentId : currentStudentIds) {
                            if (!assignedStudentIds.contains(studentId)) {
                                removedStudentIds.add(studentId);
                            }
                        }

                        // Update class with new students
                        db.collection("Classes").document(classId)
                                .update("studentIds", studentIdsList)
                                .addOnSuccessListener(aVoid -> {
                                    // Update each student's classes
                                    updateStudentClasses(studentIdsList, removedStudentIds);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    private void updateStudentClasses(List<String> addedStudentIds, List<String> removedStudentIds) {
        // Add class to students
        for (String studentId : addedStudentIds) {
            db.collection("Students").document(studentId).get()
                    .addOnSuccessListener(doc -> {
                        Student student = doc.toObject(Student.class);
                        if (student != null) {
                            // If student already has classIds, use them, otherwise create new list
                            List<String> classIds = student.getClassIds();
                            if (classIds == null) {
                                classIds = new ArrayList<>();
                            }

                            // Add current class if not already present
                            if (!classIds.contains(classId)) {
                                classIds.add(classId);
                                db.collection("Students").document(studentId)
                                        .update("classIds", classIds);
                            }
                        }
                    });
        }

        // Remove class from students
        for (String studentId : removedStudentIds) {
            db.collection("Students").document(studentId).get()
                    .addOnSuccessListener(doc -> {
                        Student student = doc.toObject(Student.class);
                        if (student != null && student.getClassIds() != null) {
                            List<String> classIds = student.getClassIds();
                            classIds.remove(classId);
                            db.collection("Students").document(studentId)
                                    .update("classIds", classIds);
                        }
                    });
        }

        Toast.makeText(this, "Students assigned successfully", Toast.LENGTH_SHORT).show();
        new android.os.Handler().postDelayed(this::finish, 300);
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