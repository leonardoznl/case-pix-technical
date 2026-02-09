package com.example.pix.application.port.out.model;

import com.example.pix.domain.enums.PixStatus;
import java.time.Instant;

public record PixWebhookRequest(
        String transactionId,
        PixStatus status,
        String reason,
        Instant occurredAt
) {
}
