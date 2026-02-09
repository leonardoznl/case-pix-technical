package com.example.pix.exception;

public class FraudServiceException extends RuntimeException {
    public FraudServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
