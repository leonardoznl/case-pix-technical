package com.example.pix.application.port.out.model;

import lombok.Builder;

@Builder
public record FraudResponse(
        boolean approved,
        String reason
) {
}
