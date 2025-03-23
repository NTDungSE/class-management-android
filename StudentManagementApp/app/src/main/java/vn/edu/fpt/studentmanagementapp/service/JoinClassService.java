package vn.edu.fpt.studentmanagementapp.service;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import vn.edu.fpt.studentmanagementapp.model.Class;
import vn.edu.fpt.studentmanagementapp.model.Student;

public class JoinClassService {
    private static final String TAG = "JoinClassService";
    private final FirebaseFirestore db;
    private final FirebaseUser currentUser;

    public interface JoinClassCallback {
        void onSuccess(String className);
        void onFailure(String errorMessage);
        void onClassNotFound();
        void onAlreadyJoined();
    }

    public JoinClassService() {
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void joinClassWithCode(String classCode, JoinClassCallback callback) {
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        // First, find the class with this code
        db.collection("Classes")
                .whereEqualTo("classCode", classCode)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onClassNotFound();
                        return;
                    }

                    // Get the class document
                    DocumentSnapshot classDoc = querySnapshot.getDocuments().get(0);
                    Class classData = classDoc.toObject(Class.class);
                    String classId = classDoc.getId();

                    if (classData == null) {
                        callback.onFailure("Invalid class data");
                        return;
                    }

                    String userId = currentUser.getUid();
                    if (!classData.getTeacherId().equals(userId) &&
                            !classData.isStudentEnrolled(userId) &&
                            !classData.isStudentEnrolled(currentUser.getEmail())) {
                        // The user is allowed to join this class
                        // Continue with joining process...
                        // Update the class document to add the student
                        updateClassWithStudent(classId, classData, callback);
                    } else {
                        callback.onAlreadyJoined();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding class: " + e.getMessage(), e);
                    callback.onFailure("Error finding class: " + e.getMessage());
                });
    }

    private void updateClassWithStudent(String classId, Class classData, JoinClassCallback callback) {
        // Get current student info
        db.collection("Students")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Student student = documentSnapshot.toObject(Student.class);

                    if (student == null) {
                        // Create a new student record if it doesn't exist
                        student = new Student(
                                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Student",
                                currentUser.getEmail(),
                                currentUser.getUid()
                        );

                        db.collection("Students")
                                .document(currentUser.getUid())
                                .set(student);
                    }

                    // Update class enrollment map
                    Map<String, String> enrolledStudents = classData.getEnrolledStudents() != null ?
                            new HashMap<>(classData.getEnrolledStudents()) : new HashMap<>();

                    // If student was previously invited by email, update status
                    if (enrolledStudents.containsKey(currentUser.getEmail())) {
                        enrolledStudents.remove(currentUser.getEmail());
                    }

                    // Add student with "enrolled" status
                    enrolledStudents.put(currentUser.getUid(), "enrolled");

                    // Update class document
                    Student finalStudent = student;
                    db.collection("Classes")
                            .document(classId)
                            .update("enrolledStudents", enrolledStudents)
                            .addOnSuccessListener(aVoid -> {
                                // Now update the student document
                                updateStudentWithClass(classId, finalStudent, classData.getName(), callback);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating class: " + e.getMessage(), e);
                                callback.onFailure("Error updating class: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting student data: " + e.getMessage(), e);
                    callback.onFailure("Error getting student data: " + e.getMessage());
                });
    }

    private void updateStudentWithClass(String classId, Student student, String className, JoinClassCallback callback) {
        // Update student's enrolled classes
        Map<String, String> enrolledClasses = student.getEnrolledClasses() != null ?
                new HashMap<>(student.getEnrolledClasses()) : new HashMap<>();

        enrolledClasses.put(classId, "enrolled");

        db.collection("Students")
                .document(currentUser.getUid())
                .update("enrolledClasses", enrolledClasses)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(className);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating student enrollment: " + e.getMessage(), e);
                    callback.onFailure("Error updating student enrollment: " + e.getMessage());
                });
    }
}