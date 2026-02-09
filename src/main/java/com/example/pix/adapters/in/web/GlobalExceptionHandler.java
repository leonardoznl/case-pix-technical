package com.example.pix.adapters.in.web;

import com.example.pix.exception.PixProcessingException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR_TYPE = "pix";
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setType(URI.create(ERROR_TYPE));
        detail.setTitle("validation-error");
        detail.setProperty("fields", errors);
        return detail;
    }

    @ExceptionHandler(PixProcessingException.class)
    public ProblemDetail handlePixProcessing(PixProcessingException ex) {
        logger.error("PIX processing failure: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        detail.setType(URI.create(ERROR_TYPE));
        detail.setTitle("processing-error");
        detail.setDetail(ex.getMessage());
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        logger.error("Unexpected error", ex);
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        detail.setType(URI.create(ERROR_TYPE));
        detail.setTitle("unexpected-error");
        detail.setDetail("unexpected-error");
        return detail;
    }
}
