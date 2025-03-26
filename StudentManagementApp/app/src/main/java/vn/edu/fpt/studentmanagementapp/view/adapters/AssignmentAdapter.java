// AssignmentAdapter.java (New)
package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Assignment;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    public interface OnAssignmentClickListener {
        void onAssignmentClick(Assignment assignment);
    }

    private List<Assignment> assignments;
    private OnAssignmentClickListener listener;

    public AssignmentAdapter(List<Assignment> assignments, OnAssignmentClickListener listener) {
        this.assignments = assignments;
        this.listener = listener;
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

        // Format due date properly
        if(assignment.getDueDate() != null) {
            holder.tvDueDate.setText("Due: " + formatDate(assignment.getDueDate()));
        } else {
            holder.tvDueDate.setText("No due date");
        }

        // Add click listener
        holder.itemView.setOnClickListener(v -> {
            if(listener != null) {
                listener.onAssignmentClick(assignment);
            }
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
        TextView tvTitle, tvDueDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
        }
    }
}