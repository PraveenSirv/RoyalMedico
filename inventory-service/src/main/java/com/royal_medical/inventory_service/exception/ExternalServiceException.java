package com.royal_medical.inventory_service.exception;


public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }
}