package vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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
import vn.edu.fpt.studentmanagementapp.model.Class;
import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;
import vn.edu.fpt.studentmanagementapp.view.adapters.ClassAdapter;

public class ClassListActivity extends AppCompatActivity implements ClassAdapter.ClassActionListener {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ClassAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list); // New layout needed
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        RecyclerView rvClasses = findViewById(R.id.rv_classes);
        rvClasses.setLayoutManager(new LinearLayoutManager(this));

        String teacherId = mAuth.getCurrentUser().getUid();
        Query query = db.collection("Classes").whereEqualTo("teacherId", teacherId);
        FirestoreRecyclerOptions<Class> options = new FirestoreRecyclerOptions.Builder<Class>()
                .setQuery(query, Class.class)
                .build();

        adapter = new ClassAdapter(options, this);
        rvClasses.setAdapter(adapter);

        FloatingActionButton fabAddClass = findViewById(R.id.fab_add_class);
        fabAddClass.setOnClickListener(v -> startActivity(new Intent(this, AddClassActivity.class)));

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    public void onEditClass(String classId, Class classData) {
        Intent intent = new Intent(this, EditClassActivity.class);
        intent.putExtra("CLASS_ID", classId);
        intent.putExtra("CLASS_NAME", classData.getName());
        startActivity(intent);
    }

    @Override
    public void onAssignStudents(String classId) {
        startActivity(new Intent(this, AssignStudentsActivity.class)
                .putExtra("CLASS_ID", classId));
    }

    @Override
    public void onDeleteClass(String classId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete this class?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("Classes").document(classId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {})
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}