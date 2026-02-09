package com.example.pix.application.service;

import com.example.pix.application.port.in.usecase.GetPixStatusUseCase;
import com.example.pix.application.port.in.usecase.ListPixCommunicationsUseCase;
import com.example.pix.application.port.in.model.PixCommunicationResponse;
import com.example.pix.application.port.in.model.PixStatusResponse;
import com.example.pix.application.port.out.usecase.PixCommunicationPort;
import com.example.pix.domain.PixCommunication;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PixQueryService implements GetPixStatusUseCase, ListPixCommunicationsUseCase {
    private final PixCommunicationPort communicationPort;

    @Override
    public Optional<PixStatusResponse> getStatus(String transactionId) {
        return communicationPort.findLatestByTransactionId(transactionId)
                .map(this::toStatusResult);
    }

    @Override
    public Optional<List<PixCommunicationResponse>> listByTransactionId(String transactionId) {
        List<PixCommunication> communications = communicationPort.findByTransactionId(transactionId);
        return Optional.of(communications)
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream()
                        .map(this::toCommunicationResult)
                        .toList());
    }

    private PixStatusResponse toStatusResult(PixCommunication communication) {
        return PixStatusResponse.builder()
                .transactionId(communication.transactionId())
                .status(communication.status())
                .direction(communication.direction())
                .reason(communication.reason())
                .updatedAt(communication.createdAt())
                .build();
    }

    private PixCommunicationResponse toCommunicationResult(PixCommunication communication) {
        return PixCommunicationResponse.builder()
                .direction(communication.direction())
                .status(communication.status())
                .payload(communication.payload())
                .createdAt(communication.createdAt())
                .build();
    }
}
