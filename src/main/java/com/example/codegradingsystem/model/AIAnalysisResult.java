package com.example.codegradingsystem.model;

public class AIAnalysisResult {
    private int codeQuality;
    private String explanation;
    private String[] suggestions;
    private String[] fixSteps;
    private String[] knowledgePoints;

    public int getCodeQuality() {
        return codeQuality;
    }

    public void setCodeQuality(int codeQuality) {
        this.codeQuality = codeQuality;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String[] getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String[] suggestions) {
        this.suggestions = suggestions;
    }

    public String[] getFixSteps() {
        return fixSteps;
    }

    public void setFixSteps(String[] fixSteps) {
        this.fixSteps = fixSteps;
    }

    public String[] getKnowledgePoints() {
        return knowledgePoints;
    }

    public void setKnowledgePoints(String[] knowledgePoints) {
        this.knowledgePoints = knowledgePoints;
    }
}
