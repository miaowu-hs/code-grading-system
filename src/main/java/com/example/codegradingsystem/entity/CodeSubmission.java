package com.example.codegradingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_submissions")
public class CodeSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String language;
    
    @Lob
    @Column(nullable = false)
    private String code;
    
    @Lob
    private String feedback;
    
    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    
    @Column(nullable = false)
    private LocalDateTime submittedAt;
    
    private LocalDateTime completedAt;
    
    @Column(nullable = false)
    private Integer score;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}