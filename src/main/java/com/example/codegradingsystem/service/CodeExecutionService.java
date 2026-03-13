package com.example.codegradingsystem.service;

import com.example.codegradingsystem.model.CodeSubmission;
import com.example.codegradingsystem.model.ExecutionResult;
import com.example.codegradingsystem.model.TestCase;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionService {

    private static final long TIMEOUT_SECONDS = 30;
    private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    private static final String[] WINDOWS_EXECUTABLE_SUFFIXES = {".exe", ".cmd", ".bat", ""};

    public ExecutionResult executeCode(CodeSubmission submission) {
        return executeCode(submission.getLanguage(), submission.getSourceCode(), submission.getTestCases());
    }

    public ExecutionResult executeCode(String language, String sourceCode, List<TestCase> testCases) {
        ExecutionResult result = new ExecutionResult();
        Path tempDir = null;
        long startedAt = System.nanoTime();
        try {
            validateSubmission(language, sourceCode);
            tempDir = Files.createTempDirectory("code-grading-");
            writeSourceCode(tempDir, language, sourceCode);

            ExecutionResult compileResult = compileCode(language, tempDir);
            if (!compileResult.isCompileSuccess()) {
                compileResult.setExecutionTime(toMillis(startedAt));
                compileResult.setError(true);
                return compileResult;
            }

            List<TestCase> effectiveTestCases = normalizeTestCases(testCases);
            List<String> outputs = new ArrayList<>();
            for (TestCase testCase : effectiveTestCases) {
                String output = runWithInput(language, tempDir, testCase.getInput());
                outputs.add(output);
                if (output.startsWith("ERROR:") || output.startsWith("TIMEOUT:")) {
                    result.setCompileSuccess(true);
                    result.setRuntimeSuccess(false);
                    result.setError(true);
                    result.setErrorMessage(output);
                    result.setOutputs(outputs);
                    result.setOutput(output);
                    result.setExecutionTime(toMillis(startedAt));
                    return result;
                }
            }

            result.setCompileSuccess(true);
            result.setRuntimeSuccess(true);
            result.setOutputs(outputs);
            result.setOutput(outputs.isEmpty() ? "" : outputs.get(outputs.size() - 1));
            result.setExecutionTime(toMillis(startedAt));
            return result;
        } catch (Exception e) {
            result.setCompileSuccess(false);
            result.setRuntimeSuccess(false);
            result.setError(true);
            result.setErrorMessage(e.getMessage());
            result.setExecutionTime(toMillis(startedAt));
            return result;
        } finally {
            if (tempDir != null) {
                cleanupDirectory(tempDir);
            }
        }
    }

    private void validateSubmission(String language, String sourceCode) {
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Language is required");
        }
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("Source code is required");
        }
    }

    private List<TestCase> normalizeTestCases(List<TestCase> testCases) {
        if (testCases == null || testCases.isEmpty()) {
            return List.of(new TestCase("", ""));
        }
        return testCases;
    }

    private void writeSourceCode(Path tempDir, String language, String sourceCode) throws IOException {
        Path sourceFile = tempDir.resolve(getSourceFileName(language));
        Files.writeString(sourceFile, sourceCode, StandardCharsets.UTF_8);
    }

    private ExecutionResult compileCode(String language, Path tempDir) throws IOException, InterruptedException {
        List<String> command = getCompileCommand(language, tempDir);
        ExecutionResult result = new ExecutionResult();
        if (command == null) {
            result.setCompileSuccess(true);
            return result;
        }

        Process process = new ProcessBuilder(command)
                .directory(tempDir.toFile())
                .start();

        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            result.setCompileSuccess(false);
            result.setError(true);
            result.setErrorMessage("Compilation timeout");
            return result;
        }

        if (process.exitValue() != 0) {
            result.setCompileSuccess(false);
            result.setError(true);
            result.setErrorMessage(readStream(process.getErrorStream()));
            return result;
        }

        result.setCompileSuccess(true);
        return result;
    }

    private String runWithInput(String language, Path tempDir, String input) throws IOException, InterruptedException {
        List<String> command = getRunCommand(language, tempDir);
        Process process = new ProcessBuilder(command)
                .directory(tempDir.toFile())
                .start();

        try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
            if (input != null) {
                writer.write(input);
            }
        }

        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return "TIMEOUT: Execution exceeded " + TIMEOUT_SECONDS + " seconds";
        }

        if (process.exitValue() != 0) {
            String errorOutput = readStream(process.getErrorStream());
            if (errorOutput == null || errorOutput.isBlank()) {
                errorOutput = "Process exited with code " + process.exitValue();
            }
            return "ERROR: " + errorOutput;
        }

        return readStream(process.getInputStream());
    }

    private List<String> getCompileCommand(String language, Path tempDir) {
        return switch (normalizeLanguage(language)) {
            case "java" -> {
                ensureCommandAvailable("javac");
                yield List.of("javac", "-encoding", "UTF-8", tempDir.resolve("Main.java").toString());
            }
            case "python" -> null;
            case "cpp" -> {
                ensureCommandAvailable("g++");
                yield WINDOWS
                        ? List.of("g++", "-o", tempDir.resolve("main.exe").toString(), tempDir.resolve("main.cpp").toString())
                        : List.of("g++", "-o", tempDir.resolve("main").toString(), tempDir.resolve("main.cpp").toString());
            }
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    private List<String> getRunCommand(String language, Path tempDir) {
        return switch (normalizeLanguage(language)) {
            case "java" -> {
                ensureCommandAvailable("java");
                yield List.of("java", "-cp", tempDir.toString(), "Main");
            }
            case "python" -> {
                String pythonCommand = firstAvailableCommand("python3", "python");
                if (pythonCommand == null) {
                    throw new IllegalArgumentException("Python execution is not available on this machine because neither python3 nor python is installed");
                }
                yield List.of(pythonCommand, tempDir.resolve("main.py").toString());
            }
            case "cpp" -> {
                ensureCommandAvailable("g++");
                yield WINDOWS
                        ? List.of(tempDir.resolve("main.exe").toString())
                        : List.of(tempDir.resolve("main").toString());
            }
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    private String getSourceFileName(String language) {
        return switch (normalizeLanguage(language)) {
            case "java" -> "Main.java";
            case "python" -> "main.py";
            case "cpp" -> "main.cpp";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    private String normalizeLanguage(String language) {
        return language.toLowerCase(Locale.ROOT).trim();
    }

    private String readStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (builder.length() > 0) {
                    builder.append(System.lineSeparator());
                }
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private void cleanupDirectory(Path directory) {
        try {
            Files.walk(directory)
                    .sorted((left, right) -> right.compareTo(left))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private long toMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }

    private void ensureCommandAvailable(String command) {
        if (firstAvailableCommand(command) == null) {
            throw new IllegalArgumentException(command + " is not installed or not available in PATH");
        }
    }

    private String firstAvailableCommand(String... commands) {
        for (String command : commands) {
            if (isCommandAvailable(command)) {
                return command;
            }
        }
        return null;
    }

    private boolean isCommandAvailable(String command) {
        String path = System.getenv("PATH");
        if (path == null || path.isBlank()) {
            return false;
        }

        String[] directories = path.split(File.pathSeparator);
        if (WINDOWS) {
            for (String directory : directories) {
                for (String suffix : WINDOWS_EXECUTABLE_SUFFIXES) {
                    File candidate = new File(directory, command + suffix);
                    if (candidate.isFile()) {
                        return true;
                    }
                }
            }
            return false;
        }

        for (String directory : directories) {
            File candidate = new File(directory, command);
            if (candidate.isFile() && candidate.canExecute()) {
                return true;
            }
        }
        return false;
    }
}
