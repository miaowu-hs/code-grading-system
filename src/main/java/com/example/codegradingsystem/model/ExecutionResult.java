package com.example.codegradingsystem.model;

import java.util.ArrayList;
import java.util.List;

public class ExecutionResult {
    private boolean compileSuccess;
    private boolean runtimeSuccess;
    private boolean error;
    private String output;
    private List<String> outputs = new ArrayList<>();
    private String errorMessage;
    private long executionTime;

    public boolean isCompileSuccess() {
        return compileSuccess;
    }

    public void setCompileSuccess(boolean compileSuccess) {
        this.compileSuccess = compileSuccess;
    }

    public boolean isRuntimeSuccess() {
        return runtimeSuccess;
    }

    public void setRuntimeSuccess(boolean runtimeSuccess) {
        this.runtimeSuccess = runtimeSuccess;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<String> outputs) {
        this.outputs = outputs == null ? new ArrayList<>() : outputs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
}
