package vn.edu.fpt.studentmanagementapp.view.adapters;

import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getSnapshots().getSnapshot(position).getId().hashCode();
    }

    @Override
    protected void onBindViewHolder(@NonNull StudentViewHolder holder, int position, @NonNull Student model) {
        holder.tvStudentName.setText(model.getName());

        // Display classes loading message while we fetch them
        holder.tvClass.setText("Classes: Loading...");

        // Fetch and display class names
        List<String> classIds = model.getClassIds();
        if (classIds != null && !classIds.isEmpty()) {
            fetchClassNames(classIds, classNames -> {
                if (classNames == null || classNames.isEmpty()) {
                    holder.tvClass.setText("Classes: None");
                } else {
                    holder.tvClass.setText("Classes: " + classNames);
                }
            });
        } else {
            holder.tvClass.setText("Classes: None");
        }

        // Handle email
        if (model.getUserId() != null && !model.getUserId().isEmpty()) {
            holder.tvEmail.setText("Email: Loading...");
            db.collection("Users").document(model.getUserId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String email = documentSnapshot.getString("email");
                            holder.tvEmail.setText("Email: " + (email != null ? email : "N/A"));
                        } else {
                            holder.tvEmail.setText("Email: N/A");
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.tvEmail.setText("Email: Error loading");
                    });
        } else {
            holder.tvEmail.setText("Email: N/A");
        }

        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> listener.onEditStudent(getSnapshots().getSnapshot(position).getId(), model));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteStudent(getSnapshots().getSnapshot(position).getId()));
    }

    private void fetchClassNames(List<String> classIds, ClassNamesCallback callback) {
        if (classIds == null || classIds.isEmpty()) {
            callback.onClassNamesLoaded("None");
            return;
        }

        List<String> classNames = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);

        for (String classId : classIds) {
            // Skip null or empty class IDs
            if (classId == null || classId.isEmpty()) {
                if (counter.incrementAndGet() == classIds.size()) {
                    String result = classNames.isEmpty() ? "None" : TextUtils.join(", ", classNames);
                    callback.onClassNamesLoaded(result);
                }
                continue;
            }

            db.collection("Classes").document(classId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String className = documentSnapshot.getString("name");
                            if (className != null && !className.isEmpty()) {
                                classNames.add(className);
                            } else {
                                classNames.add("Unknown Class");
                            }
                        } else {
                            classNames.add("Invalid Class");
                        }

                        // Check if we've processed all classes
                        if (counter.incrementAndGet() == classIds.size()) {
                            String result = classNames.isEmpty() ? "None" : TextUtils.join(", ", classNames);
                            callback.onClassNamesLoaded(result);
                        }
                    })
                    .addOnFailureListener(e -> {
                        classNames.add("Error");
                        if (counter.incrementAndGet() == classIds.size()) {
                            String result = classNames.isEmpty() ? "None" : TextUtils.join(", ", classNames);
                            callback.onClassNamesLoaded(result);
                        }
                    });
        }
    }

    interface ClassNamesCallback {
        void onClassNamesLoaded(String classNames);
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvClass, tvStudentCode, tvEmail;
        ImageButton btnEdit, btnDelete;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvClass = itemView.findViewById(R.id.tv_class);
            tvStudentCode = itemView.findViewById(R.id.tv_student_code);
            tvEmail = itemView.findViewById(R.id.tv_email);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}