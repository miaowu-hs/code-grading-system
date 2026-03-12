package com.example.codegradingsystem.model;

import lombok.Data;

@Data
public class AIAnalysisResult {
    private int codeQualityScore; // 代码质量评分 (0-100)
    private String explanation; // 错误解释
    private String[] suggestions; // 优化建议
    private String learningTips; // 学习提示
    private boolean hasSecurityIssues; // 是否有安全问题
}