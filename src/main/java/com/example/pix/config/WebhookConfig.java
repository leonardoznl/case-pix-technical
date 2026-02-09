package com.example.pix.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebhookConfig {
    @Bean
    public RestTemplate webhookRestTemplate(
            RestTemplateBuilder builder,
            @Value("${pix.webhook.timeout-ms:2000}") long timeoutMs
    ) {
        Duration timeout = Duration.ofMillis(timeoutMs);
        return builder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }
}
