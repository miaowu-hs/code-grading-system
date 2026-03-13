package com.example.codegradingsystem.service;

import com.example.codegradingsystem.model.AIAnalysisResult;
import com.example.codegradingsystem.model.ExecutionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class AISubmissionAnalysisService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${ai.api.key:}")
    private String aiApiKey;

    @Value("${ai.api.url:}")
    private String aiApiUrl;

    @Value("${ai.model:}")
    private String aiModel;

    public AISubmissionAnalysisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public AIAnalysisResult analyzeCode(String language, String sourceCode, ExecutionResult executionResult) {
        if (isAiConfigured()) {
            try {
                return callExternalAi(language, sourceCode, executionResult);
            } catch (Exception ignored) {
            }
        }
        return createFallbackAnalysis(language, executionResult);
    }

    private boolean isAiConfigured() {
        return !isBlank(aiApiKey) && !isBlank(aiApiUrl) && !isBlank(aiModel);
    }

    private AIAnalysisResult callExternalAi(String language, String sourceCode, ExecutionResult executionResult)
            throws IOException, InterruptedException {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", aiModel);

        ArrayNode messages = payload.putArray("messages");
        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content",
                "你是中文代码评审助手。只返回严格 JSON，不要返回 Markdown，不要返回代码块。" +
                        "必须包含这些键：codeQuality, explanation, suggestions, fixSteps, knowledgePoints。" +
                        "其中 explanation、suggestions、fixSteps、knowledgePoints 的内容必须全部使用简体中文。");

        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", buildPrompt(language, sourceCode, executionResult));

        payload.put("temperature", 0.2);

        HttpRequest request = HttpRequest.newBuilder(URI.create(resolveAiApiUrl()))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + aiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("AI API returned HTTP " + response.statusCode());
        }

        JsonNode body = objectMapper.readTree(response.body());
        String content = body.path("choices").path(0).path("message").path("content").asText();
        if (isBlank(content)) {
            throw new IOException("AI API returned an empty response");
        }

        String json = extractJson(content);
        JsonNode aiResult = objectMapper.readTree(json);
        return mapAiResult(aiResult, language, executionResult);
    }

    private String buildPrompt(String language, String sourceCode, ExecutionResult executionResult) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析下面这份代码提交，并且只返回 JSON。\n");
        prompt.append("所有说明文字必须使用简体中文。\n");
        prompt.append("语言：").append(language).append("\n");
        prompt.append("源码：\n").append(sourceCode).append("\n\n");
        prompt.append("编译是否成功：").append(executionResult != null && executionResult.isCompileSuccess()).append("\n");
        prompt.append("运行是否成功：").append(executionResult != null && executionResult.isRuntimeSuccess()).append("\n");
        prompt.append("程序输出：").append(executionResult == null ? "" : safe(executionResult.getOutput())).append("\n");
        prompt.append("错误信息：").append(executionResult == null ? "" : safe(executionResult.getErrorMessage())).append("\n");
        prompt.append("返回字段要求：\n");
        prompt.append("- codeQuality：0 到 100 的整数\n");
        prompt.append("- explanation：一句到三句中文说明\n");
        prompt.append("- suggestions：2 到 4 条中文改进建议数组\n");
        prompt.append("- fixSteps：1 到 4 条中文修复步骤数组\n");
        prompt.append("- knowledgePoints：2 到 4 条中文知识点数组\n");
        return prompt.toString();
    }

    private AIAnalysisResult mapAiResult(JsonNode aiResult, String language, ExecutionResult executionResult) {
        AIAnalysisResult result = new AIAnalysisResult();
        result.setCodeQuality(aiResult.path("codeQuality").asInt(defaultScore(executionResult)));
        result.setExplanation(nonBlankOrDefault(
                aiResult.path("explanation").asText(),
                buildFallbackExplanation(language, executionResult)
        ));
        result.setSuggestions(toStringArray(aiResult.path("suggestions"), defaultSuggestions(executionResult)));
        result.setFixSteps(toStringArray(aiResult.path("fixSteps"), defaultFixSteps(executionResult)));
        result.setKnowledgePoints(toStringArray(aiResult.path("knowledgePoints"), defaultKnowledgePoints(language)));
        return result;
    }

    private AIAnalysisResult createFallbackAnalysis(String language, ExecutionResult executionResult) {
        AIAnalysisResult result = new AIAnalysisResult();
        result.setCodeQuality(defaultScore(executionResult));
        result.setExplanation(buildFallbackExplanation(language, executionResult));
        result.setSuggestions(defaultSuggestions(executionResult));
        result.setFixSteps(defaultFixSteps(executionResult));
        result.setKnowledgePoints(defaultKnowledgePoints(language));
        return result;
    }

    private int defaultScore(ExecutionResult executionResult) {
        return executionResult != null && executionResult.isCompileSuccess() && executionResult.isRuntimeSuccess() ? 85 : 60;
    }

    private String buildFallbackExplanation(String language, ExecutionResult executionResult) {
        boolean success = executionResult != null && executionResult.isCompileSuccess() && executionResult.isRuntimeSuccess();
        if (success) {
            return language + " 代码编译并运行成功，当前结果可以作为基础验证通过的参考。";
        }
        String message = executionResult == null ? "" : executionResult.getErrorMessage();
        if (isBlank(message)) {
            return "程序未能成功完成执行。";
        }
        return message;
    }

    private String[] defaultSuggestions(ExecutionResult executionResult) {
        boolean success = executionResult != null && executionResult.isCompileSuccess() && executionResult.isRuntimeSuccess();
        if (success) {
            return new String[]{
                    "提交前补充边界条件和异常输入测试。",
                    "保持方法名和变量名语义清晰，便于阅读。",
                    "为每个测试用例明确记录期望输出。"
            };
        }
        return new String[]{
                "先阅读错误信息，优先修复最先出现的问题。",
                "将代码缩减到最小可复现示例，便于定位错误。",
                "先用简单输入重新测试，再逐步增加复杂用例。"
        };
    }

    private String[] defaultFixSteps(ExecutionResult executionResult) {
        boolean success = executionResult != null && executionResult.isCompileSuccess() && executionResult.isRuntimeSuccess();
        if (success) {
            return new String[]{
                    "保留当前实现作为可运行基线。",
                    "为当前行为补充至少一个回归测试。"
            };
        }
        return new String[]{
                "先修复返回结果中的编译错误或运行时错误。",
                "使用相同输入再次运行，并核对输出是否符合预期。"
        };
    }

    private String[] defaultKnowledgePoints(String language) {
        return new String[]{
                language + " 基础语法",
                "输入输出处理",
                "基于错误信息的调试方法"
        };
    }

    private String[] toStringArray(JsonNode node, String[] fallback) {
        if (!node.isArray() || node.isEmpty()) {
            return fallback;
        }
        List<String> items = new ArrayList<>();
        for (JsonNode item : node) {
            String text = item.asText();
            if (!isBlank(text)) {
                items.add(text);
            }
        }
        return items.isEmpty() ? fallback : items.toArray(new String[0]);
    }

    private String extractJson(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstBrace = trimmed.indexOf('{');
            int lastBrace = trimmed.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1);
            }
        }
        return trimmed;
    }

    private String resolveAiApiUrl() {
        String trimmed = aiApiUrl.trim();
        if (trimmed.endsWith("/chat/completions")) {
            return trimmed;
        }
        if (trimmed.endsWith("/v1")) {
            return trimmed + "/chat/completions";
        }
        if (trimmed.endsWith("/v1/")) {
            return trimmed + "chat/completions";
        }
        return trimmed;
    }

    private String nonBlankOrDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
