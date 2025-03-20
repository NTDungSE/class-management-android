package vn.edu.fpt.studentmanagementapp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student {
    private String name;
    private String userId;     // Firebase User ID (null if student hasn't registered)
    private String email;      // Email address (used as identifier before registration)

    // Maps to store class enrollment
    // Key: classId
    // Value: enrollment status ("enrolled", "invited")
    private Map<String, String> enrolledClasses;

    // Empty constructor needed for Firestore
    public Student() {
        this.enrolledClasses = new HashMap<>();
    }

    public Student(String name, String email) {
        this.name = name;
        this.email = email;
        this.enrolledClasses = new HashMap<>();
    }

    // Constructor with userId (for registered students)
    public Student(String name, String email, String userId) {
        this.name = name;
        this.email = email;
        this.userId = userId;
        this.enrolledClasses = new HashMap<>();
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Map<String, String> getEnrolledClasses() { return enrolledClasses; }
    public void setEnrolledClasses(Map<String, String> enrolledClasses) { this.enrolledClasses = enrolledClasses; }

    // Helper methods for class enrollment
    public void addClass(String classId, String status) {
        enrolledClasses.put(classId, status);
    }

    public void removeClass(String classId) {
        enrolledClasses.remove(classId);
    }

    public boolean isEnrolledInClass(String classId) {
        return enrolledClasses.containsKey(classId);
    }

    public String getEnrollmentStatus(String classId) {
        return enrolledClasses.get(classId);
    }

    // Get list of class IDs (needed for backward compatibility)
    public List<String> getClassIds() {
        return new ArrayList<>(enrolledClasses.keySet());
    }

    // Helper method to determine if student is registered
    public boolean isRegistered() {
        return userId != null && !userId.isEmpty();
    }

    // Get the identifier that should be used (userId if registered, email otherwise)
    public String getIdentifier() {
        return isRegistered() ? userId : email;
    }
}