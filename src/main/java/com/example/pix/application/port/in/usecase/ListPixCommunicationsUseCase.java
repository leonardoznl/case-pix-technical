package com.example.pix.application.port.in.usecase;

import com.example.pix.application.port.in.model.PixCommunicationResponse;
import java.util.List;
import java.util.Optional;

public interface ListPixCommunicationsUseCase {
    Optional<List<PixCommunicationResponse>> listByTransactionId(String transactionId);
}
