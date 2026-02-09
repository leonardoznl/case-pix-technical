package com.example.pix.domain;

import com.example.pix.domain.enums.PixStatus;

public record PixResponseMessage(
        String transactionId,
        PixStatus status,
        String reason
) {
}
