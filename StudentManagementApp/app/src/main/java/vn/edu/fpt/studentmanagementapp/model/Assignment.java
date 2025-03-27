package vn.edu.fpt.studentmanagementapp.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Assignment {
    private String assignmentId;
    private String title;
    private String description;
    private String classId;
    private Date dueDate;
    private Date createdDate;
    private int possiblePoints;
    private String fileUrl;
    private String fileType;
    private boolean isPublished;
    private boolean overdue;

    private Map<String, String> submissionStatus;

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

    public boolean isOverdue() {
        return dueDate != null && new Date().after(dueDate);
    }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
}