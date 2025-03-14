package vn.edu.fpt.studentmanagementapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditClassActivity extends AppCompatActivity {
    private EditText etClassName;
    private FirebaseFirestore db;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class); // New layout needed
        db = FirebaseFirestore.getInstance();

        etClassName = findViewById(R.id.et_class_name);
        Button btnUpdate = findViewById(R.id.btn_update);
        Button btnCancel = findViewById(R.id.btn_cancel);

        // Get class data from intent
        classId = getIntent().getStringExtra("CLASS_ID");
        String name = getIntent().getStringExtra("CLASS_NAME");

        etClassName.setText(name);

        btnUpdate.setOnClickListener(v -> {
            String updatedName = etClassName.getText().toString().trim();

            if (updatedName.isEmpty()) {
                Toast.makeText(this, "Please enter a class name", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updatedClass = new HashMap<>();
            updatedClass.put("name", updatedName);

            db.collection("Classes").document(classId)
                    .update(updatedClass)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Class updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}