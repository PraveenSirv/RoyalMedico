package com.royal_medical.admin_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<?> handleCustomerNotFound(CustomerNotFoundException ex){
        return new ResponseEntity<>(buildError(ex.getMessage(), HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<?> handleAdminNotFound(AdminNotFoundException ex) {
        return new ResponseEntity<>(buildError(ex.getMessage(), HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomerServiceException.class)
    public ResponseEntity<?> handleCustomerServiceError(CustomerServiceException ex) {
        return new ResponseEntity<>(buildError(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllOtherErrors(Exception ex) {
        return new ResponseEntity<>(buildError("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AdminAlreadyExistsException.class)
    public ResponseEntity<?> handleAdminAlreadyExists(AdminAlreadyExistsException ex) {
        return new ResponseEntity<>(buildError(ex.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
    }

    private Map<String, Object> buildError(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return error;
    }
}
