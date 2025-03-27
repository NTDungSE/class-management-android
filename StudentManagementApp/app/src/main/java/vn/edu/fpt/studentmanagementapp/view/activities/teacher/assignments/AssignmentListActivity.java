package vn.edu.fpt.studentmanagementapp.view.activities.teacher.assignments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Assignment;
import vn.edu.fpt.studentmanagementapp.view.activities.student.assignments.AssignmentDetailActivity;
import vn.edu.fpt.studentmanagementapp.view.adapters.AssignmentAdapter;

public class AssignmentListActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private String classId;
    private boolean isTeacher;
    private FloatingActionButton fabCreateAssignment;
    private AssignmentAdapter adapter; // Single adapter instance

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_list);

        db = FirebaseFirestore.getInstance();
        classId = getIntent().getStringExtra("CLASS_ID");
        isTeacher = getIntent().getBooleanExtra("IS_TEACHER", false);

        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);
        fabCreateAssignment = findViewById(R.id.fab_create_assignment);

        setupRecyclerView(); // Initialize adapter once

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
        // Initialize adapter with empty list
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
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
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
                assignment.setAssignmentId(doc.getId()); // Ensure ID is set
                assignments.add(assignment);
            }
            if (isTeacher) {
                loadSubmissionCounts(assignments);
            } else {
                updateUI(assignments);
            }
        });
    }

    private void loadSubmissionCounts(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            db.collection("Submissions")
                    .whereEqualTo("assignmentId", assignment.getAssignmentId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int submittedCount = queryDocumentSnapshots.size();
                        int gradedCount = 0;

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (doc.getBoolean("isGraded")) gradedCount++;
                        }

                        assignment.setSubmittedCount(submittedCount);
                        assignment.setGradedCount(gradedCount);
                        adapter.notifyDataSetChanged();
                    });
        }
        updateUI(assignments);
    }

    private void updateUI(List<Assignment> assignments) {
        if (assignments.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            // Update the existing adapter's data
            adapter.setAssignments(assignments);
        }
    }
}