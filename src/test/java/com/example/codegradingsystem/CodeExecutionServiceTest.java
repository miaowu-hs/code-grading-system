package com.example.codegradingsystem;

import com.example.codegradingsystem.model.CodeSubmission;
import com.example.codegradingsystem.model.ExecutionResult;
import com.example.codegradingsystem.model.TestCase;
import com.example.codegradingsystem.service.CodeExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CodeExecutionServiceTest {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @Test
    public void testJavaCodeExecution() {
        String javaCode = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello World\");\n" +
                "    }\n" +
                "}";

        List<TestCase> testCases = Arrays.asList(
            new TestCase("", "Hello World\n")
        );

        CodeSubmission submission = new CodeSubmission();
        submission.setLanguage("java");
        submission.setSourceCode(javaCode);
        submission.setTestCases(testCases);

        ExecutionResult result = codeExecutionService.executeCode(submission);
        
        assertNotNull(result);
        assertTrue(result.isCompileSuccess());
        assertTrue(result.isRuntimeSuccess());
        assertEquals("Hello World\n", result.getOutput());
    }

    @Test
    public void testPythonCodeExecution() {
        String pythonCode = "print('Hello Python')";

        List<TestCase> testCases = Arrays.asList(
            new TestCase("", "Hello Python\n")
        );

        CodeSubmission submission = new CodeSubmission();
        submission.setLanguage("python");
        submission.setSourceCode(pythonCode);
        submission.setTestCases(testCases);

        ExecutionResult result = codeExecutionService.executeCode(submission);
        
        assertNotNull(result);
        assertTrue(result.isRuntimeSuccess());
        assertEquals("Hello Python\n", result.getOutput());
    }

    @Test
    public void testCppCodeExecution() {
        String cppCode = "#include <iostream>\n" +
                "int main() {\n" +
                "    std::cout << \"Hello C++\" << std::endl;\n" +
                "    return 0;\n" +
                "}";

        List<TestCase> testCases = Arrays.asList(
            new TestCase("", "Hello C++\n")
        );

        CodeSubmission submission = new CodeSubmission();
        submission.setLanguage("cpp");
        submission.setSourceCode(cppCode);
        submission.setTestCases(testCases);

        ExecutionResult result = codeExecutionService.executeCode(submission);
        
        assertNotNull(result);
        assertTrue(result.isCompileSuccess());
        assertTrue(result.isRuntimeSuccess());
        assertEquals("Hello C++\n", result.getOutput());
    }

    @Test
    public void testCompilationError() {
        String invalidJavaCode = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(;\n" + // Syntax error
                "    }\n" +
                "}";

        List<TestCase> testCases = Arrays.asList(
            new TestCase("", "")
        );

        CodeSubmission submission = new CodeSubmission();
        submission.setLanguage("java");
        submission.setSourceCode(invalidJavaCode);
        submission.setTestCases(testCases);

        ExecutionResult result = codeExecutionService.executeCode(submission);
        
        assertNotNull(result);
        assertFalse(result.isCompileSuccess());
        assertNotNull(result.getErrorMessage());
    }
}