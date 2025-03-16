package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Student;


public class StudentAdapter extends FirestoreRecyclerAdapter<Student, StudentAdapter.StudentViewHolder> {
    private final StudentActionListener listener;
    private final FirebaseFirestore db;

    public interface StudentActionListener {
        void onEditStudent(String documentId, Student student);
        void onDeleteStudent(String documentId);
    }

    public StudentAdapter(@NonNull FirestoreRecyclerOptions<Student> options, StudentActionListener listener) {
        super(options);
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
        setHasStableIds(true); // Add this line to ensure stable IDs
    }

    @Override
    public long getItemId(int position) {
        // This ensures stable IDs based on the document ID
        return getSnapshots().getSnapshot(position).getId().hashCode();
    }

    @Override
    protected void onBindViewHolder(@NonNull StudentViewHolder holder, int position, @NonNull Student student) {
        holder.tvName.setText(student.getName());
        holder.tvClass.setText(student.getClassName());
        holder.tvCode.setText(student.getStudentCode());

        // Reset any previous email information
        holder.tvEmail.setText("");

        if (student.getUserId() != null) {
            // Use the tag to avoid updating wrong views
            final String userId = student.getUserId();
            holder.tvEmail.setTag(userId);

            db.collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        // Only update if this view still belongs to the same student
                        if (holder.tvEmail.getTag() != null && holder.tvEmail.getTag().equals(userId)) {
                            String email = doc.getString("email");
                            holder.tvEmail.setText(email != null ? email : "Not linked");
                        }
                    });
        } else {
            holder.tvEmail.setText("No user account");
        }

        String documentId = getSnapshots().getSnapshot(position).getId();
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditStudent(documentId, student);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteStudent(documentId);
            }
        });
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvClass, tvCode, tvEmail;
        ImageButton btnEdit, btnDelete;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_student_name);
            tvClass = itemView.findViewById(R.id.tv_class);
            tvCode = itemView.findViewById(R.id.tv_student_code);
            tvEmail = itemView.findViewById(R.id.tv_email); // Add this TextView to your item_student.xml
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}