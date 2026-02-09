package com.example.pix.adapters.out.fraud;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record FraudCheckRequest(
        BigDecimal amount,
        String payerKey,
        String receiverKey
) {
}
