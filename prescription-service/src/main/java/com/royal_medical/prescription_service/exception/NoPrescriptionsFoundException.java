package com.royal_medical.prescription_service.exception;

public class NoPrescriptionsFoundException extends RuntimeException {
    public NoPrescriptionsFoundException(String message) {
        super(message);
    }
}

