package com.example.pix.application.service;

import com.example.pix.application.port.in.model.PixPaymentRequest;
import com.example.pix.application.port.in.model.PixResponse;
import com.example.pix.application.port.in.usecase.ProcessPixUseCase;
import com.example.pix.application.port.out.model.FraudResponse;
import com.example.pix.application.port.out.usecase.FraudCheckPort;
import com.example.pix.application.port.out.usecase.PixCommunicationPort;
import com.example.pix.application.port.out.usecase.PixMessagePublisherPort;
import com.example.pix.domain.PixCommunication;
import com.example.pix.domain.PixMessage;
import com.example.pix.domain.enums.PixDirection;
import com.example.pix.domain.enums.PixStatus;
import com.example.pix.exception.PixProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PixProcessingService implements ProcessPixUseCase {
    private static final Logger logger = LoggerFactory.getLogger(PixProcessingService.class);

    private final FraudCheckPort fraudCheckPort;
    private final PixMessagePublisherPort messagePublisherPort;
    private final PixCommunicationPort communicationPort;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @Override
    public PixResponse process(PixPaymentRequest request) {
        meterRegistry.counter("pix.requests").increment();

        FraudResponse fraudResponse;
        try {
            fraudResponse = fraudCheckPort.check(request);
        } catch (Exception ex) {
            meterRegistry.counter("pix.fraud.failed").increment();
            logger.error("Fraud check failed", ex);
            throw new PixProcessingException("fraud-check-failed");
        }

        return fraudResponse.approved()
                ? processApproved(request)
                : rejectFraud(fraudResponse);
    }

    private PixResponse rejectFraud(FraudResponse fraudResponse) {
        meterRegistry.counter("pix.fraud.rejected").increment();
        logger.info("PIX rejected by fraud: reason={}", fraudResponse.reason());
        return PixResponse.builder()
                .status(PixStatus.REJECTED_FRAUD)
                .message(fraudResponse.reason())
                .build();
    }

    private PixResponse processApproved(PixPaymentRequest request) {
        String transactionId = UUID.randomUUID().toString();
        PixMessage message = PixMessage.builder()
                .transactionId(transactionId)
                .amount(request.amount())
                .payerKey(request.payerKey())
                .receiverKey(request.receiverKey())
                .description(request.description())
                .createdAt(Instant.now())
                .build();

        String payload = toJson(message);
        saveCommunication(transactionId, PixStatus.PROCESSING, payload);
        try {
            messagePublisherPort.publish(message);
            saveCommunication(transactionId, PixStatus.SENT_TO_BC, payload);
            meterRegistry.counter("pix.mq.published").increment();
        } catch (Exception ex) {
            meterRegistry.counter("pix.mq.publish.failed").increment();
            logger.error("Failed to publish PIX message", ex);
            savePublishError(transactionId, payload);
            throw new PixProcessingException("mq-publish-failed");
        }

        return PixResponse.builder()
                .transactionId(transactionId)
                .status(PixStatus.PROCESSING)
                .message("sent-to-banco-central")
                .build();
    }

    private void saveCommunication(String transactionId, PixStatus status, String payload) {
        communicationPort.save(PixCommunication.builder()
                .transactionId(transactionId)
                .direction(PixDirection.REQUEST)
                .status(status)
                .payload(payload)
                .createdAt(Instant.now())
                .build());
    }

    private void savePublishError(String transactionId, String payload) {
        try {
            saveCommunication(transactionId, PixStatus.ERROR, payload);
        } catch (Exception ex) {
            logger.error("Failed to persist PIX error status", ex);
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new PixProcessingException("payload-serialization-failed");
        }
    }
}
