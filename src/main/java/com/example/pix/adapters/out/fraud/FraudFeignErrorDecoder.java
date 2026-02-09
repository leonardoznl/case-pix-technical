package com.example.pix.adapters.out.fraud;

import com.example.pix.exception.FraudServiceException;
import feign.RetryableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FraudFeignErrorDecoder implements ErrorDecoder {
    private static final Logger logger = LoggerFactory.getLogger(FraudFeignErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = readBody(response);
        int status = response.status();
        boolean retryable = isRetryable(status);

        logResponse(status, body, retryable);

        String message = retryable
                ? retryableMessage(status)
                : (status >= 500 ? "fraud-api-unavailable" : "fraud-api-error");

        return retryable
                ? new RetryableException(
                        status,
                        message,
                        response.request().httpMethod(),
                        (Long) null,
                        response.request()
                )
                : new FraudServiceException(message, null);
    }

    private boolean isRetryable(int status) {
        return status == 408 || status == 429 || status >= 500;
    }

    private String retryableMessage(int status) {
        return status == 429 ? "fraud-api-rate-limited" : "fraud-api-retryable";
    }

    private void logResponse(int status, String body, boolean retryable) {
        String withBody = retryable
                ? "Fraud API retryable error: status={}, body={}"
                : "Fraud API error: status={}, body={}";
        String withoutBody = retryable
                ? "Fraud API retryable error: status={}"
                : "Fraud API error: status={}";

        Optional.ofNullable(body)
                .filter(value -> !value.isBlank())
                .ifPresentOrElse(
                        value -> log(retryable, withBody, status, value),
                        () -> log(retryable, withoutBody, status)
                );
    }

    private void log(boolean retryable, String message, Object... args) {
        BiConsumer<String, Object[]> log = retryable
                ? (msg, params) -> logger.warn(msg, params)
                : (msg, params) -> logger.error(msg, params);
        log.accept(message, args);
    }

    private String readBody(Response response) {
        return Optional.ofNullable(response.body())
                .map(body -> {
                    try (InputStream inputStream = body.asInputStream()) {
                        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    } catch (IOException ex) {
                        return null;
                    }
                })
                .orElse(null);
    }
}
