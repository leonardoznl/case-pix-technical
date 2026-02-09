package com.example.pix.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.pix.application.port.out.usecase.PixCommunicationPort;
import com.example.pix.domain.PixCommunication;
import com.example.pix.domain.enums.PixDirection;
import com.example.pix.domain.enums.PixStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PixQueryServiceTest {
    @Mock
    private PixCommunicationPort communicationPort;

    private PixQueryService service;

    @BeforeEach
    void setUp() {
        service = new PixQueryService(communicationPort);
    }

    @Test
    void shouldReturnEmpty_whenNoCommunications() {
        when(communicationPort.findByTransactionId("tx-1")).thenReturn(List.of());

        Optional<?> result = service.listByTransactionId("tx-1");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldMapCommunications_whenPresent() {
        Instant now = Instant.parse("2026-02-07T13:00:00Z");
        PixCommunication communication = PixCommunication.builder()
                .transactionId("tx-1")
                .direction(PixDirection.REQUEST)
                .status(PixStatus.SENT_TO_BC)
                .payload("payload")
                .createdAt(now)
                .build();
        when(communicationPort.findByTransactionId("tx-1")).thenReturn(List.of(communication));

        var result = service.listByTransactionId("tx-1");

        assertThat(result).isPresent();
        var first = result.get().getFirst();
        assertThat(first.direction()).isEqualTo(PixDirection.REQUEST);
        assertThat(first.status()).isEqualTo(PixStatus.SENT_TO_BC);
        assertThat(first.payload()).isEqualTo("payload");
        assertThat(first.createdAt()).isEqualTo(now);
    }

    @Test
    void shouldReturnLatestStatus_whenAvailable() {
        Instant now = Instant.parse("2026-02-07T13:00:00Z");
        PixCommunication communication = PixCommunication.builder()
                .transactionId("tx-2")
                .direction(PixDirection.RESPONSE)
                .status(PixStatus.CONFIRMED)
                .payload("payload")
                .createdAt(now)
                .build();
        when(communicationPort.findLatestByTransactionId("tx-2"))
                .thenReturn(Optional.of(communication));

        var result = service.getStatus("tx-2");

        assertThat(result).isPresent();
        assertThat(result.get().transactionId()).isEqualTo("tx-2");
        assertThat(result.get().status()).isEqualTo(PixStatus.CONFIRMED);
        assertThat(result.get().direction()).isEqualTo(PixDirection.RESPONSE);
        assertThat(result.get().updatedAt()).isEqualTo(now);
    }
}
