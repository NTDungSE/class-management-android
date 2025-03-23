package vn.edu.fpt.studentmanagementapp.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Submission {
    private String submissionId;       // Unique identifier
    private String assignmentId;       // ID of the assignment
    private String classId;            // ID of the class
    private String studentId;          // ID of the student
    private String content;            // Text response (if applicable)
    private String fileUrl;            // URL to submitted file (if applicable)
    private String fileType;           // Type of submitted file
    private Date submittedDate;        // When the submission was made
    private boolean isLate;            // Whether submission was after due date
    private int earnedPoints;          // Points earned
    private String teacherFeedback;    // Feedback from teacher
    private boolean isGraded;          // Whether submission has been graded
    private Map<String, Object> metadata; // Additional metadata
    
    // Empty constructor needed for Firestore
    public Submission() {
        this.submittedDate = new Date();
        this.isGraded = false;
        this.metadata = new HashMap<>();
    }
    
    public Submission(String submissionId, String assignmentId, String classId, 
                     String studentId, String content, String fileUrl, String fileType) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.classId = classId;
        this.studentId = studentId;
        this.content = content;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.submittedDate = new Date();
        this.isGraded = false;
        this.metadata = new HashMap<>();
    }
    
    // Getters and setters
    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }
    
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }
    
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public Date getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(Date submittedDate) { this.submittedDate = submittedDate; }
    
    public boolean isLate() { return isLate; }
    public void setLate(boolean late) { isLate = late; }
    
    public int getEarnedPoints() { return earnedPoints; }
    public void setEarnedPoints(int earnedPoints) { this.earnedPoints = earnedPoints; }
    
    public String getTeacherFeedback() { return teacherFeedback; }
    public void setTeacherFeedback(String teacherFeedback) { this.teacherFeedback = teacherFeedback; }
    
    public boolean isGraded() { return isGraded; }
    public void setGraded(boolean graded) { isGraded = graded; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    // Helper methods
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
}
