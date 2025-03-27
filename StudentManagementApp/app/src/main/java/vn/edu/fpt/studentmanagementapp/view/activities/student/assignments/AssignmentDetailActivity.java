package vn.edu.fpt.studentmanagementapp.view.activities.student.assignments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Assignment;
import vn.edu.fpt.studentmanagementapp.model.Submission;

public class AssignmentDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvDueDate, tvPoints, tvDescription, tvAttachmentName;
    private TextInputEditText etSubmissionText;
    private Button btnDownloadFile, btnAttachFile, btnSubmit;
    private View submissionContainer, submittedContainer;
    private TextView tvSubmittedDate, tvSubmissionStatus, tvGrade, tvFeedback, tvSubmittedFileName;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    
    private String assignmentId;
    private String classId;
    private Assignment assignment;
    private Submission submission;
    private Uri selectedFileUri;
    private String fileType;
    
    // ActivityResultLauncher for file picking
    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    String fileName = getFileNameFromUri(uri);
                    ((TextView) findViewById(R.id.tv_selected_file)).setText(fileName);
                    
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
        setContentView(R.layout.activity_assignment_detail);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        
        // Get assignment ID from intent
        assignmentId = getIntent().getStringExtra("assignmentId");
        classId = getIntent().getStringExtra("classId");
        
        if (assignmentId == null || classId == null) {
            Toast.makeText(this, "Assignment information is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        initializeViews();
        
        // Load assignment data
        loadAssignmentData();
    }
    
    private void initializeViews() {
        tvTitle = findViewById(R.id.tv_assignment_title);
        tvDueDate = findViewById(R.id.tv_due_date);
        tvPoints = findViewById(R.id.tv_points);
        tvDescription = findViewById(R.id.tv_assignment_description);
        tvAttachmentName = findViewById(R.id.tv_attachment_name);
        
        btnDownloadFile = findViewById(R.id.btn_download_file);
        btnAttachFile = findViewById(R.id.btn_attach_file);
        btnSubmit = findViewById(R.id.btn_submit);
        
        etSubmissionText = findViewById(R.id.et_submission_text);
        
        submissionContainer = findViewById(R.id.submission_container);
        submittedContainer = findViewById(R.id.submitted_container);
        
        tvSubmittedDate = findViewById(R.id.tv_submitted_date);
        tvSubmissionStatus = findViewById(R.id.tv_submission_status);
        tvGrade = findViewById(R.id.tv_grade);
        tvFeedback = findViewById(R.id.tv_feedback);
        tvSubmittedFileName = findViewById(R.id.tv_submitted_file_name);
        
        // Set button click listeners
        btnDownloadFile.setOnClickListener(v -> downloadAssignmentFile());
        btnAttachFile.setOnClickListener(v -> openFilePicker());
        btnSubmit.setOnClickListener(v -> submitAssignment());
    }
    
    private void loadAssignmentData() {
        db.collection("Assignments").document(assignmentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        assignment = documentSnapshot.toObject(Assignment.class);
                        
                        if (assignment != null) {
                            displayAssignmentDetails();
                            checkForExistingSubmission();
                        }
                    } else {
                        Toast.makeText(this, "Assignment not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading assignment: " + e.getMessage(), 
                               Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
    
    private void displayAssignmentDetails() {
        tvTitle.setText(assignment.getTitle());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.US);
        tvDueDate.setText("Due: " + dateFormat.format(assignment.getDueDate()));
        
        tvPoints.setText(assignment.getPossiblePoints() + " points");
        tvDescription.setText(assignment.getDescription());
        
        if (assignment.getFileUrl() != null && !assignment.getFileUrl().isEmpty()) {
            btnDownloadFile.setVisibility(View.VISIBLE);
            tvAttachmentName.setVisibility(View.VISIBLE);
            tvAttachmentName.setText("Attachment: " + getFileNameFromUrl(assignment.getFileUrl()));
        } else {
            btnDownloadFile.setVisibility(View.GONE);
            tvAttachmentName.setVisibility(View.GONE);
        }
    }
    
    private void checkForExistingSubmission() {
        String userId = mAuth.getCurrentUser().getUid();
        
        db.collection("Submissions")
                .whereEqualTo("assignmentId", assignmentId)
                .whereEqualTo("studentId", userId)
                .get()
                .addOnSuccessListener(queryDocuments -> {
                    if (!queryDocuments.isEmpty()) {
                        // Student has already submitted
                        submission = queryDocuments.getDocuments().get(0).toObject(Submission.class);
                        displaySubmissionDetails();
                    } else {
                        // No submission yet
                        submissionContainer.setVisibility(View.VISIBLE);
                        submittedContainer.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking submission status: " + e.getMessage(), 
                               Toast.LENGTH_SHORT).show();
                });
    }
    
    private void displaySubmissionDetails() {
        submissionContainer.setVisibility(View.GONE);
        submittedContainer.setVisibility(View.VISIBLE);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.US);
        tvSubmittedDate.setText("Submitted: " + dateFormat.format(submission.getSubmittedDate()));
        
        if (submission.isLate()) {
            tvSubmissionStatus.setText("Status: Late");
            tvSubmissionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvSubmissionStatus.setText("Status: On time");
            tvSubmissionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        
        if (submission.isGraded()) {
            tvGrade.setVisibility(View.VISIBLE);
            tvFeedback.setVisibility(View.VISIBLE);
            tvGrade.setText("Grade: " + submission.getEarnedPoints() + "/" + assignment.getPossiblePoints());
            tvFeedback.setText("Feedback: " + (submission.getTeacherFeedback() != null ? 
                                            submission.getTeacherFeedback() : "No feedback provided"));
        } else {
            tvGrade.setVisibility(View.GONE);
            tvFeedback.setVisibility(View.GONE);
        }
        
        if (submission.getFileUrl() != null && !submission.getFileUrl().isEmpty()) {
            tvSubmittedFileName.setVisibility(View.VISIBLE);
            tvSubmittedFileName.setText("Submitted file: " + getFileNameFromUrl(submission.getFileUrl()));
        } else {
            tvSubmittedFileName.setVisibility(View.GONE);
        }
    }
    
    private void downloadAssignmentFile() {
        if (assignment != null && assignment.getFileUrl() != null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(assignment.getFileUrl()));
            startActivity(browserIntent);
        }
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
    
    private String getFileNameFromUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String path = uri.getLastPathSegment();
            return path != null ? path.substring(path.lastIndexOf('/') + 1) : "Download file";
        } catch (Exception e) {
            return "Download file";
        }
    }
    
    private void submitAssignment() {
        if(assignment.getDueDate().before(new Date())) {
            Toast.makeText(this, "Cannot submit - assignment is overdue",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String content = etSubmissionText.getText().toString().trim();
        
        if (content.isEmpty() && selectedFileUri == null) {
            Toast.makeText(this, "Please provide text or attach a file", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable submit button to prevent multiple submissions
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");
        
        String userId = mAuth.getCurrentUser().getUid();
        String submissionId = db.collection("Submissions").document().getId();
        
        if (selectedFileUri != null) {
            // Upload file first
            String fileName = UUID.randomUUID().toString();
            StorageReference fileRef = storageRef.child("submissions/" + classId + "/" + 
                                                  assignmentId + "/" + fileName);
            
            fileRef.putFile(selectedFileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    String fileUrl = downloadUri.toString();
                                    createSubmission(submissionId, userId, content, fileUrl, fileType);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AssignmentDetailActivity.this, 
                                               "Failed to get download URL: " + e.getMessage(), 
                                               Toast.LENGTH_SHORT).show();
                                    btnSubmit.setEnabled(true);
                                    btnSubmit.setText("Submit");
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AssignmentDetailActivity.this, 
                                   "Failed to upload file: " + e.getMessage(), 
                                   Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit");
                    });
        } else {
            // No file to upload, create submission directly
            createSubmission(submissionId, userId, content, null, null);
        }
    }
    
    private void createSubmission(String submissionId, String userId, String content, 
                              String fileUrl, String fileType) {
        Date now = new Date();
        boolean isLate = assignment.getDueDate().before(now);
        
        Submission newSubmission = new Submission(submissionId, assignmentId, classId, userId, 
                                              content, fileUrl, fileType);
        newSubmission.setLate(isLate);
        newSubmission.setSubmittedDate(now);

        // Save to Firestore
        db.collection("Submissions").document(submissionId)
                .set(newSubmission)
                .addOnSuccessListener(aVoid -> {
                    // No longer updating Assignment document directly
                    Toast.makeText(AssignmentDetailActivity.this, 
                        "Assignment submitted successfully", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Refresh UI
                    submission = newSubmission;
                    displaySubmissionDetails();
                    
                    // Set result so the list activity knows to refresh
                    setResult(RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AssignmentDetailActivity.this, 
                               "Error submitting assignment: " + e.getMessage(), 
                               Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit");
                });
    }
}
