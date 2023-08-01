package com.markhmnv.graaljsexecutor.exception;

public class ScriptExecutionStopException extends RuntimeException {
    public ScriptExecutionStopException() {
        super("You can't stop script that is not executing");
    }
}