package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Student;

public class ClassStudentAdapter extends RecyclerView.Adapter<ClassStudentAdapter.StudentViewHolder> {
    private List<StudentWithStatus> students = new ArrayList<>();
    private boolean isTeacher = false;

    public static class StudentWithStatus {
        public Student student;
        public String status;

        public StudentWithStatus(Student student, String status) {
            this.student = student;
            this.status = status;
        }
    }

    public interface OnStudentRemovedListener {
        void onStudentRemoved(String identifier);
    }
    private OnStudentRemovedListener removalListener;
    public void setOnStudentRemovedListener(OnStudentRemovedListener listener) {
        this.removalListener = listener;
    }

    public void setTeacherRole(boolean isTeacher) {
        this.isTeacher = isTeacher;
    }

    public void setStudents(List<Student> students) {
        this.students.clear();
        for (Student student : students) {
            this.students.add(new StudentWithStatus(student, "enrolled"));
        }
        notifyDataSetChanged();
    }

    public void setStudentsWithStatus(List<StudentWithStatus> students) {
        this.students = new ArrayList<>(students);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentWithStatus studentWithStatus = students.get(position);
        Student student = studentWithStatus.student;
        String status = studentWithStatus.status;

        holder.tvName.setText(student.getName());
        holder.tvEmail.setText(student.getEmail());
        holder.tvStatus.setText(capitalizeFirstLetter(status));


        if (student.isRegistered() && isTeacher) {
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> {
                if (removalListener != null) {
                    removalListener.onStudentRemoved(student.getUserId());
                }
            });
        } else {
            holder.btnRemove.setVisibility(View.GONE);
        }

        // Display enrollment status
        holder.tvStatus.setText(capitalizeFirstLetter(status));

        // Set appropriate background color based on status
        int colorResId = status.equals("enrolled") ?
                R.color.status_enrolled : R.color.status_invited;
        int color = ContextCompat.getColor(holder.itemView.getContext(), colorResId);
        holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvStatus;
        ImageButton btnRemove;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_student_name);
            tvEmail = itemView.findViewById(R.id.tv_student_email);
            tvStatus = itemView.findViewById(R.id.tv_student_status);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}