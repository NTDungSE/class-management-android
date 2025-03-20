package vn.edu.fpt.studentmanagementapp.view.activities.teacher.classes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;
import vn.edu.fpt.studentmanagementapp.model.Student;

public class ClassInviteActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String classId;
    private String classCode;
    private Map<String, String> enrolledStudents = new HashMap<>();

    private TextView tvClassCode;
    private Button btnCopyCode;
    private EditText etEmailInvite;
    private Button btnSendInvite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_invite);
        db = FirebaseFirestore.getInstance();

        classId = getIntent().getStringExtra("CLASS_ID");
        String className = getIntent().getStringExtra("CLASS_NAME");

        if (classId == null) {
            Toast.makeText(this, "Error: Class ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar(className);
        initializeViews();
        setupListeners();
        loadClassData();
    }

    private void setupToolbar(String className) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Invite Students");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TextView tvClassName = findViewById(R.id.tv_class_name);
        tvClassName.setText(className);
    }

    private void initializeViews() {
        tvClassCode = findViewById(R.id.tv_class_code);
        btnCopyCode = findViewById(R.id.btn_copy_code);
        etEmailInvite = findViewById(R.id.et_email_invite);
        btnSendInvite = findViewById(R.id.btn_send_invite);
    }

    private void setupListeners() {
        btnCopyCode.setOnClickListener(v -> copyClassCode());
        btnSendInvite.setOnClickListener(v -> handleEmailInvite());
    }

    private void copyClassCode() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Class Code", classCode);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Class code copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void handleEmailInvite() {
        String email = etEmailInvite.getText().toString().trim();
        if (validateEmail(email)) {
            sendInvitation(email);
            etEmailInvite.setText("");
        }
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (enrolledStudents.containsKey(email)) {
            Toast.makeText(this, "This email is already invited", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void loadClassData() {
        db.collection("Classes").document(classId).get()
                .addOnSuccessListener(doc -> {
                    Class classData = doc.toObject(Class.class);
                    if (classData != null) {
                        classCode = classData.getClassCode();
                        tvClassCode.setText(classCode);
                        enrolledStudents = classData.getEnrolledStudents() != null ?
                                new HashMap<>(classData.getEnrolledStudents()) : new HashMap<>();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading class data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendInvitation(String email) {
        enrolledStudents.put(email, "invited");
        db.collection("Classes").document(classId)
                .update("enrolledStudents", enrolledStudents)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Invitation sent to " + email, Toast.LENGTH_SHORT).show();
                    createOrUpdateStudentRecord(email);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createOrUpdateStudentRecord(String email) {
        db.collection("Students")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Student newStudent = new Student("Invited Student", email);
                        Map<String, String> classes = new HashMap<>();
                        classes.put(classId, "invited");
                        newStudent.setEnrolledClasses(classes);
                        db.collection("Students").add(newStudent);
                    } else {
                        String studentId = querySnapshot.getDocuments().get(0).getId();
                        updateStudentClassEnrollment(studentId);
                    }
                });
    }

    private void updateStudentClassEnrollment(String studentId) {
        db.collection("Students").document(studentId).get()
                .addOnSuccessListener(doc -> {
                    Student student = doc.toObject(Student.class);
                    if (student != null) {
                        Map<String, String> classes = student.getEnrolledClasses() != null ?
                                new HashMap<>(student.getEnrolledClasses()) : new HashMap<>();
                        classes.put(classId, "invited");
                        db.collection("Students").document(studentId)
                                .update("enrolledClasses", classes);
                    }
                });
    }
}