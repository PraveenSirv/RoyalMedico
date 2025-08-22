package com.royal_medical.payment_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Helper method to build error response
    private Map<String, Object> buildErrorResponse(String message, HttpStatus status, WebRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", message);
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        return errorDetails;
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<?> handlePaymentNotFound(PaymentNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND,request));
    }


    // Custom exception: External service failure
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<?> handleExternalService(ExternalServiceException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, request), HttpStatus.SERVICE_UNAVAILABLE);
    }

    // Custom exception: Invalid request
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<?> handleInvalidRequest(InvalidRequestException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request), HttpStatus.BAD_REQUEST);
    }

    // Custom exception: Bill not found
    @ExceptionHandler(BillNotFoundException.class)
    public ResponseEntity<?> handleBillNotFound(BillNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request), HttpStatus.NOT_FOUND);
    }


    // Global fallback for unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse("An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR, request), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
