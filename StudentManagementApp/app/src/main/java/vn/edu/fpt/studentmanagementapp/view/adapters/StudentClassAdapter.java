package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;

public class StudentClassAdapter extends RecyclerView.Adapter<StudentClassAdapter.ClassViewHolder> {
    private final List<Class> classList;
    private final ClassActionListener listener;

    public interface ClassActionListener {
        void onViewClassDetails(String classId, Class classData);
    }

    public StudentClassAdapter(List<Class> classList, ClassActionListener listener) {
        this.classList = classList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        Class classData = classList.get(position);
        holder.tvClassName.setText(classData.getName());

        // Show enrolled count if available
        if (classData.getEnrolledStudents() != null) {
            int count = classData.getEnrolledStudentCount();
            holder.tvStudentCount.setText(count + " Students");
            holder.tvStudentCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvStudentCount.setVisibility(View.GONE);
        }

        // Show class code
        holder.tvClassCode.setText("Code: " + classData.getClassCode());

        // Make the whole item clickable to see class details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewClassDetails(classData.getClassId(), classData);
            }
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvStudentCount, tvClassCode;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tv_class_name);
            tvStudentCount = itemView.findViewById(R.id.tv_student_count);
            tvClassCode = itemView.findViewById(R.id.tv_class_code);
        }
    }
}