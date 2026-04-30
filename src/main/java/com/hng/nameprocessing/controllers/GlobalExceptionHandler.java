package com.hng.nameprocessing.controllers;

import com.hng.nameprocessing.dtos.ErrorDto;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.concurrent.CompletionException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        LOGGER.error("CONTENT IS EMPTY");
        ErrorDto errorDto = ErrorDto.builder()
                .message("Request submitted without content")
                .build();
        return ResponseEntity
                .status(400)
                .body(errorDto);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        org.springframework.validation.FieldError error = ex.getBindingResult().getFieldError();

        assert error != null;
        String message = error.getDefaultMessage();
        assert message != null;

        HttpStatus statusCode;

        if (message.contains("blank")) {
            LOGGER.error("BLANK INPUT ERROR");
            statusCode = HttpStatus.valueOf(400);
        } else if (message.contains("letters")||message.contains("query")) {
            LOGGER.error("INVALID INPUT ERROR");
            statusCode = HttpStatus.valueOf(422);
        } else {
            LOGGER.error("ANONYMOUS INPUT ERROR");
            statusCode = HttpStatus.BAD_REQUEST;
        }

        ErrorDto errorDto = ErrorDto.builder()
                .message(message)
                .build();
        return ResponseEntity
                .status(statusCode)
                .body(errorDto);
    }

    // Error handling for non-string name validation failure
    @Override
    protected @Nullable ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        LOGGER.error("TYPE MISMATCH OCCURRED" + ex.getMessage() +"required " + ex.getRequiredType()+" "+request.getDescription(true));
        ErrorDto errorDto = ErrorDto.builder()
                .message("Input name cannot be processed")
                .build();
        return ResponseEntity
                .status(422)
                .body(errorDto);
    }

    // Error handling for missing parameter exception
    @Override
    protected @Nullable ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        LOGGER.error("INPUT PARAMETER MISSING");
        ErrorDto errorDto = ErrorDto.builder()
                .message("Input parameter is missing")
                .build();
        return ResponseEntity
                .status(400)
                .body(errorDto);
    }

    // Handling Service Validation Exceptions
    @ExceptionHandler(ServiceValidationException.class)
    public ResponseEntity<ErrorDto> handleNoPredictionException(ServiceValidationException e){
        LOGGER.error("NO PREDICTION ERROR");
        ErrorDto error = ErrorDto.builder()
                .message(e.getMessage())
                .build();

        return ResponseEntity
                .status(e.getCode())
                .body(error);
    }

    // Error Handling for Request Parameter
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handle(ConstraintViolationException ex) {

        var violation = ex.getConstraintViolations().iterator().next();

        String message = violation.getMessage();

        HttpStatus status;

        if (message.contains("blank")) {
            LOGGER.error("BLANK INPUT ERROR");
            status = HttpStatus.valueOf(400);
        } else if (message.contains("letters")|message.contains("query")) {
            LOGGER.error("INVALID INPUT ERROR");
            status = HttpStatus.valueOf(422);
        } else {
            LOGGER.error("ANONYMOUS INPUT ERROR");
            status = HttpStatus.BAD_REQUEST;
        }

        ErrorDto error = ErrorDto.builder()
                .message(message)
                .build();

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
