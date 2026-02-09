package com.example.pix.application.service;

import com.example.pix.application.port.in.usecase.HandlePixResponseUseCase;
import com.example.pix.application.port.out.model.PixWebhookRequest;
import com.example.pix.application.port.out.usecase.PixCommunicationPort;
import com.example.pix.application.port.out.usecase.PixWebhookPort;
import com.example.pix.domain.PixCommunication;
import com.example.pix.domain.enums.PixDirection;
import com.example.pix.domain.PixResponseMessage;
import com.example.pix.exception.PixProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PixResponseService implements HandlePixResponseUseCase {
    private static final Logger logger = LoggerFactory.getLogger(PixResponseService.class);

    private final PixCommunicationPort communicationPort;
    private final PixWebhookPort webhookPort;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @Override
    public void handle(PixResponseMessage response) {
        try {
            Instant now = Instant.now();
            communicationPort.save(PixCommunication.builder()
                    .transactionId(response.transactionId())
                    .direction(PixDirection.RESPONSE)
                    .status(response.status())
                    .reason(response.reason())
                    .payload(toJson(response))
                    .createdAt(now)
                    .build());
            meterRegistry.counter("pix.bc.responses").increment();
            logger.info(
                    "PIX response stored: transactionId={}, status={}",
                    response.transactionId(), response.status()
            );
            notifyWebhook(response, now);
        } catch (Exception ex) {
            meterRegistry.counter("pix.bc.responses.failed").increment();
            logger.error("Failed to store PIX response", ex);
            throw new PixProcessingException("response-persist-failed");
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new PixProcessingException("payload-serialization-failed");
        }
    }

    private void notifyWebhook(PixResponseMessage response, Instant occurredAt) {
        try {
            webhookPort.notify(new PixWebhookRequest(
                    response.transactionId(),
                    response.status(),
                    response.reason(),
                    occurredAt
            ));
            meterRegistry.counter("pix.webhook.sent").increment();
        } catch (Exception ex) {
            meterRegistry.counter("pix.webhook.failed").increment();
            logger.error("Failed to notify webhook", ex);
        }
    }
}
