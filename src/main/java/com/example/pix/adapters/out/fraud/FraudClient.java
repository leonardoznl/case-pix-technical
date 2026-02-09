package com.example.pix.adapters.out.fraud;

import com.example.pix.application.port.in.model.PixPaymentRequest;
import com.example.pix.application.port.out.model.FraudResponse;
import com.example.pix.application.port.out.usecase.FraudCheckPort;
import com.example.pix.exception.FraudServiceException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FraudClient implements FraudCheckPort {
    private static final Logger logger = LoggerFactory.getLogger(FraudClient.class);

    private final FraudFeignClient fraudFeignClient;

    @Override
    public FraudResponse check(PixPaymentRequest request) {
        FraudCheckRequest payload = FraudCheckRequest.builder()
                .amount(request.amount())
                .payerKey(request.payerKey())
                .receiverKey(request.receiverKey())
                .build();

        try {
            FraudResponse response = fraudFeignClient.check(payload);
            return normalizeResponse(response);
        } catch (FraudServiceException ex) {
            logger.error("Fraud API error", ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Fraud API error", ex);
            throw new FraudServiceException("fraud-api-error", ex);
        }
    }

    private FraudResponse normalizeResponse(FraudResponse response) {
        return Optional.ofNullable(response)
                .map(this::buildResponse)
                .orElseThrow(() -> new FraudServiceException("fraud-api-empty-response", null));
    }

    private FraudResponse buildResponse(FraudResponse response) {
        String reason = Optional.ofNullable(response.reason())
                .filter(value -> !value.isBlank())
                .orElseGet(() -> response.approved() ? "approved" : "fraud-rejected");
        return FraudResponse.builder()
                .approved(response.approved())
                .reason(reason)
                .build();
    }
}
