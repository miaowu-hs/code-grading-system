package com.example.codegradingsystem.controller;

import com.example.codegradingsystem.model.AIAnalysisResult;
import com.example.codegradingsystem.model.CodeSubmission;
import com.example.codegradingsystem.model.ExecutionResult;
import com.example.codegradingsystem.model.SubmissionResponse;
import com.example.codegradingsystem.repository.CodeSubmissionRepository;
import com.example.codegradingsystem.service.AISubmissionAnalysisService;
import com.example.codegradingsystem.service.CodeExecutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submissions")
public class CodeSubmissionController {

    private final CodeExecutionService codeExecutionService;
    private final AISubmissionAnalysisService aiSubmissionAnalysisService;
    private final CodeSubmissionRepository codeSubmissionRepository;

    public CodeSubmissionController(
            CodeExecutionService codeExecutionService,
            AISubmissionAnalysisService aiSubmissionAnalysisService,
            CodeSubmissionRepository codeSubmissionRepository
    ) {
        this.codeExecutionService = codeExecutionService;
        this.aiSubmissionAnalysisService = aiSubmissionAnalysisService;
        this.codeSubmissionRepository = codeSubmissionRepository;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> submitCode(@RequestBody CodeSubmission submission) {
        try {
            ExecutionResult executionResult = codeExecutionService.executeCode(submission);
            AIAnalysisResult aiFeedback = aiSubmissionAnalysisService.analyzeCode(
                    submission.getLanguage(),
                    submission.getSourceCode(),
                    executionResult
            );

            com.example.codegradingsystem.entity.CodeSubmission record = new com.example.codegradingsystem.entity.CodeSubmission();
            record.setLanguage(submission.getLanguage());
            record.setSourceCode(submission.getSourceCode());
            record.setInputData(submission.getTestCases().stream()
                    .map(testCase -> testCase.getInput() == null ? "" : testCase.getInput())
                    .collect(Collectors.joining("\n---\n")));
            record.setStatus(executionResult.isRuntimeSuccess() ? "COMPLETED" : "FAILED");
            record.setSubmittedAt(LocalDateTime.now());
            record.setCompletedAt(LocalDateTime.now());
            record.setCompileSuccess(executionResult.isCompileSuccess());
            record.setRuntimeSuccess(executionResult.isRuntimeSuccess());
            record.setOutput(executionResult.getOutput());
            record.setErrorMessage(executionResult.getErrorMessage());
            record = codeSubmissionRepository.save(record);

            SubmissionResponse response = new SubmissionResponse();
            response.setId(record.getId());
            response.setStatus(record.getStatus());
            response.setExecutionResult(executionResult);
            response.setAiFeedback(aiFeedback);
            response.setMessage(executionResult.isRuntimeSuccess()
                    ? "提交执行成功"
                    : "提交执行失败");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SubmissionResponse response = new SubmissionResponse();
            ExecutionResult executionResult = new ExecutionResult();
            executionResult.setCompileSuccess(false);
            executionResult.setRuntimeSuccess(false);
            executionResult.setError(true);
            executionResult.setErrorMessage("服务器内部错误：" + e.getMessage());
            response.setStatus("FAILED");
            response.setExecutionResult(executionResult);
            response.setMessage(executionResult.getErrorMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> getSubmissionResult(@PathVariable Long id) {
        return codeSubmissionRepository.findById(id)
                .map(record -> {
                    ExecutionResult executionResult = new ExecutionResult();
                    executionResult.setCompileSuccess(record.isCompileSuccess());
                    executionResult.setRuntimeSuccess(record.isRuntimeSuccess());
                    executionResult.setOutput(record.getOutput());
                    executionResult.setErrorMessage(record.getErrorMessage());
                    executionResult.setError(!record.isRuntimeSuccess());

                    SubmissionResponse response = new SubmissionResponse();
                    response.setId(record.getId());
                    response.setStatus(record.getStatus());
                    response.setExecutionResult(executionResult);
                    response.setMessage("提交记录加载成功");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    SubmissionResponse response = new SubmissionResponse();
                    response.setStatus("NOT_FOUND");
                    response.setMessage("未找到提交记录");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }
}
