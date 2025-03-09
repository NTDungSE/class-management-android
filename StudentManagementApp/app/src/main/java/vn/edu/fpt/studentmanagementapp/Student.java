package vn.edu.fpt.studentmanagementapp;

public class Student {
    private String name;
    private String className;
    private String studentCode;

    // Empty constructor needed for Firestore
    public Student() {}

    public Student(String name, String className, String studentCode) {
        this.name = name;
        this.className = className;
        this.studentCode = studentCode;
    }

    public String getName() { return name; }
    public String getClassName() { return className; }
    public String getStudentCode() { return studentCode; }
}