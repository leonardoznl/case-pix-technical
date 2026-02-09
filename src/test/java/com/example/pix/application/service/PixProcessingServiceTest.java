package com.example.pix.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.pix.application.port.in.model.PixPaymentRequest;
import com.example.pix.application.port.in.model.PixResponse;
import com.example.pix.application.port.out.model.FraudResponse;
import com.example.pix.application.port.out.usecase.FraudCheckPort;
import com.example.pix.application.port.out.usecase.PixCommunicationPort;
import com.example.pix.application.port.out.usecase.PixMessagePublisherPort;
import com.example.pix.domain.PixCommunication;
import com.example.pix.domain.PixMessage;
import com.example.pix.domain.enums.PixDirection;
import com.example.pix.domain.enums.PixStatus;
import com.example.pix.exception.PixProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PixProcessingServiceTest {
    @Mock
    private FraudCheckPort fraudCheckPort;
    @Mock
    private PixMessagePublisherPort messagePublisherPort;
    @Mock
    private PixCommunicationPort communicationPort;
    @Mock
    private ObjectMapper objectMapper;

    private SimpleMeterRegistry meterRegistry;
    private PixProcessingService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new PixProcessingService(
                fraudCheckPort,
                messagePublisherPort,
                communicationPort,
                objectMapper,
                meterRegistry
        );
    }

    @Test
    void shouldReturnRejected_whenFraudRejected() {
        PixPaymentRequest request = PixPaymentRequest.builder()
                .amount(BigDecimal.valueOf(10.50))
                .payerKey("payer")
                .receiverKey("receiver")
                .description("desc")
                .build();
        when(fraudCheckPort.check(request)).thenReturn(FraudResponse.builder()
                .approved(false)
                .reason("blocked")
                .build());

        PixResponse result = service.process(request);

        assertThat(result.transactionId()).isNull();
        assertThat(result.status()).isEqualTo(PixStatus.REJECTED_FRAUD);
        assertThat(result.message()).isEqualTo("blocked");
        assertThat(meterRegistry.get("pix.requests").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("pix.fraud.rejected").counter().count()).isEqualTo(1.0);

        verifyNoInteractions(messagePublisherPort, communicationPort, objectMapper);
    }

    @Test
    void shouldPublishAndPersist_whenFraudApproved() throws Exception {
        PixPaymentRequest request = PixPaymentRequest.builder()
                .amount(BigDecimal.valueOf(99.99))
                .payerKey("payer")
                .receiverKey("receiver")
                .description("order-123")
                .build();
        when(fraudCheckPort.check(request)).thenReturn(FraudResponse.builder()
                .approved(true)
                .reason("approved")
                .build());
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        PixResponse result = service.process(request);

        ArgumentCaptor<PixCommunication> communicationCaptor = ArgumentCaptor.forClass(PixCommunication.class);
        ArgumentCaptor<PixMessage> messageCaptor = ArgumentCaptor.forClass(PixMessage.class);
        InOrder inOrder = inOrder(communicationPort, messagePublisherPort);
        inOrder.verify(communicationPort).save(communicationCaptor.capture());
        inOrder.verify(messagePublisherPort).publish(messageCaptor.capture());
        inOrder.verify(communicationPort).save(communicationCaptor.capture());

        PixMessage message = messageCaptor.getValue();
        PixCommunication processingCommunication = communicationCaptor.getAllValues().get(0);
        PixCommunication sentCommunication = communicationCaptor.getAllValues().get(1);

        assertThat(result.transactionId()).isEqualTo(message.transactionId());
        assertThat(result.status()).isEqualTo(PixStatus.PROCESSING);
        assertThat(result.message()).isEqualTo("sent-to-banco-central");

        assertThat(processingCommunication.transactionId()).isEqualTo(message.transactionId());
        assertThat(processingCommunication.direction()).isEqualTo(PixDirection.REQUEST);
        assertThat(processingCommunication.status()).isEqualTo(PixStatus.PROCESSING);
        assertThat(processingCommunication.payload()).isEqualTo("{\"ok\":true}");

        assertThat(sentCommunication.transactionId()).isEqualTo(message.transactionId());
        assertThat(sentCommunication.direction()).isEqualTo(PixDirection.REQUEST);
        assertThat(sentCommunication.status()).isEqualTo(PixStatus.SENT_TO_BC);
        assertThat(sentCommunication.payload()).isEqualTo("{\"ok\":true}");

        verify(objectMapper).writeValueAsString(any());
        assertThat(meterRegistry.get("pix.mq.published").counter().count()).isEqualTo(1.0);
    }

    @Test
    void shouldPersistError_whenPublishFails() throws Exception {
        PixPaymentRequest request = PixPaymentRequest.builder()
                .amount(BigDecimal.valueOf(25.00))
                .payerKey("payer")
                .receiverKey("receiver")
                .description("order-999")
                .build();
        when(fraudCheckPort.check(request)).thenReturn(FraudResponse.builder()
                .approved(true)
                .reason("approved")
                .build());
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");
        RuntimeException publishException = new RuntimeException("publish-down");
        org.mockito.Mockito.doThrow(publishException).when(messagePublisherPort).publish(any());

        assertThatThrownBy(() -> service.process(request))
                .isInstanceOf(PixProcessingException.class)
                .hasMessage("mq-publish-failed");

        ArgumentCaptor<PixCommunication> communicationCaptor = ArgumentCaptor.forClass(PixCommunication.class);
        InOrder inOrder = inOrder(communicationPort, messagePublisherPort);
        inOrder.verify(communicationPort).save(communicationCaptor.capture());
        inOrder.verify(messagePublisherPort).publish(any(PixMessage.class));
        inOrder.verify(communicationPort).save(communicationCaptor.capture());

        PixCommunication processingCommunication = communicationCaptor.getAllValues().get(0);
        PixCommunication errorCommunication = communicationCaptor.getAllValues().get(1);

        assertThat(processingCommunication.status()).isEqualTo(PixStatus.PROCESSING);
        assertThat(errorCommunication.status()).isEqualTo(PixStatus.ERROR);
        assertThat(meterRegistry.get("pix.mq.publish.failed").counter().count()).isEqualTo(1.0);
    }
}
