package com.royal_medical.billing_service.exception;

public class BillNotFoundException extends RuntimeException {
    public BillNotFoundException(String message) {
        super(message);
    }
}
