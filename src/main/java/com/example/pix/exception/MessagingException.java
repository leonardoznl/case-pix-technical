package com.example.pix.exception;

public class MessagingException extends RuntimeException {
    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}
