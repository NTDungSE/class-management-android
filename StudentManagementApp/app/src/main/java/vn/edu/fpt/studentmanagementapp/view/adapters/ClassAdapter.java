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

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.model.Class;

public class ClassAdapter extends FirestoreRecyclerAdapter<Class, ClassAdapter.ClassViewHolder> {
    private final ClassActionListener listener;

    public interface ClassActionListener {
        void onEditClass(String classId, Class classData);
        void onAssignStudents(String classId);
        void onDeleteClass(String classId);
    }

    public ClassAdapter(@NonNull FirestoreRecyclerOptions<Class> options, ClassActionListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ClassViewHolder holder, int position, @NonNull Class classData) {
        holder.tvName.setText(classData.getName());
        String classId = getSnapshots().getSnapshot(position).getId();

        holder.btnEdit.setOnClickListener(v -> listener.onEditClass(classId, classData));
        holder.btnAssign.setOnClickListener(v -> listener.onAssignStudents(classId));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClass(classId));
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnEdit, btnAssign, btnDelete;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_class_name);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnAssign = itemView.findViewById(R.id.btn_assign);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}