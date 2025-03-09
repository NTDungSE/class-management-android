package vn.edu.fpt.studentmanagementapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddStudentActivity extends AppCompatActivity {
    private EditText etName, etClass, etCode;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_name);
        etClass = findViewById(R.id.et_class);
        etCode = findViewById(R.id.et_code);
        Button btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String className = etClass.getText().toString().trim();
            String code = etCode.getText().toString().trim();

            Map<String, Object> student = new HashMap<>();
            student.put("name", name);
            student.put("className", className);
            student.put("studentCode", code);

            db.collection("Students")
                    .add(student)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
