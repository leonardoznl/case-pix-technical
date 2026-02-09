package com.example.pix.exception;

public class WebhookException extends RuntimeException {
    public WebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
