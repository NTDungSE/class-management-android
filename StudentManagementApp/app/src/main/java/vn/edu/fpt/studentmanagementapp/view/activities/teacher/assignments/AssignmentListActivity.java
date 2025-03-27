package vn.edu.fpt.studentmanagementapp.view.activities.teacher.assignments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Assignment;
import vn.edu.fpt.studentmanagementapp.model.Submission;
import vn.edu.fpt.studentmanagementapp.view.activities.student.assignments.AssignmentDetailActivity;
import vn.edu.fpt.studentmanagementapp.view.adapters.AssignmentAdapter;

public class AssignmentListActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private String classId;
    private boolean isTeacher;
    private FloatingActionButton fabCreateAssignment;
    private AssignmentAdapter adapter;
    private static final int ASSIGNMENT_DETAIL_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_list);

        db = FirebaseFirestore.getInstance();
        classId = getIntent().getStringExtra("CLASS_ID");
        isTeacher = getIntent().getBooleanExtra("IS_TEACHER", false);

        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);
        fabCreateAssignment = findViewById(R.id.fab_create_assignment);

        setupRecyclerView();

        if (isTeacher) {
            fabCreateAssignment.setVisibility(View.VISIBLE);
            fabCreateAssignment.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateAssignmentActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            });
        }

        loadAssignments();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new AssignmentAdapter(new ArrayList<>(), isTeacher, assignment -> {
            if (isTeacher) {
                Intent intent = new Intent(this, SubmissionListActivity.class);
                intent.putExtra("classId", classId);
                intent.putExtra("assignmentId", assignment.getAssignmentId());
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, AssignmentDetailActivity.class);
                intent.putExtra("classId", classId);
                intent.putExtra("assignmentId", assignment.getAssignmentId());
                startActivityForResult(intent, ASSIGNMENT_DETAIL_REQUEST_CODE);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ASSIGNMENT_DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            // Force refresh the assignment list
            adapter.notifyDataSetChanged();
        }
    }

    private void loadAssignments() {
        Query baseQuery = db.collection("Assignments")
                .whereEqualTo("classId", classId);

        if (!isTeacher) {
            baseQuery = baseQuery.whereEqualTo("published", true);
        }

        baseQuery.addSnapshotListener((value, error) -> {
            if (error != null) return;

            List<Assignment> assignments = new ArrayList<>();
            for (QueryDocumentSnapshot doc : value) {
                Assignment assignment = doc.toObject(Assignment.class);
                assignment.setAssignmentId(doc.getId());
                assignments.add(assignment);
                
                // For teachers, check and update submission statuses
                if (isTeacher) {
                    updateSubmissionStatus(assignment);
                }
            }
            updateUI(assignments);
        });
    }

    // Add this new method to update submission status for each assignment
    private void updateSubmissionStatus(Assignment assignment) {
        db.collection("Submissions")
            .whereEqualTo("assignmentId", assignment.getAssignmentId())
            .whereEqualTo("classId", classId)
            .get()
            .addOnSuccessListener(queryDocuments -> {
                if (!queryDocuments.isEmpty()) {
                    Map<String, String> statusMap = assignment.getSubmissionStatus();
                    if (statusMap == null) {
                        statusMap = new HashMap<>();
                    }
                    
                    for (DocumentSnapshot doc : queryDocuments.getDocuments()) {
                        Submission submission = doc.toObject(Submission.class);
                        if (submission != null) {
                            String status = submission.isGraded() ? "graded" : "submitted";
                            statusMap.put(submission.getStudentId(), status);
                        }
                    }
                    
                    // Update the assignment in Firestore
                    db.collection("Assignments")
                        .document(assignment.getAssignmentId())
                        .update("submissionStatus", statusMap)
                        .addOnSuccessListener(aVoid -> adapter.notifyDataSetChanged());
                }
            });
    }

    private void updateUI(List<Assignment> assignments) {
        if (assignments.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.setAssignments(assignments);
        }
    }
}