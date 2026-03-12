package com.example.codegradingsystem.service;

import com.example.codegradingsystem.model.ExecutionResult;
import com.example.codegradingsystem.model.AIAnalysisResult;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * AI代码分析服务 - 使用GLM-4/Qwen API进行智能代码分析
 */
@Service
public class AISubmissionAnalysisService {
    
    @Value("${ai.api.key}")
    private String aiApiKey;
    
    @Value("${ai.api.url}")
    private String aiApiUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AISubmissionAnalysisService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 分析代码执行结果并生成AI反馈
     */
    public AIAnalysisResult analyzeCode(String language, String sourceCode, ExecutionResult executionResult) {
        try {
            // 构建AI分析请求
            String prompt = buildAnalysisPrompt(language, sourceCode, executionResult);
            
            // 调用AI API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "glm-4");
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", prompt)
            });
            requestBody.put("temperature", 0.7);
            
            // 发送请求到AI API
            // 这里会根据实际配置使用GLM-4或Qwen API
            String aiResponse = callAiApi(requestBody);
            
            // 解析AI响应
            return parseAiResponse(aiResponse);
            
        } catch (Exception e) {
            // AI分析失败时返回基础分析结果
            return createFallbackAnalysis(executionResult);
        }
    }
    
    /**
     * 构建AI分析提示词
     */
    private String buildAnalysisPrompt(String language, String sourceCode, ExecutionResult executionResult) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专业的编程导师，请分析以下学生提交的代码：\n\n");
        prompt.append("编程语言: ").append(language).append("\n");
        prompt.append("源代码:\n").append(sourceCode).append("\n\n");
        
        if (executionResult.isCompileSuccess()) {
            prompt.append("编译结果: 成功\n");
            if (executionResult.isRuntimeSuccess()) {
                prompt.append("运行结果: 成功\n");
                prompt.append("输出: ").append(executionResult.getOutput()).append("\n");
            } else {
                prompt.append("运行结果: 失败\n");
                prompt.append("错误信息: ").append(executionResult.getError()).append("\n");
            }
        } else {
            prompt.append("编译结果: 失败\n");
            prompt.append("错误信息: ").append(executionResult.getError()).append("\n");
        }
        
        prompt.append("\n请提供以下分析：\n");
        prompt.append("1. 代码质量评分 (1-100分)\n");
        prompt.append("2. 具体的问题和改进建议\n");
        prompt.append("3. 如果有错误，请用通俗易懂的语言解释错误原因\n");
        prompt.append("4. 提供修复步骤指导\n");
        prompt.append("5. 相关的编程知识点提醒\n\n");
        prompt.append("请用中文回答，语气温和友善，重点帮助学生学习和进步。");
        
        return prompt.toString();
    }
    
    /**
     * 调用AI API
     */
    private String callAiApi(Map<String, Object> requestBody) {
        // 这里会根据实际配置调用GLM-4或Qwen API
        // 由于网络环境限制，这里先返回模拟响应
        // 实际部署时会替换为真实的API调用
        
        return simulateAiResponse();
    }
    
    /**
     * 模拟AI响应（实际部署时替换为真实API调用）
     */
    private String simulateAiResponse() {
        return "{\n" +
               "  \"qualityScore\": 85,\n" +
               "  \"suggestions\": [\n" +
               "    \"变量命名可以更具有描述性\",\n" +
               "    \"可以添加更多的注释来解释复杂逻辑\"\n" +
               "  ],\n" +
               "  \"explanation\": \"你的代码逻辑基本正确，但有一些小的改进空间。\",\n" +
               "  \"fixSteps\": [\n" +
               "    \"将变量名从'a'改为'dataArray'\",\n" +
               "    \"在循环处添加注释说明算法目的\"\n" +
               "  ],\n" +
               "  \"knowledgePoints\": [\n" +
               "    \"良好的变量命名规范\",\n" +
               "    \"代码可读性的重要性\"\n" +
               "  ]\n" +
               "}";
    }
    
    /**
     * 解析AI响应
     */
    private AIAnalysisResult parseAiResponse(String response) {
        try {
            return objectMapper.readValue(response, AIAnalysisResult.class);
        } catch (Exception e) {
            return createFallbackAnalysis(null);
        }
    }
    
    /**
     * 创建备用分析结果
     */
    private AIAnalysisResult createFallbackAnalysis(ExecutionResult executionResult) {
        AIAnalysisResult result = new AIAnalysisResult();
        result.setQualityScore(executionResult != null && executionResult.isRuntimeSuccess() ? 70 : 50);
        result.setSuggestions(new String[]{"AI分析暂时不可用，但代码执行结果已记录"});
        result.setExplanation("系统正在使用基础分析模式");
        result.setFixSteps(new String[]{"请稍后重试AI分析功能"});
        result.setKnowledgePoints(new String[]{"基础代码分析"});
        return result;
    }
}