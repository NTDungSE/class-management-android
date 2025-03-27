package vn.edu.fpt.studentmanagementapp.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;
import vn.edu.fpt.studentmanagementapp.model.Student;
import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.student.JoinClassActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.assignments.CreateAssignmentActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes.ClassFormActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes.ClassInviteActivity;
import vn.edu.fpt.studentmanagementapp.view.adapters.TeacherClassAdapter;
import vn.edu.fpt.studentmanagementapp.view.adapters.StudentClassAdapter;

public class ClassListActivity extends AppCompatActivity implements
        TeacherClassAdapter.ClassActionListener, StudentClassAdapter.ClassActionListener {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView rvClasses;
    private TextView tvNoClasses;
    private ExtendedFloatingActionButton fabMain;
    private String userRole;

    private List<Class> enrolledClasses;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userRole = getIntent().getStringExtra("USER_ROLE");

        // Initialize UI
        rvClasses = findViewById(R.id.rv_classes);
        tvNoClasses = findViewById(R.id.tv_no_classes);
        fabMain = findViewById(R.id.fab_main);

        // Setup RecyclerView
        rvClasses.setLayoutManager(new LinearLayoutManager(this));

        // Configure UI based on role
        if ("teacher".equals(userRole)) {
            setupTeacherUI();
        } else {
            setupStudentUI();
        }

        // Logout button
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void setupTeacherUI() {
        fabMain.setText("Add Class");
        fabMain.setIconResource(android.R.drawable.ic_input_add);
        fabMain.setOnClickListener(v -> startActivity(new Intent(this, ClassFormActivity.class)));

        Query query = db.collection("Classes").whereEqualTo("teacherId", mAuth.getCurrentUser().getUid());
        FirestoreRecyclerOptions<Class> options = new FirestoreRecyclerOptions.Builder<Class>()
                .setQuery(query, Class.class)
                .build();
        TeacherClassAdapter adapter = new TeacherClassAdapter(options, this);
        rvClasses.setAdapter(adapter);

        // Add AdapterDataObserver to handle empty state
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                checkEmptyView(adapter);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                checkEmptyView(adapter);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                checkEmptyView(adapter);
            }

            private void checkEmptyView(TeacherClassAdapter adapter) {
                if (adapter.getItemCount() == 0) {
                    tvNoClasses.setVisibility(View.VISIBLE);
                    rvClasses.setVisibility(View.GONE);
                } else {
                    tvNoClasses.setVisibility(View.GONE);
                    rvClasses.setVisibility(View.VISIBLE);
                }
            }
        });

        adapter.startListening();
    }

    private void setupStudentUI() {
        fabMain.setText("Join Class");
        fabMain.setIconResource(android.R.drawable.ic_input_add);
        fabMain.setOnClickListener(v -> startActivity(new Intent(this, JoinClassActivity.class)));

        // Initialize adapter with the instance variable enrolledClasses
        enrolledClasses = new ArrayList<>(); // Initialize the instance variable
        StudentClassAdapter adapter = new StudentClassAdapter(enrolledClasses, this);
        rvClasses.setAdapter(adapter);

        // Load student's enrolled classes
        String studentId = mAuth.getCurrentUser().getUid();
        db.collection("Students").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Student student = documentSnapshot.toObject(Student.class);
                    if (student != null && student.getEnrolledClasses() != null && !student.getEnrolledClasses().isEmpty()) {
                        List<String> classIds = new ArrayList<>(student.getEnrolledClasses().keySet());

                        // Fetch all classes where document ID is in classIds
                        db.collection("Classes")
                                .whereIn(FieldPath.documentId(), classIds)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    enrolledClasses.clear();
                                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                        Class classData = doc.toObject(Class.class);
                                        if (classData != null) {
                                            classData.setClassId(doc.getId());
                                            enrolledClasses.add(classData);
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                    if (enrolledClasses.isEmpty()) {
                                        showNoClassesMessage();
                                    } else {
                                        tvNoClasses.setVisibility(View.GONE);
                                        rvClasses.setVisibility(View.VISIBLE);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    showNoClassesMessage();
                                    Toast.makeText(this, "Error loading classes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        showNoClassesMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    showNoClassesMessage();
                    Toast.makeText(this, "Error loading classes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showNoClassesMessage() {
        tvNoClasses.setVisibility(View.VISIBLE);
        rvClasses.setVisibility(View.GONE);
        tvNoClasses.setText("You are not enrolled in any classes");
    }


    // Implement interface methods for teacher
    @Override
    public void onEditClass(String classId, Class classData) {
        Intent intent = new Intent(this, ClassFormActivity.class);
        intent.putExtra("CLASS_ID", classId);
        startActivity(intent);
    }

    @Override
    public void onManageStudents(String classId, Class classData) {
        Intent intent = new Intent(this, ClassInviteActivity.class);
        intent.putExtra("CLASS_ID", classId);
        startActivity(intent);
    }

    @Override
    public void onDeleteClass(String classId) {
        // Teacher's delete logic
    }

    // Implement interface methods for student
    @Override
    public void onViewClassDetails(String classId, Class classData) {
        Intent intent = new Intent(this, ClassDetailActivity.class);
        intent.putExtra("CLASS_ID", classId);
        intent.putExtra("IS_TEACHER", "teacher".equals(userRole));
        startActivity(intent);
    }

    @Override
    public void onCreateAssignment(String classId, Class classData) {
        Intent intent = new Intent(this, CreateAssignmentActivity.class);
        intent.putExtra("classId", classId);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh class list when returning from assignment creation
            if (rvClasses.getAdapter() != null) {
                ((TeacherClassAdapter) rvClasses.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if ("teacher".equals(userRole) && rvClasses.getAdapter() != null) {
            ((TeacherClassAdapter) rvClasses.getAdapter()).startListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ("teacher".equals(userRole) && rvClasses.getAdapter() != null) {
            rvClasses.getRecycledViewPool().clear();
            rvClasses.setAdapter(rvClasses.getAdapter());
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if ("teacher".equals(userRole) && rvClasses.getAdapter() != null) {
            ((TeacherClassAdapter) rvClasses.getAdapter()).stopListening();
        }
    }
}