package vn.edu.fpt.studentmanagementapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;
import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Student;
import vn.edu.fpt.studentmanagementapp.model.Submission;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.ViewHolder> {
    private final List<Submission> submissions;
    private final Map<String, Student> studentMap;
    private final OnSubmissionClickListener listener;

    public interface OnSubmissionClickListener {
        void onSubmissionClick(Submission submission);
    }

    public SubmissionAdapter(List<Submission> submissions,
                             Map<String, Student> studentMap,
                             OnSubmissionClickListener listener) {
        this.submissions = submissions;
        this.studentMap = studentMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submission, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Submission submission = submissions.get(position);
        Student student = studentMap.get(submission.getStudentId());

        holder.tvStudentName.setText(student != null ? student.getName() : "Unknown Student");
        holder.tvStatus.setText(submission.isGraded() ? "Graded" : "Submitted");
        holder.tvGrade.setText(submission.isGraded() ?
                submission.getEarnedPoints() + " pts" : "-");

        holder.itemView.setOnClickListener(v -> listener.onSubmissionClick(submission));
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvStudentName, tvStatus, tvGrade;

        public ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvGrade = itemView.findViewById(R.id.tv_grade);
        }
    }
}