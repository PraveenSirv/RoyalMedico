package com.royal_medical.customer_service.exception;

import com.royal_medical.customer_service.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request));
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCustomerExists(CustomerAlreadyExistsException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, request));
    }

    private ErrorResponse buildErrorResponse(String message, HttpStatus status, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }
}
