package com.example.pix.application.port.in.usecase;

import com.example.pix.application.port.in.model.PixStatusResponse;
import java.util.Optional;

public interface GetPixStatusUseCase {
    Optional<PixStatusResponse> getStatus(String transactionId);
}
