package com.example.pix.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PixCommunicationRepository extends MongoRepository<PixCommunicationEntity, String> {
    Optional<PixCommunicationEntity> findTopByTransactionIdOrderByCreatedAtDesc(String transactionId);

    List<PixCommunicationEntity> findByTransactionIdOrderByCreatedAtAsc(String transactionId);
}
