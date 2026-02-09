package com.example.pix.application.port.in.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PixPaymentRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String payerKey,
        @NotBlank String receiverKey,
        String description
) {
}
