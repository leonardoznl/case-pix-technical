package com.example.pix.application.port.in.model;

import com.example.pix.domain.enums.PixDirection;
import com.example.pix.domain.enums.PixStatus;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PixCommunicationResponse(
        PixDirection direction,
        PixStatus status,
        String payload,
        Instant createdAt
) {
}
