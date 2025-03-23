package vn.edu.fpt.studentmanagementapp.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Assignment {
    private String assignmentId;    // Unique identifier (Firestore document ID)
    private String title;           // Title of the assignment
    private String description;     // Detailed instructions
    private String classId;         // Class this assignment belongs to
    private Date dueDate;           // When the assignment is due
    private Date createdDate;       // When the assignment was created
    private int possiblePoints;     // Maximum points possible
    private String fileUrl;         // Optional: URL to attached file
    private String fileType;        // Optional: Type of attached file
    private boolean isPublished;    // Whether assignment is visible to students
    
    // Maps student IDs to their submission status
    // Status can be: "not_submitted", "submitted", "graded"
    private Map<String, String> submissionStatus;
    
    // Empty constructor needed for Firestore
    public Assignment() {
        this.submissionStatus = new HashMap<>();
        this.createdDate = new Date();
        this.isPublished = false;
    }
    
    public Assignment(String assignmentId, String title, String description, 
                     String classId, Date dueDate, int possiblePoints) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.description = description;
        this.classId = classId;
        this.dueDate = dueDate;
        this.possiblePoints = possiblePoints;
        this.createdDate = new Date();
        this.isPublished = false;
        this.submissionStatus = new HashMap<>();
    }
    
    // Getters and setters
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    
    public int getPossiblePoints() { return possiblePoints; }
    public void setPossiblePoints(int possiblePoints) { this.possiblePoints = possiblePoints; }
    
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }
    
    public Map<String, String> getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(Map<String, String> submissionStatus) { this.submissionStatus = submissionStatus; }
    
    // Helper methods
    public void updateStudentSubmissionStatus(String studentId, String status) {
        submissionStatus.put(studentId, status);
    }
    
    public boolean isOverdue() {
        return dueDate != null && new Date().after(dueDate);
    }
    
    public int getSubmittedCount() {
        int count = 0;
        for (String status : submissionStatus.values()) {
            if ("submitted".equals(status) || "graded".equals(status)) {
                count++;
            }
        }
        return count;
    }
    
    public int getGradedCount() {
        int count = 0;
        for (String status : submissionStatus.values()) {
            if ("graded".equals(status)) {
                count++;
            }
        }
        return count;
    }
}
