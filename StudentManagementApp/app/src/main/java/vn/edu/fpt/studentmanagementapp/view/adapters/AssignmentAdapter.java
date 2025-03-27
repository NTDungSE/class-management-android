package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Assignment;
import vn.edu.fpt.studentmanagementapp.model.Submission;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    public interface OnAssignmentClickListener {
        void onAssignmentClick(Assignment assignment);
    }

    private List<Assignment> assignments;
    private final OnAssignmentClickListener listener;
    private final boolean isTeacher;

    public AssignmentAdapter(List<Assignment> assignments, boolean isTeacher, OnAssignmentClickListener listener) {
        this.assignments = assignments;
        this.isTeacher = isTeacher;
        this.listener = listener;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Assignment assignment = assignments.get(position);
        holder.tvTitle.setText(assignment.getTitle());

        if (assignment.getDueDate() != null) {
            holder.tvDueDate.setText("Due: " + formatDate(assignment.getDueDate()));
        } else {
            holder.tvDueDate.setText("No due date");
        }

        holder.itemView.setOnClickListener(v -> listener.onAssignmentClick(assignment));

        if (isTeacher) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            // For teachers, we'll load the submission counts separately
            loadSubmissionCounts(holder, assignment);
        } else {
            // For students, check their own submission status
            loadStudentSubmissionStatus(holder, assignment);
        }
    }

    private void loadSubmissionCounts(ViewHolder holder, Assignment assignment) {
        holder.tvStatus.setText("Loading...");
        
        FirebaseFirestore.getInstance()
            .collection("Submissions")
            .whereEqualTo("assignmentId", assignment.getAssignmentId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int submitted = querySnapshot.size();
                int graded = 0;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Submission submission = doc.toObject(Submission.class);
                    if (submission != null && submission.isGraded()) {
                        graded++;
                    }
                }
                
                String stats = "Submitted: " + submitted + " • Graded: " + graded;
                holder.tvStatus.setText(stats);
            })
            .addOnFailureListener(e -> {
                holder.tvStatus.setText("Error loading submission status");
            });
    }

    private void loadStudentSubmissionStatus(ViewHolder holder, Assignment assignment) {
        String userId = FirebaseAuth.getInstance().getUid();
        
        holder.tvStatus.setText("Checking status...");
        
        FirebaseFirestore.getInstance()
            .collection("Submissions")
            .whereEqualTo("assignmentId", assignment.getAssignmentId())
            .whereEqualTo("studentId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                String status = "Not submitted";
                
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    Submission submission = doc.toObject(Submission.class);
                    
                    if (submission != null) {
                        if (submission.isGraded()) {
                            status = "Graded";
                        } else {
                            status = "Submitted";
                        }
                    }
                }
                
                holder.tvStatus.setText(status);
            })
            .addOnFailureListener(e -> {
                holder.tvStatus.setText("Error");
            });
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDueDate, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}