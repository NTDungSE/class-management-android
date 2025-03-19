package vn.edu.fpt.studentmanagementapp.model;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private String name;
    private String studentCode;
    private String userId;
    private List<String> classIds; // Changed from className to classIds list

    // Empty constructor needed for Firestore
    public Student() {
        this.classIds = new ArrayList<>();
    }

    public Student(String name, String studentCode, String userId) {
        this.name = name;
        this.studentCode = studentCode;
        this.userId = userId;
        this.classIds = new ArrayList<>();
    }

    // Getters and setters
    public String getName() { return name; }
    public String getStudentCode() { return studentCode; }
    public String getUserId() { return userId; }
    public List<String> getClassIds() { return classIds; }
    public void setClassIds(List<String> classIds) { this.classIds = classIds; }

    // Helper methods
    public void addClass(String classId) {
        if (!classIds.contains(classId)) {
            classIds.add(classId);
        }
    }

    public void removeClass(String classId) {
        classIds.remove(classId);
    }
}