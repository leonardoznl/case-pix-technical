package com.example.pix.domain;

import com.example.pix.domain.enums.PixDirection;
import com.example.pix.domain.enums.PixStatus;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PixCommunication(
        String transactionId,
        PixDirection direction,
        PixStatus status,
        String payload,
        String reason,
        Instant createdAt
) {
}
