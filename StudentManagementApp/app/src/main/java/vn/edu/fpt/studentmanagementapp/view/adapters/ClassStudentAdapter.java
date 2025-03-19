package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Student;

public class ClassStudentAdapter extends RecyclerView.Adapter<ClassStudentAdapter.StudentViewHolder> {
    private List<Student> students = new ArrayList<>();

    public void setStudents(List<Student> students) {
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
        Student student = students.get(position);
        holder.tvName.setText(student.getName());
        holder.tvCode.setText(student.getStudentCode());
        holder.tvEmail.setText(student.getUserId() != null ? "User ID: " + student.getUserId() : "No user account");
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvEmail;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_student_name);
            tvCode = itemView.findViewById(R.id.tv_student_code);
            tvEmail = itemView.findViewById(R.id.tv_student_email);
        }
    }
}