package com.example.codegradingsystem.model;

public class SubmissionResponse {
    private Long id;
    private String status;
    private ExecutionResult executionResult;
    private AIAnalysisResult aiFeedback;
    private String message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public AIAnalysisResult getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(AIAnalysisResult aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
