package vn.edu.fpt.studentmanagementapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddClassActivity extends AppCompatActivity {
    private EditText etClassName;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class); // New layout needed
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etClassName = findViewById(R.id.et_class_name);
        Button btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> {
            String className = etClassName.getText().toString().trim();
            String teacherId = mAuth.getCurrentUser().getUid();

            if (className.isEmpty()) {
                Toast.makeText(this, "Please enter a class name", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> classData = new HashMap<>();
            classData.put("name", className);
            classData.put("teacherId", teacherId);
            classData.put("studentIds", new ArrayList<String>());

            db.collection("Classes")
                    .add(classData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Class added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}