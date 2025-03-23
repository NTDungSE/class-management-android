package vn.edu.fpt.studentmanagementapp.view.activities.student;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;

public class StudentClassDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView tvClassName, tvClassCode, tvStudentCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_class_detail);

        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        tvClassName = findViewById(R.id.tv_class_name);
        tvClassCode = findViewById(R.id.tv_class_code);
        tvStudentCount = findViewById(R.id.tv_student_count);

        // Get class ID from intent
        String classId = getIntent().getStringExtra("CLASS_ID");
        String className = getIntent().getStringExtra("CLASS_NAME");

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(className);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load class details
        loadClassDetails(classId);
    }

    private void loadClassDetails(String classId) {
        db.collection("Classes").document(classId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Class classData = documentSnapshot.toObject(Class.class);
                    if (classData != null) {
                        updateUI(classData);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading class details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(Class classData) {
        tvClassName.setText(classData.getName());
        tvClassCode.setText("Class Code: " + classData.getClassCode());

        int enrolledCount = classData.getEnrolledStudentCount();
        tvStudentCount.setText("Students: " + enrolledCount);

        // TODO: Add more class details as needed
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}