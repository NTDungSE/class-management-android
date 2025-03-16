package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;
import java.util.List;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Student;

public class AssignStudentAdapter extends FirestoreRecyclerAdapter<Student, AssignStudentAdapter.AssignStudentViewHolder> {
    private final AssignActionListener listener;
    private List<String> assignedStudentIds = new ArrayList<>();

    public interface AssignActionListener {
        void onToggleStudent(String studentId, boolean isAssigned);
    }

    public AssignStudentAdapter(@NonNull FirestoreRecyclerOptions<Student> options, AssignActionListener listener) {
        super(options);
        this.listener = listener;
        setHasStableIds(true);
    }

    public void setAssignedStudentIds(List<String> assignedStudentIds) {
        if (assignedStudentIds == null) {
            this.assignedStudentIds = new ArrayList<>();
        } else {
            this.assignedStudentIds = new ArrayList<>(assignedStudentIds);
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        // This ensures stable IDs based on the document ID
        return getSnapshots().getSnapshot(position).getId().hashCode();
    }

    @Override
    protected void onBindViewHolder(@NonNull AssignStudentViewHolder holder, int position, @NonNull Student student) {
        holder.tvName.setText(student.getName());
        String studentId = student.getUserId() != null ? student.getUserId() : getSnapshots().getSnapshot(position).getId();

        // Prevent CheckBox listener from triggering during binding
        holder.cbAssign.setOnCheckedChangeListener(null);
        holder.cbAssign.setChecked(assignedStudentIds.contains(studentId));

        // Re-add listener after setting checked state
        holder.cbAssign.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleStudent(studentId, isChecked);
            }
        });

        // Make the entire item clickable for better UX
        holder.itemView.setOnClickListener(v -> {
            boolean newState = !holder.cbAssign.isChecked();
            holder.cbAssign.setChecked(newState);
            if (listener != null) {
                listener.onToggleStudent(studentId, newState);
            }
        });
    }

    @NonNull
    @Override
    public AssignStudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assign_student, parent, false);
        return new AssignStudentViewHolder(view);
    }

    static class AssignStudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox cbAssign;

        public AssignStudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_student_name);
            cbAssign = itemView.findViewById(R.id.cb_assign);
        }
    }
}