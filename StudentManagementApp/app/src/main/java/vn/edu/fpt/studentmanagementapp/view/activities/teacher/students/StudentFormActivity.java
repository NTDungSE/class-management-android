package vn.edu.fpt.studentmanagementapp.view.activities.teacher.students;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import vn.edu.fpt.studentmanagementapp.R;

public class StudentFormActivity extends AppCompatActivity {
    private EditText etName, etCode, etEmail;

    private Button btnSave, btnCancel;
    private TextView tvTitle;
    private FirebaseFirestore db;
    private Map<String, String> classNameToIdMap; // For class name to ID mapping
    private Map<String, String> classIdToNameMap; // For class ID to name mapping

    // Variables to track if we're editing
    private String studentId;
    private boolean isEditMode = false;
    private String originalUserId;
    private List<String> existingClassIds;

    private AutoCompleteTextView actvClass;
    private List<String> allClassNames = new ArrayList<>();
    private Set<String> selectedClassIds = new HashSet<>();
    private ArrayAdapter<String> classAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_form);
        db = FirebaseFirestore.getInstance();
        classNameToIdMap = new HashMap<>();
        classIdToNameMap = new HashMap<>();
        existingClassIds = new ArrayList<>();

        // Initialize views
        tvTitle = findViewById(R.id.tv_form_title);
        etName = findViewById(R.id.et_name);
        etCode = findViewById(R.id.et_code);
        etEmail = findViewById(R.id.et_email);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        actvClass = findViewById(R.id.actv_class);

        setupClassSelector();

        // Check if we're in edit mode
        studentId = getIntent().getStringExtra("STUDENT_ID");
        isEditMode = (studentId != null);

        // Configure UI based on mode
        if (isEditMode) {
            tvTitle.setText(R.string.edit_student);
            btnSave.setText(R.string.update);
            btnCancel.setVisibility(View.VISIBLE);

            // Fill form with student data
            String name = getIntent().getStringExtra("STUDENT_NAME");
            String code = getIntent().getStringExtra("STUDENT_CODE");
            originalUserId = getIntent().getStringExtra("USER_ID");

            etName.setText(name);
            etCode.setText(code);

            // For edit mode, retrieve the existing class IDs
            if (studentId != null) {
                db.collection("Students").document(studentId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                List<String> classIds = (List<String>) documentSnapshot.get("classIds");
                                if (classIds != null && !classIds.isEmpty()) {
                                    existingClassIds = new ArrayList<>(classIds);
                                    fetchExistingClassNames(classIds);
                                }
                            }
                        });
            }

            // Load email if user ID exists
            if (originalUserId != null && !originalUserId.isEmpty()) {
                db.collection("Users").document(originalUserId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String email = documentSnapshot.getString("email");
                                if (email != null) {
                                    etEmail.setText(email);
                                }
                            }
                        });
            }
        } else {
            tvTitle.setText(R.string.add_student);
            btnSave.setText(R.string.save);
            btnCancel.setVisibility(View.GONE);
        }

        // Load available classes for dropdown
        loadClassesForDropdown();

        // Set up button listeners
        btnSave.setOnClickListener(v -> saveStudent());

        if (btnCancel.getVisibility() == View.VISIBLE) {
            btnCancel.setOnClickListener(v -> finish());
        }
    }

    private void setupClassSelector() {
        // Initialize adapter
        classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allClassNames);
        actvClass.setAdapter(classAdapter);

        // Set click listener
        actvClass.setOnClickListener(v -> showClassSelectionDialog());
    }

    private void showClassSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Classes");

        // Convert class names array to boolean array for selections
        boolean[] checkedItems = new boolean[allClassNames.size()];
        final List<String> selectedClasses = new ArrayList<>();

        for (int i = 0; i < allClassNames.size(); i++) {
            String className = allClassNames.get(i);
            if (selectedClassIds.contains(classNameToIdMap.get(className))) {
                checkedItems[i] = true;
                selectedClasses.add(className);
            }
        }

        builder.setMultiChoiceItems(allClassNames.toArray(new String[0]), checkedItems,
                (dialog, which, isChecked) -> {
                    String className = allClassNames.get(which);
                    if (isChecked) {
                        selectedClasses.add(className);
                    } else {
                        selectedClasses.remove(className);
                    }
                });

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Update selected classes
            selectedClassIds.clear();
            StringBuilder displayText = new StringBuilder();

            for (String className : selectedClasses) {
                String classId = classNameToIdMap.get(className);
                if (classId != null) {
                    selectedClassIds.add(classId);
                    if (displayText.length() > 0) displayText.append(", ");
                    displayText.append(className);
                }
            }

            actvClass.setText(displayText.toString());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveStudent() {
        String name = etName.getText().toString().trim();
        String studentCode = etCode.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(studentCode)) {
            etCode.setError("Student code is required");
            return;
        }

        // Convert selected class IDs to list
        List<String> selectedClassIdsList = new ArrayList<>(selectedClassIds);

        // Prepare student data
        Map<String, Object> student = new HashMap<>();
        student.put("name", name);
        student.put("studentCode", studentCode);
        student.put("classIds", selectedClassIdsList);

        // Handle email/user association
        if (!TextUtils.isEmpty(email)) {
            // Validate email format
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email format");
                return;
            }

            db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Existing user found
                            String userId = querySnapshot.getDocuments().get(0).getId();
                            student.put("userId", userId);
                            finalizeSave(student, userId);
                        } else {
                            // New user - create record first
                            createUserAndSaveStudent(email, student);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finalizeSave(student, null);
                    });
        } else {
            // No email provided
            if (isEditMode && originalUserId != null) {
                student.put("userId", originalUserId);
            }
            finalizeSave(student, originalUserId);
        }
    }

    private void createUserAndSaveStudent(String email, Map<String, Object> student) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, "tempPassword")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = task.getResult().getUser().getUid();

                        // Create basic user document
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("role", "student");

                        db.collection("Users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    student.put("userId", userId);
                                    finalizeSave(student, userId);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error creating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Error creating user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void finalizeSave(Map<String, Object> student, String userId) {
        if (isEditMode) {
            updateStudent(student);
        } else {
            createStudent(student, userId);
        }
    }

    private void updateStudent(Map<String, Object> student) {
        db.collection("Students").document(studentId)
                .update(student)
                .addOnSuccessListener(aVoid -> {
                    updateClassStudentRelationships(
                            (List<String>) student.get("classIds"),
                            studentId
                    );
                    Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createStudent(Map<String, Object> student, String userId) {
        if (userId != null) {
            // Use user ID as document ID
            db.collection("Students").document(userId)
                    .set(student)
                    .addOnSuccessListener(aVoid -> {
                        updateClassStudentRelationships(
                                (List<String>) student.get("classIds"),
                                userId
                        );
                        Toast.makeText(this, "Student created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Creation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Create new student with generated ID
            db.collection("Students")
                    .add(student)
                    .addOnSuccessListener(documentReference -> {
                        updateClassStudentRelationships(
                                (List<String>) student.get("classIds"),
                                documentReference.getId()
                        );
                        Toast.makeText(this, "Student created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Creation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateClassStudentRelationships(List<String> newClassIds, String studentId) {
        // Remove from old classes
        if (isEditMode) {
            for (String oldClassId : existingClassIds) {
                if (!newClassIds.contains(oldClassId)) {
                    db.collection("Classes").document(oldClassId)
                            .update("studentIds", FieldValue.arrayRemove(studentId));
                }
            }
        }

        // Add to new classes
        if (newClassIds != null) {
            for (String classId : newClassIds) {
                if (!existingClassIds.contains(classId)) {
                    db.collection("Classes").document(classId)
                            .update("studentIds", FieldValue.arrayUnion(studentId));
                }
            }
        }
    }

    private void loadClassesForDropdown() {
        db.collection("Classes")
                .whereEqualTo("teacherId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allClassNames.clear();
                    classNameToIdMap.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String className = doc.getString("name");
                        String classId = doc.getId();
                        if (className != null && !className.isEmpty()) {
                            allClassNames.add(className);
                            classNameToIdMap.put(className, classId);
                        }
                    }

                    classAdapter.notifyDataSetChanged();

                    if (allClassNames.isEmpty()) {
                        showNoClassesWarning();
                    }
                });
    }

    private void showNoClassesWarning() {
        new AlertDialog.Builder(this)
                .setTitle("No Classes Found")
                .setMessage("You need to create classes first before adding students. Create a class now?")
                .setPositiveButton("Create Class", (dialog, which) -> {
                    // Start your Class creation activity
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
    }

    // Helper to display existing classes
    private void fetchExistingClassNames(List<String> classIds) {
        selectedClassIds.clear();
        AtomicInteger counter = new AtomicInteger(0);

        for (String classId : classIds) {
            db.collection("Classes").document(classId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String className = doc.getString("name");
                        if (className != null) {
                            selectedClassIds.add(classId);
                            classNameToIdMap.put(className, classId);
                        }

                        if (counter.incrementAndGet() == classIds.size()) {
                            updateClassSelectionDisplay();
                        }
                    });
        }
    }

    private void updateClassSelectionDisplay() {
        StringBuilder displayText = new StringBuilder();
        for (Map.Entry<String, String> entry : classNameToIdMap.entrySet()) {
            if (selectedClassIds.contains(entry.getValue())) {
                if (displayText.length() > 0) displayText.append(", ");
                displayText.append(entry.getKey());
            }
        }
        actvClass.setText(displayText.toString());
    }
}