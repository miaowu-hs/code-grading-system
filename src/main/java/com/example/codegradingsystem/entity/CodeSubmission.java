package com.example.codegradingsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_submissions")
public class CodeSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String language;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String sourceCode;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String inputData;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private boolean compileSuccess;

    @Column(nullable = false)
    private boolean runtimeSuccess;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String output;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String errorMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isCompileSuccess() {
        return compileSuccess;
    }

    public void setCompileSuccess(boolean compileSuccess) {
        this.compileSuccess = compileSuccess;
    }

    public boolean isRuntimeSuccess() {
        return runtimeSuccess;
    }

    public void setRuntimeSuccess(boolean runtimeSuccess) {
        this.runtimeSuccess = runtimeSuccess;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
