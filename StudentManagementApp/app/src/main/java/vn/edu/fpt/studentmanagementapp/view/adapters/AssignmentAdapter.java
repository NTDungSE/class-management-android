package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
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
    private FirebaseFirestore db;

    public AssignmentAdapter(List<Assignment> assignments, boolean isTeacher, OnAssignmentClickListener listener) {
        this.assignments = assignments;
        this.isTeacher = isTeacher;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    // Add this method to update data
    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
        notifyDataSetChanged(); // Trigger UI refresh
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

        // Format due date
        if (assignment.getDueDate() != null) {
            holder.tvDueDate.setText("Due: " + formatDate(assignment.getDueDate()));
        } else {
            holder.tvDueDate.setText("No due date");
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> listener.onAssignmentClick(assignment));

        // In onBindViewHolder (teacher view)
        if (isTeacher) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            String stats = "Submitted: " + assignment.getSubmittedCount()
                    + " • Graded: " + assignment.getGradedCount();
            holder.tvStatus.setText(stats);
        }
        // In onBindViewHolder (student view)
        if (!isTeacher) {
            String userId = FirebaseAuth.getInstance().getUid();
            db.collection("Submissions")
                    .whereEqualTo("assignmentId", assignment.getAssignmentId())
                    .whereEqualTo("studentId", userId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        String status = "Not submitted";
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Submission submission = queryDocumentSnapshots.getDocuments().get(0).toObject(Submission.class);
                            status = submission.isGraded() ? "Graded" : "Submitted";
                        }
                        holder.tvStatus.setText(status);
                    });
        }
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