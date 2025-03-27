package vn.edu.fpt.studentmanagementapp.view.activities.teacher.assignments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.adapter.SubmissionAdapter;
import vn.edu.fpt.studentmanagementapp.model.Assignment;
import vn.edu.fpt.studentmanagementapp.model.Student;
import vn.edu.fpt.studentmanagementapp.model.Submission;

public class SubmissionListActivity extends AppCompatActivity implements SubmissionAdapter.OnSubmissionClickListener {
    private RecyclerView recyclerView;
    private SubmissionAdapter adapter;
    private TextView tvTitle, tvDueDate, tvPoints, tvSubmissionStats;
    private View emptyView;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private String classId;
    private String assignmentId;
    private Assignment assignment;
    private List<Submission> submissions;
    private Map<String, Student> studentMap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_list);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // Get data from intent
        classId = getIntent().getStringExtra("classId");
        assignmentId = getIntent().getStringExtra("assignmentId");
        
        if (classId == null || assignmentId == null) {
            Toast.makeText(this, "Missing assignment information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        initializeViews();
        
        // Load data
        loadAssignmentData();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh submissions data when returning to this screen
        if (assignment != null) {
            loadSubmissions();
        }
    }
    
    private void initializeViews() {
        tvTitle = findViewById(R.id.tv_assignment_title);
        tvDueDate = findViewById(R.id.tv_due_date);
        tvPoints = findViewById(R.id.tv_points);
        tvSubmissionStats = findViewById(R.id.tv_submission_stats);
        emptyView = findViewById(R.id.empty_view);
        
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        submissions = new ArrayList<>();
        studentMap = new HashMap<>();
        adapter = new SubmissionAdapter(submissions, studentMap, this);
        recyclerView.setAdapter(adapter);
    }
    
    private void loadAssignmentData() {
        db.collection("Assignments").document(assignmentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        assignment = documentSnapshot.toObject(Assignment.class);
                        
                        if (assignment != null) {
                            displayAssignmentDetails();
                            loadClassStudents();
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
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
        tvDueDate.setText("Due: " + dateFormat.format(assignment.getDueDate()));
        
        tvPoints.setText(assignment.getPossiblePoints() + " points");
    }
    
    private void loadClassStudents() {
        db.collection("Classes").document(classId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, String> enrolledStudents = 
                            (Map<String, String>) documentSnapshot.get("enrolledStudents");
                        
                        if (enrolledStudents != null && !enrolledStudents.isEmpty()) {
                            for (Map.Entry<String, String> entry : enrolledStudents.entrySet()) {
                                if ("enrolled".equals(entry.getValue())) {
                                    loadStudentInfo(entry.getKey());
                                }
                            }
                        }
                        
                        // Load submissions after getting the student list
                        loadSubmissions();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading class data: " + e.getMessage(), 
                               Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadStudentInfo(String studentId) {
        // Skip if it looks like an email address
        if (studentId.contains("@")) {
            return;
        }
        
        db.collection("Users").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("displayName");
                        String email = documentSnapshot.getString("email");
                        
                        if (name != null && email != null) {
                            Student student = new Student(name, email, studentId);
                            studentMap.put(studentId, student);
                            
                            // Update adapter if we have submissions
                            if (!submissions.isEmpty()) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }
    
    private void loadSubmissions() {
        db.collection("Submissions")
                .whereEqualTo("assignmentId", assignmentId)
                .whereEqualTo("classId", classId)
                .get()
                .addOnSuccessListener(queryDocuments -> {
                    submissions.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocuments) {
                        Submission submission = document.toObject(Submission.class);
                        submissions.add(submission);
                    }
                    
                    // Always sync submission status when loading submissions
                    syncAssignmentSubmissionStatus();
                    
                    // Update UI after syncing
                    if (submissions.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading submissions: " + e.getMessage(), 
                               Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateSubmissionStats() {
        int totalStudents = studentMap.size();
        int submitted = submissions.size();
        int graded = 0;
        
        for (Submission submission : submissions) {
            if (submission.isGraded()) {
                graded++;
            }
        }
        
        String stats = "Submitted: " + submitted + "/" + totalStudents + " • Graded: " + graded + "/" + submitted;
        tvSubmissionStats.setText(stats);
    }

    private void syncAssignmentSubmissionStatus() {
        if (assignment == null) return;

        Map<String, String> statusMap = new HashMap<>();

        // Initialize all enrolled students as "not_submitted"
        db.collection("Classes").document(classId)
                .get()
                .addOnSuccessListener(classDoc -> {
                    Map<String, String> enrolledStudents = (Map<String, String>) classDoc.get("enrolledStudents");
                    if (enrolledStudents != null) {
                        for (String studentId : enrolledStudents.keySet()) {
                            if (!studentId.contains("@")) { // Skip email-based entries
                                statusMap.put(studentId, "not_submitted");
                            }
                        }
                    }

                    // Update status based on actual submissions
                    for (Submission submission : submissions) {
                        String status = submission.isGraded() ? "graded" : "submitted";
                        statusMap.put(submission.getStudentId(), status);
                    }

                    // Update the Assignment's submissionStatus
                    assignment.setSubmissionStatus(statusMap);

                    // Save to Firestore
                    db.collection("Assignments").document(assignmentId)
                            .update("submissionStatus", statusMap)
                            .addOnSuccessListener(aVoid -> {
                                // Update UI counts
                                updateSubmissionStats();
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Sync failed", Toast.LENGTH_SHORT).show());
                });
    }

    @Override
    public void onSubmissionClick(Submission submission) {
//        Intent intent = new Intent(this, GradeSubmissionActivity.class);
//        intent.putExtra("submissionId", submission.getSubmissionId());
//        intent.putExtra("assignmentId", assignmentId);
//        intent.putExtra("classId", classId);
//        startActivity(intent);
    }
}
