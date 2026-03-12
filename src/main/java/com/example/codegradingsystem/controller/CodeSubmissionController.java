package com.example.codegradingsystem.controller;

import com.example.codegradingsystem.model.CodeSubmission;
import com.example.codegradingsystem.model.ExecutionResult;
import com.example.codegradingsystem.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class CodeSubmissionController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @PostMapping
    public ResponseEntity<ExecutionResult> submitCode(@RequestBody CodeSubmission submission) {
        try {
            ExecutionResult result = codeExecutionService.executeCode(
                submission.getLanguage(),
                submission.getSourceCode(),
                submission.getTestCases()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ExecutionResult errorResult = new ExecutionResult();
            errorResult.setError(true);
            errorResult.setErrorMessage("Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionResult> getSubmissionResult(@PathVariable Long id) {
        // TODO: Implement result retrieval by ID
        ExecutionResult result = new ExecutionResult();
        result.setError(true);
        result.setErrorMessage("Not implemented yet");
        return ResponseEntity.ok(result);
    }
}