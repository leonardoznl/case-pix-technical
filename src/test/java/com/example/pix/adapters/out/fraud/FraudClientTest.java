package com.example.pix.adapters.out.fraud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.pix.application.port.in.model.PixPaymentRequest;
import com.example.pix.application.port.out.model.FraudResponse;
import com.example.pix.exception.FraudServiceException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FraudClientTest {
    @Mock
    private FraudFeignClient fraudFeignClient;

    private FraudClient fraudClient;

    @BeforeEach
    void setUp() {
        fraudClient = new FraudClient(fraudFeignClient);
    }

    @Test
    void shouldNormalizeBlankReason_whenResponseReasonBlank() {
        PixPaymentRequest request = PixPaymentRequest.builder()
                .amount(BigDecimal.TEN)
                .payerKey("payer")
                .receiverKey("receiver")
                .build();
        when(fraudFeignClient.check(any())).thenReturn(FraudResponse.builder()
                .approved(false)
                .reason(" ")
                .build());

        FraudResponse response = fraudClient.check(request);

        assertThat(response.reason()).isEqualTo("fraud-rejected");
        assertThat(response.approved()).isFalse();
    }

    @Test
    void shouldThrowException_whenResponseIsNull() {
        PixPaymentRequest request = PixPaymentRequest.builder()
                .amount(BigDecimal.ONE)
                .payerKey("payer")
                .receiverKey("receiver")
                .build();
        when(fraudFeignClient.check(any())).thenReturn(null);

        assertThatThrownBy(() -> fraudClient.check(request))
                .isInstanceOf(FraudServiceException.class)
                .hasMessage("fraud-api-empty-response");
    }
}
