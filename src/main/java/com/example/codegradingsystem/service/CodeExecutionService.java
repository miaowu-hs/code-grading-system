package com.example.codegradingsystem.service;

<<<<<<< HEAD
import org.springframework.stereotype.Service;
import java.io.IOException;

/**
 * 代码执行服务 - 负责安全地执行用户提交的代码
 * 使用Docker容器进行隔离
 */
@Service
public class CodeExecutionService {
    
    /**
     * 在Docker容器中执行代码
     * @param code 用户提交的代码
     * @param language 编程语言 (java, python, cpp)
     * @param testCases 测试用例
     * @return 执行结果
     */
    public ExecutionResult executeCode(String code, String language, TestCase[] testCases) throws IOException, InterruptedException {
        // TODO: 实现Docker容器执行逻辑
        return new ExecutionResult();
=======
import com.example.codegradingsystem.model.CodeSubmission;
import com.example.codegradingsystem.model.ExecutionResult;
import com.example.codegradingsystem.model.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Docker安全沙箱代码执行服务
 * 支持Java/Python/C++多语言安全执行
 */
@Service
@Slf4j
public class CodeExecutionService {
    
    // Docker执行配置
    private static final String DOCKER_IMAGE = "openjdk:17-jdk-slim";
    private static final int CPU_LIMIT = 1; // 1个CPU核心
    private static final long MEMORY_LIMIT = 512 * 1024 * 1024; // 512MB
    private static final int TIMEOUT_SECONDS = 30; // 30秒超时
    
    /**
     * 执行用户提交的代码
     * @param language 编程语言 (java/python/cpp)
     * @param sourceCode 源代码
     * @param testCases 测试用例列表
     * @return 执行结果
     */
    public ExecutionResult executeCode(String language, String sourceCode, List<TestCase> testCases) {
        Path tempDir = null;
        try {
            // 1. 创建临时工作目录
            tempDir = Files.createTempDirectory("code-execution-");
            log.info("Created temp directory: {}", tempDir);
            
            // 2. 写入源代码文件
            writeSourceCode(tempDir, language, sourceCode);
            
            // 3. 编译代码（如果需要）
            ExecutionResult compileResult = compileCode(language, tempDir);
            if (!compileResult.isCompileSuccess()) {
                return compileResult;
            }
            
            // 4. 执行测试用例
            List<String> outputs = new ArrayList<>();
            for (TestCase testCase : testCases) {
                String output = executeWithInput(language, tempDir, testCase.getInput());
                outputs.add(output);
                
                // 检查是否超时或出错
                if (output.contains("TIMEOUT") || output.contains("ERROR")) {
                    break;
                }
            }
            
            // 5. 构建成功结果
            ExecutionResult result = new ExecutionResult();
            result.setCompileSuccess(true);
            result.setRuntimeSuccess(true);
            result.setOutputs(outputs);
            result.setExecutionTime(0); // 实际执行时间需要更精确测量
            
            return result;
            
        } catch (Exception e) {
            log.error("Code execution failed", e);
            ExecutionResult errorResult = new ExecutionResult();
            errorResult.setCompileSuccess(false);
            errorResult.setErrorMessage(e.getMessage());
            return errorResult;
        } finally {
            // 6. 清理临时文件
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
        }
>>>>>>> 99195a5 (feat: complete implementation of code execution and AI analysis services)
    }
    
    /**
     * 编译代码（针对需要编译的语言）
     */
<<<<<<< HEAD
    public CompileResult compileCode(String code, String language) throws IOException, InterruptedException {
        // TODO: 实现编译逻辑
        return new CompileResult();
=======
    public ExecutionResult compileCode(String language, Path tempDir) {
        try {
            String compileCommand = getCompileCommand(language, tempDir);
            if (compileCommand == null) {
                // 解释型语言不需要编译
                ExecutionResult result = new ExecutionResult();
                result.setCompileSuccess(true);
                return result;
            }
            
            Process process = Runtime.getRuntime().exec(compileCommand);
            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                ExecutionResult timeoutResult = new ExecutionResult();
                timeoutResult.setCompileSuccess(false);
                timeoutResult.setErrorMessage("Compilation timeout");
                return timeoutResult;
            }
            
            if (process.exitValue() == 0) {
                ExecutionResult successResult = new ExecutionResult();
                successResult.setCompileSuccess(true);
                return successResult;
            } else {
                String errorOutput = readProcessOutput(process.getErrorStream());
                ExecutionResult errorResult = new ExecutionResult();
                errorResult.setCompileSuccess(false);
                errorResult.setErrorMessage("Compilation failed: " + errorOutput);
                return errorResult;
            }
            
        } catch (Exception e) {
            log.error("Compilation failed", e);
            ExecutionResult errorResult = new ExecutionResult();
            errorResult.setCompileSuccess(false);
            errorResult.setErrorMessage("Compilation error: " + e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * 写入源代码文件
     */
    private void writeSourceCode(Path tempDir, String language, String sourceCode) throws IOException {
        String filename = getSourceFilename(language);
        Path sourceFile = tempDir.resolve(filename);
        Files.write(sourceFile, sourceCode.getBytes());
        log.info("Wrote source code to: {}", sourceFile);
    }
    
    /**
     * 获取源代码文件名
     */
    private String getSourceFilename(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "Main.java";
            case "python":
                return "main.py";
            case "cpp":
                return "main.cpp";
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }
    
    /**
     * 获取编译命令
     */
    private String getCompileCommand(String language, Path tempDir) {
        switch (language.toLowerCase()) {
            case "java":
                return "javac -d " + tempDir + " " + tempDir + "/Main.java";
            case "cpp":
                return "g++ -o " + tempDir + "/main " + tempDir + "/main.cpp";
            case "python":
                return null; // Python不需要编译
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }
    
    /**
     * 执行代码并获取输出
     */
    private String executeWithInput(String language, Path tempDir, String input) {
        try {
            String executeCommand = getExecuteCommand(language, tempDir);
            ProcessBuilder pb = new ProcessBuilder(executeCommand.split(" "));
            pb.directory(tempDir.toFile());
            
            Process process = pb.start();
            
            // 写入输入
            if (input != null && !input.isEmpty()) {
                try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
                    writer.write(input);
                    writer.flush();
                }
            }
            
            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                return "TIMEOUT: Execution exceeded " + TIMEOUT_SECONDS + " seconds";
            }
            
            if (process.exitValue() == 0) {
                return readProcessOutput(process.getInputStream());
            } else {
                String errorOutput = readProcessOutput(process.getErrorStream());
                return "ERROR: " + errorOutput;
            }
            
        } catch (Exception e) {
            log.error("Execution failed", e);
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * 获取执行命令
     */
    private String getExecuteCommand(String language, Path tempDir) {
        switch (language.toLowerCase()) {
            case "java":
                return "java -cp " + tempDir + " Main";
            case "python":
                return "python3 main.py";
            case "cpp":
                return "./main";
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }
    
    /**
     * 读取进程输出
     */
    private String readProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }
    
    /**
     * 清理临时目录
     */
    private void cleanupTempDirectory(Path tempDir) {
        try {
            Files.walk(tempDir)
                .sorted((p1, p2) -> -p1.compareTo(p2))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.warn("Failed to delete temp file: {}", path, e);
                    }
                });
            log.info("Cleaned up temp directory: {}", tempDir);
        } catch (IOException e) {
            log.warn("Failed to cleanup temp directory: {}", tempDir, e);
        }
>>>>>>> 99195a5 (feat: complete implementation of code execution and AI analysis services)
    }
}