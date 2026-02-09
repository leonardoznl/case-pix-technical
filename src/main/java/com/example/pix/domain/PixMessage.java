package com.example.pix.domain;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PixMessage(
        String transactionId,
        BigDecimal amount,
        String payerKey,
        String receiverKey,
        String description,
        Instant createdAt
) {
}
