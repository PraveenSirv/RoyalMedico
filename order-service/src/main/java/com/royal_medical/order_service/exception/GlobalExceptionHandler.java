package com.royal_medical.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<?> handleInvalidRequestException(InvalidRequestException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<?> handleExternalServiceException(ExternalServiceException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllOtherExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse("Internal Server Error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<?> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
