package vn.edu.fpt.studentmanagementapp.view.activities.teacher.assignments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Assignment;
import vn.edu.fpt.studentmanagementapp.view.adapters.AssignmentAdapter;

public class AssignmentListActivity extends AppCompatActivity
        implements AssignmentAdapter.OnAssignmentClickListener {

    private RecyclerView recyclerView;
    private AssignmentAdapter adapter;
    private String classId;
    private LinearLayout tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_list);

        classId = getIntent().getStringExtra("CLASS_ID");
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvEmpty = findViewById(R.id.empty_view);


        FirebaseFirestore.getInstance().collection("Assignments")
                .whereEqualTo("classId", classId)
                .get()
                .addOnSuccessListener(query -> {
                    List<Assignment> assignments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Assignment assignment = doc.toObject(Assignment.class);
                        assignment.setAssignmentId(doc.getId()); // Important: Set document ID
                        assignments.add(assignment);
                    }

                    if(assignments.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    adapter = new AssignmentAdapter(assignments, this);
                    recyclerView.setAdapter(adapter);
                });
    }

    @Override
    public void onAssignmentClick(Assignment assignment) {
        Intent intent = new Intent(this, SubmissionListActivity.class);
        intent.putExtra("CLASS_ID", classId);
        intent.putExtra("ASSIGNMENT_ID", assignment.getAssignmentId());
        startActivity(intent);
    }
}