package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;
import vn.edu.fpt.studentmanagementapp.view.activities.ClassDetailActivity;

public class TeacherClassAdapter extends FirestoreRecyclerAdapter<Class, TeacherClassAdapter.ClassViewHolder> {
    private final ClassActionListener listener;

    public interface ClassActionListener {
        void onEditClass(String classId, Class classData);
        void onManageStudents(String classId, Class classData);
        void onDeleteClass(String classId);
        void onCreateAssignment(String classId, Class classData);
    }

    public TeacherClassAdapter(@NonNull FirestoreRecyclerOptions<Class> options, ClassActionListener listener) {
        super(options);
        this.listener = listener;
        // Enable stable IDs to help RecyclerView manage view recycling
       setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        // Use the document ID as a unique and stable identifier
        try {
            return getSnapshots().getSnapshot(position).getId().hashCode();
        } catch (IndexOutOfBoundsException e) {
            // Fallback to a default no ID if position is invalid
            return RecyclerView.NO_ID;
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull ClassViewHolder holder, int position, @NonNull Class classData) {
        try {
            // Safely get the class ID
            String classId = getSnapshots().getSnapshot(position).getId();

            holder.tvName.setText(classData.getName());

            // Show enrolled count if available
            if (classData.getEnrolledStudents() != null) {
                int count = classData.getEnrolledStudentCount() + classData.getInvitedStudentCount();
                holder.tvStudentCount.setText(count + " Students");
                holder.tvStudentCount.setVisibility(View.VISIBLE);
            } else {
                holder.tvStudentCount.setVisibility(View.GONE);
            }

            // Make the whole item clickable to see class details
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ClassDetailActivity.class);
                intent.putExtra("CLASS_ID", classId);
                intent.putExtra("CLASS_NAME", classData.getName());
                intent.putExtra("IS_TEACHER", true);
                v.getContext().startActivity(intent);
            });

            holder.btnAssignments.setOnClickListener(v -> {
                if (listener != null) listener.onCreateAssignment(classId, classData);
            });

            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClass(classId, classData);
            });

            holder.btnManage.setOnClickListener(v -> {
                if (listener != null) listener.onManageStudents(classId, classData);
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClass(classId);
            });
        } catch (IndexOutOfBoundsException e) {
            // Log or handle the exception if needed
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_class, parent, false);
        return new ClassViewHolder(view);
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStudentCount;
        ImageButton btnEdit, btnManage, btnDelete, btnAssignments;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_class_name);
            tvStudentCount = itemView.findViewById(R.id.tv_student_count);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnManage = itemView.findViewById(R.id.btn_manage);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnAssignments = itemView.findViewById(R.id.btn_assignments);
        }
    }
}