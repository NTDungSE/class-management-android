package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Assignment;

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
            Map<String, String> statusMap = assignment.getSubmissionStatus();
            int submitted = 0;
            int graded = 0;
            if (statusMap != null) {
                for (String status : statusMap.values()) {
                    if ("submitted".equals(status) || "graded".equals(status)) {
                        submitted++;
                    }
                    if ("graded".equals(status)) {
                        graded++;
                    }
                }
            }
            String stats = "Submitted: " + submitted + " • Graded: " + graded;
            holder.tvStatus.setText(stats);
        } else {
            String userId = FirebaseAuth.getInstance().getUid();
            Map<String, String> statusMap = assignment.getSubmissionStatus();
            String status = "Not submitted";
            if (statusMap != null && statusMap.containsKey(userId)) {
                String userStatus = statusMap.get(userId);
                if ("submitted".equals(userStatus)) {
                    status = "Submitted";
                } else if ("graded".equals(userStatus)) {
                    status = "Graded";
                }
            }
            holder.tvStatus.setText(status);
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