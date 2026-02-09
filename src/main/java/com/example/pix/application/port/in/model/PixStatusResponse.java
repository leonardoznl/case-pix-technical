package com.example.pix.application.port.in.model;

import com.example.pix.domain.enums.PixDirection;
import com.example.pix.domain.enums.PixStatus;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PixStatusResponse(
        String transactionId,
        PixStatus status,
        PixDirection direction,
        String reason,
        Instant updatedAt
) {
}
