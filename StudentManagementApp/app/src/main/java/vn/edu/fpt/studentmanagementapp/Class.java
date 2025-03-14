package vn.edu.fpt.studentmanagementapp;

import java.util.ArrayList;
import java.util.List;

public class Class {
    private String classId;    // Unique identifier for the class (Firestore document ID)
    private String name;       // Name of the class (e.g., "Math 101")
    private String teacherId;  // UID of the teacher who owns the class
    private List<String> studentIds; // List of student UIDs enrolled in the class

    // Empty constructor needed for Firestore
    public Class() {
        this.studentIds = new ArrayList<>();
    }

    public Class(String classId, String name, String teacherId) {
        this.classId = classId;
        this.name = name;
        this.teacherId = teacherId;
        this.studentIds = new ArrayList<>();
    }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public List<String> getStudentIds() { return studentIds; }
    public void setStudentIds(List<String> studentIds) { this.studentIds = studentIds; }

    // Helper method to add a student
    public void addStudent(String studentId) {
        if (!studentIds.contains(studentId)) {
            studentIds.add(studentId);
        }
    }

    // Helper method to remove a student
    public void removeStudent(String studentId) {
        studentIds.remove(studentId);
    }
}