package vn.edu.fpt.studentmanagementapp.view.activities.teacher.assignments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Assignment;

public class CreateAssignmentActivity extends AppCompatActivity {
    private TextInputEditText etTitle, etDescription, etPoints;
    private Button btnDatePicker, btnTimePicker, btnAttachFile, btnCancel, btnSave;
    private TextView tvSelectedDateTime, tvFileName;
    private Switch switchPublish;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    
    private Calendar calendar;
    private String classId;
    private Uri selectedFileUri;
    private String fileType;
    
    // ActivityResultLauncher for file picking
    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    String fileName = getFileNameFromUri(uri);
                    tvFileName.setText(fileName);
                    
                    // Determine file type
                    String mimeType = getContentResolver().getType(uri);
                    if (mimeType != null) {
                        if (mimeType.startsWith("image/")) {
                            fileType = "image";
                        } else if (mimeType.startsWith("application/pdf")) {
                            fileType = "pdf";
                        } else if (mimeType.startsWith("application/msword") || 
                                  mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml")) {
                            fileType = "doc";
                        } else {
                            fileType = "other";
                        }
                    }
                }
            }
    );
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_assignment);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        
        // Get classId from intent
        classId = getIntent().getStringExtra("classId");
        if (classId == null) {
            Toast.makeText(this, "Class ID is required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize calendar
        calendar = Calendar.getInstance();
        
        // Initialize UI components
        etTitle = findViewById(R.id.et_assignment_title);
        etDescription = findViewById(R.id.et_assignment_description);
        etPoints = findViewById(R.id.et_points);
        btnDatePicker = findViewById(R.id.btn_date_picker);
        btnTimePicker = findViewById(R.id.btn_time_picker);
        btnAttachFile = findViewById(R.id.btn_attach_file);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        tvSelectedDateTime = findViewById(R.id.tv_selected_datetime);
        tvFileName = findViewById(R.id.tv_file_name);
        switchPublish = findViewById(R.id.switch_publish);
        
        // Set up button click listeners
        setupButtonListeners();
    }
    
    private void setupButtonListeners() {
        btnDatePicker.setOnClickListener(v -> showDatePicker());
        btnTimePicker.setOnClickListener(v -> showTimePicker());
        btnAttachFile.setOnClickListener(v -> openFilePicker());
        
        btnCancel.setOnClickListener(v -> finish());
        
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveAssignment();
            }
        });
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeLabel();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateDateTimeLabel();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }
    
    private void updateDateTimeLabel() {
        String format = "EEEE, MMMM d, yyyy 'at' h:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        tvSelectedDateTime.setText(sdf.format(calendar.getTime()));
    }
    
    private void openFilePicker() {
        filePickerLauncher.launch("*/*");
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result != null ? result : "Selected file";
    }
    
    private boolean validateInputs() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String pointsStr = etPoints.getText().toString().trim();
        
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return false;
        }
        
        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            return false;
        }
        
        if (pointsStr.isEmpty()) {
            etPoints.setError("Points are required");
            return false;
        }
        
        if (tvSelectedDateTime.getText().toString().equals("No date selected")) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void saveAssignment() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        int points = Integer.parseInt(etPoints.getText().toString().trim());
        boolean publishImmediately = switchPublish.isChecked();
        Date dueDate = calendar.getTime();
        
        // Show loading state
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");
        
        if (selectedFileUri != null) {
            // Upload file to Firebase Storage first
            uploadFileAndCreateAssignment(title, description, points, dueDate, publishImmediately);
        } else {
            // Create assignment without file
            createAssignmentInFirestore(title, description, points, dueDate, publishImmediately, null, null);
        }
    }
    
    private void uploadFileAndCreateAssignment(String title, String description, int points, 
                                             Date dueDate, boolean publishImmediately) {
        String fileName = UUID.randomUUID().toString();
        StorageReference fileRef = storageRef.child("assignments/" + classId + "/" + fileName);
        
        fileRef.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    fileRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                String fileUrl = downloadUri.toString();
                                // Now create the assignment with the file URL
                                createAssignmentInFirestore(title, description, points, dueDate, 
                                                         publishImmediately, fileUrl, fileType);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(CreateAssignmentActivity.this, 
                                           "Failed to get download URL: " + e.getMessage(), 
                                           Toast.LENGTH_SHORT).show();
                                btnSave.setEnabled(true);
                                btnSave.setText("Save");
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateAssignmentActivity.this, 
                               "Failed to upload file: " + e.getMessage(), 
                               Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                });
    }
    
    private void createAssignmentInFirestore(String title, String description, int points, 
                                          Date dueDate, boolean publishImmediately, 
                                          String fileUrl, String fileType) {
        // Generate a new document ID
        String assignmentId = db.collection("Assignments").document().getId();
        
        // Create assignment object
        Assignment assignment = new Assignment(
                assignmentId, title, description, classId, dueDate, points);
        
        // Set additional properties
        assignment.setPublished(publishImmediately);
        if (fileUrl != null) {
            assignment.setFileUrl(fileUrl);
            assignment.setFileType(fileType);
        }
        
        // Save to Firestore
        db.collection("Assignments").document(assignmentId)
                .set(assignment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateAssignmentActivity.this, 
                               "Assignment created successfully", 
                               Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateAssignmentActivity.this, 
                               "Error creating assignment: " + e.getMessage(), 
                               Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                });
    }
}
