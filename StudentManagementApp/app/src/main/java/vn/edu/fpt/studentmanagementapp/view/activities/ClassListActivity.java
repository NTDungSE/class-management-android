package vn.edu.fpt.studentmanagementapp.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;
import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.student.JoinClassActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.student.StudentClassDetailActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes.ClassFormActivity;
import vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes.ClassInviteActivity;
import vn.edu.fpt.studentmanagementapp.view.adapters.ClassAdapter;
import vn.edu.fpt.studentmanagementapp.view.adapters.StudentClassAdapter;

public class ClassListActivity extends AppCompatActivity implements
        ClassAdapter.ClassActionListener, StudentClassAdapter.ClassActionListener {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView rvClasses;
    private TextView tvNoClasses;
    private ExtendedFloatingActionButton fabMain;
    private String userRole;

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

        // Teacher's adapter setup
        Query query = db.collection("Classes").whereEqualTo("teacherId", mAuth.getCurrentUser().getUid());
        FirestoreRecyclerOptions<Class> options = new FirestoreRecyclerOptions.Builder<Class>()
                .setQuery(query, Class.class)
                .build();
        ClassAdapter adapter = new ClassAdapter(options, this);
        rvClasses.setAdapter(adapter);
        adapter.startListening();
    }

    private void setupStudentUI() {
        fabMain.setText("Join Class");
        fabMain.setIconResource(android.R.drawable.ic_input_add);
        fabMain.setOnClickListener(v -> startActivity(new Intent(this, JoinClassActivity.class)));

        // Student's adapter setup (similar to StudentClassListActivity's logic)
        // Implement your student-specific data fetching here
    }


    // Implement interface methods for teacher
    @Override
    public void onEditClass(String classId, Class classData) {
        Intent intent = new Intent(this, ClassFormActivity.class);
        intent.putExtra("CLASS_ID", classId);
        intent.putExtra("CLASS_NAME", classData.getName());
        startActivity(intent);
    }

    @Override
    public void onManageStudents(String classId, Class classData) {
        Intent intent = new Intent(this, ClassInviteActivity.class);
        intent.putExtra("CLASS_ID", classId);
        intent.putExtra("CLASS_NAME", classData.getName());
        startActivity(intent);
    }

    @Override
    public void onDeleteClass(String classId) {
        // Teacher's delete logic
    }

    // Implement interface methods for student
    @Override
    public void onViewClassDetails(String classId, Class classData) {
        Intent intent = new Intent(this, StudentClassDetailActivity.class);
        intent.putExtra("CLASS_ID", classId);
        intent.putExtra("CLASS_NAME", classData.getName());
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if ("teacher".equals(userRole) && rvClasses.getAdapter() != null) {
            ((ClassAdapter) rvClasses.getAdapter()).startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if ("teacher".equals(userRole) && rvClasses.getAdapter() != null) {
            ((ClassAdapter) rvClasses.getAdapter()).stopListening();
        }
    }
}