package com.example.pix.application.port.in.model;

import com.example.pix.domain.enums.PixStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PixResponse(
        String transactionId,
        PixStatus status,
        String message
) {
}
