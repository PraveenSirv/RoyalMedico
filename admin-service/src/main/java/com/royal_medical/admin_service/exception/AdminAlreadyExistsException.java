package com.royal_medical.admin_service.exception;

public class AdminAlreadyExistsException extends RuntimeException {
    public AdminAlreadyExistsException(String message) {
        super(message);
    }
}
