package com.example.pix.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pix.application.port.out.model.PixWebhookRequest;
import com.example.pix.application.port.out.usecase.PixCommunicationPort;
import com.example.pix.application.port.out.usecase.PixWebhookPort;
import com.example.pix.domain.PixCommunication;
import com.example.pix.domain.PixResponseMessage;
import com.example.pix.domain.enums.PixDirection;
import com.example.pix.domain.enums.PixStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PixResponseServiceTest {
    @Mock
    private PixCommunicationPort communicationPort;
    @Mock
    private PixWebhookPort webhookPort;
    @Mock
    private ObjectMapper objectMapper;

    private SimpleMeterRegistry meterRegistry;
    private PixResponseService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new PixResponseService(communicationPort, webhookPort, objectMapper, meterRegistry);
    }

    @Test
    void shouldPersistResponseAndIncrementMetric_whenHandled() throws Exception {
        PixResponseMessage response = new PixResponseMessage("tx-1", PixStatus.CONFIRMED, "approved");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        service.handle(response);

        ArgumentCaptor<PixCommunication> captor = ArgumentCaptor.forClass(PixCommunication.class);
        verify(communicationPort).save(captor.capture());

        PixCommunication saved = captor.getValue();
        assertThat(saved.transactionId()).isEqualTo("tx-1");
        assertThat(saved.direction()).isEqualTo(PixDirection.RESPONSE);
        assertThat(saved.status()).isEqualTo(PixStatus.CONFIRMED);
        assertThat(saved.payload()).isEqualTo("{\"ok\":true}");
        assertThat(saved.createdAt()).isNotNull();

        ArgumentCaptor<PixWebhookRequest> webhookCaptor = ArgumentCaptor.forClass(PixWebhookRequest.class);
        verify(webhookPort).notify(webhookCaptor.capture());
        PixWebhookRequest webhookRequest = webhookCaptor.getValue();
        assertThat(webhookRequest.transactionId()).isEqualTo("tx-1");
        assertThat(webhookRequest.status()).isEqualTo(PixStatus.CONFIRMED);
        assertThat(webhookRequest.reason()).isEqualTo("approved");
        assertThat(webhookRequest.occurredAt()).isNotNull();

        assertThat(meterRegistry.get("pix.bc.responses").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("pix.webhook.sent").counter().count()).isEqualTo(1.0);
    }

    @Test
    void shouldNotFail_whenWebhookFails() throws Exception {
        PixResponseMessage response = new PixResponseMessage("tx-2", PixStatus.REJECTED_BC, "rejected");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");
        doThrow(new RuntimeException("webhook-down")).when(webhookPort).notify(any());

        service.handle(response);

        verify(communicationPort).save(any());
        verify(webhookPort).notify(any());
        assertThat(meterRegistry.get("pix.bc.responses").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("pix.webhook.failed").counter().count()).isEqualTo(1.0);
    }
}
