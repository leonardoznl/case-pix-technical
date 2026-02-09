package com.example.pix.adapters.out.webhook;

import com.example.pix.application.port.out.model.PixWebhookRequest;
import com.example.pix.application.port.out.usecase.PixWebhookPort;
import com.example.pix.exception.WebhookException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PixWebhookClient implements PixWebhookPort {
    private static final Logger logger = LoggerFactory.getLogger(PixWebhookClient.class);

    private final RestTemplate webhookRestTemplate;

    @Value("${pix.webhook.url}")
    private String webhookUrl;

    @Override
    public void notify(PixWebhookRequest request) {
        try {
            ResponseEntity<Void> response = webhookRestTemplate.postForEntity(webhookUrl, request, Void.class);
            logger.info(
                    "PIX webhook notified: transactionId={}, status={}, httpStatus={}",
                    request.transactionId(), request.status(), response.getStatusCode().value()
            );
        } catch (Exception ex) {
            logger.error("Failed to notify PIX webhook", ex);
            throw new WebhookException("webhook-notify-failed", ex);
        }
    }
}
