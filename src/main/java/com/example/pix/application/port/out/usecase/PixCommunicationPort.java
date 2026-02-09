package com.example.pix.application.port.out.usecase;

import com.example.pix.domain.PixCommunication;
import java.util.List;
import java.util.Optional;

public interface PixCommunicationPort {
    void save(PixCommunication communication);

    Optional<PixCommunication> findLatestByTransactionId(String transactionId);

    List<PixCommunication> findByTransactionId(String transactionId);
}
