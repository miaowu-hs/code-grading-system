package com.example.codegradingsystem.model;

import java.util.ArrayList;
import java.util.List;

public class CodeSubmission {
    private String language;
    private String sourceCode;
    private List<TestCase> testCases = new ArrayList<>();

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

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases == null ? new ArrayList<>() : testCases;
    }
}
