package vn.edu.fpt.studentmanagementapp.model;

public class Student {
    private String name;
    private String className;
    private String studentCode;
    private String userId; // New field to link to Users collection

    // Empty constructor needed for Firestore
    public Student() {}

    public Student(String name, String className, String studentCode, String userId) {
        this.name = name;
        this.className = className;
        this.studentCode = studentCode;
        this.userId = userId;
    }

    public String getName() { return name; }
    public String getClassName() { return className; }
    public String getStudentCode() { return studentCode; }
    public String getUserId() { return userId; }
}