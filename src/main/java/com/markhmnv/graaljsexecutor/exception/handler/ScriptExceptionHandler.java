package com.markhmnv.graaljsexecutor.exception.handler;

import com.markhmnv.graaljsexecutor.exception.EvaluationException;
import com.markhmnv.graaljsexecutor.exception.IllegalDeletionException;
import com.markhmnv.graaljsexecutor.exception.ScriptExecutionStopException;
import com.markhmnv.graaljsexecutor.exception.ScriptNotFoundException;
import com.markhmnv.graaljsexecutor.util.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ScriptExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            EvaluationException.class,
            ScriptExecutionStopException.class
    })
    public ErrorResponse handlerBadRequest(RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ScriptNotFoundException.class)
    public ErrorResponse handlerScriptNotFoundException(ScriptNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalDeletionException.class)
    public ErrorResponse handlerIllegalDeletionException(IllegalDeletionException e) {
        return new ErrorResponse(e.getMessage());
    }
}
