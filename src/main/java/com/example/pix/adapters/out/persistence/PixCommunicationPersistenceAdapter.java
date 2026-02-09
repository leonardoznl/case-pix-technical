package com.example.pix.adapters.out.persistence;

import com.example.pix.application.port.out.usecase.PixCommunicationPort;
import com.example.pix.domain.PixCommunication;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PixCommunicationPersistenceAdapter implements PixCommunicationPort {
    private static final Logger logger = LoggerFactory.getLogger(PixCommunicationPersistenceAdapter.class);

    private final PixCommunicationRepository repository;

    @Override
    public void save(PixCommunication communication) {
        PixCommunicationEntity entity = PixCommunicationEntity.builder()
                .transactionId(communication.transactionId())
                .direction(communication.direction())
                .status(communication.status())
                .reason(communication.reason())
                .payload(communication.payload())
                .createdAt(communication.createdAt())
                .build();

        repository.save(entity);
        logger.info(
                "PIX communication persisted: transactionId={}, direction={}, status={}",
                communication.transactionId(), communication.direction(), communication.status()
        );
    }

    @Override
    public Optional<PixCommunication> findLatestByTransactionId(String transactionId) {
        return repository.findTopByTransactionIdOrderByCreatedAtDesc(transactionId)
                .map(this::toDomain);
    }

    @Override
    public List<PixCommunication> findByTransactionId(String transactionId) {
        return repository.findByTransactionIdOrderByCreatedAtAsc(transactionId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private PixCommunication toDomain(PixCommunicationEntity entity) {
        return PixCommunication.builder()
                .transactionId(entity.getTransactionId())
                .direction(entity.getDirection())
                .status(entity.getStatus())
                .reason(entity.getReason())
                .payload(entity.getPayload())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

