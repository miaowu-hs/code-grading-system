package com.example.codegradingsystem.service;

import org.springframework.stereotype.Service;

@Service
public class LanguageExecutionService {
    
    public String buildCompileCommand(String language, String filename) {
        switch (language.toLowerCase()) {
            case "java":
                return "javac " + filename + ".java";
            case "python":
                return ""; // Python不需要编译
            case "cpp":
                return "g++ -std=c++17 -O2 -o " + filename + " " + filename + ".cpp";
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }
    
    public String buildExecuteCommand(String language, String filename) {
        switch (language.toLowerCase()) {
            case "java":
                return "java " + filename;
            case "python":
                return "python3 " + filename + ".py";
            case "cpp":
                return "./" + filename;
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }
    
    public String getSourceFilename(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "Main";
            case "python":
                return "main";
            case "cpp":
                return "main";
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }
    
    public String getFileExtension(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return ".java";
            case "python":
                return ".py";
            case "cpp":
                return ".cpp";
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }
}