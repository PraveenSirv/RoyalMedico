package com.royal_medical.billing_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<?> handleExternalServiceException(ExternalServiceException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE,request), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(BillNotFoundException.class)
    public ResponseEntity<?> handleBillNotFoundException(BillNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex, HttpStatus.NOT_FOUND,request), HttpStatus.NOT_FOUND);
    }
    // Add other exception handlers as needed...

    private Map<String, Object> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return body;
    }
}
