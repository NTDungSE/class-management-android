package vn.edu.fpt.studentmanagementapp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Class {
    private String classId;    // Unique identifier for the class (Firestore document ID)
    private String name;       // Name of the class (e.g., "Math 101")
    private String teacherId;  // UID of the teacher who owns the class
    private String classCode;  // Unique code for students to join (like Google Classroom)

    // Maps to store enrollment status
    // Key: userId or email for unregistered students
    // Value: enrollment status ("enrolled", "invited")
    private Map<String, String> enrolledStudents;

    // Empty constructor needed for Firestore
    public Class() {
        this.enrolledStudents = new HashMap<>();
    }

    public Class(String classId, String name, String teacherId, String classCode) {
        this.classId = classId;
        this.name = name;
        this.teacherId = teacherId;
        this.classCode = classCode;
        this.enrolledStudents = new HashMap<>();
    }

    // Getters and setters
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public Map<String, String> getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(Map<String, String> enrolledStudents) { this.enrolledStudents = enrolledStudents; }

    // Helper methods for student enrollment
    public void addStudent(String identifier, String status) {
        enrolledStudents.put(identifier, status);
    }

    public void removeStudent(String identifier) {
        enrolledStudents.remove(identifier);
    }

    public boolean isStudentEnrolled(String identifier) {
        return enrolledStudents.containsKey(identifier);
    }

    public String getStudentStatus(String identifier) {
        return enrolledStudents.get(identifier);
    }

    // Get list of registered student IDs (needed for backward compatibility)
    public List<String> getStudentIds() {
        List<String> studentIds = new ArrayList<>();
        for (Map.Entry<String, String> entry : enrolledStudents.entrySet()) {
            // Only include entries that appear to be user IDs (not emails)
            if (!entry.getKey().contains("@")) {
                studentIds.add(entry.getKey());
            }
        }
        return studentIds;
    }

    // Add these methods to Class.java
    public List<Map<String, Object>> getStudentsWithStatus() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<String, String> entry : enrolledStudents.entrySet()) {
            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put("identifier", entry.getKey());
            studentInfo.put("status", entry.getValue());
            result.add(studentInfo);
        }

        return result;
    }

    public int getEnrolledStudentCount() {
        int count = 0;
        for (String status : enrolledStudents.values()) {
            if ("enrolled".equals(status)) {
                count++;
            }
        }
        return count;
    }

    public int getInvitedStudentCount() {
        int count = 0;
        for (String status : enrolledStudents.values()) {
            if ("invited".equals(status)) {
                count++;
            }
        }
        return count;
    }
}