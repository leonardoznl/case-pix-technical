package com.example.pix.adapters.out.fraud;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FraudFeignConfig {
    @Bean
    public ErrorDecoder fraudErrorDecoder() {
        return new FraudFeignErrorDecoder();
    }

    @Bean
    public Retryer fraudRetryer(
            @Value("${pix.fraud.retry.initial-interval-ms:200}") long initialIntervalMs,
            @Value("${pix.fraud.retry.max-interval-ms:1000}") long maxIntervalMs,
            @Value("${pix.fraud.retry.max-attempts:3}") int maxAttempts
    ) {
        return new Retryer.Default(initialIntervalMs, maxIntervalMs, maxAttempts);
    }

    @Bean
    public Request.Options fraudRequestOptions(@Value("${pix.fraud.timeout-ms}") long timeoutMs) {
        Duration timeout = Duration.ofMillis(timeoutMs);
        return new Request.Options(timeout, timeout, true);
    }

    @Bean
    public Logger.Level fraudFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
