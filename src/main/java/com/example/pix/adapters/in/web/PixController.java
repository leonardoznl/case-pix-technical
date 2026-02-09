package com.example.pix.adapters.in.web;

import com.example.pix.application.port.in.usecase.GetPixStatusUseCase;
import com.example.pix.application.port.in.usecase.ListPixCommunicationsUseCase;
import com.example.pix.application.port.in.model.PixCommunicationResponse;
import com.example.pix.application.port.in.model.PixPaymentRequest;
import com.example.pix.application.port.in.model.PixResponse;
import com.example.pix.application.port.in.model.PixStatusResponse;
import com.example.pix.application.port.in.usecase.ProcessPixUseCase;
import com.example.pix.domain.enums.PixStatus;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pix")
@RequiredArgsConstructor
public class PixController {
    private static final Logger logger = LoggerFactory.getLogger(PixController.class);

    private final ProcessPixUseCase processPixUseCase;
    private final GetPixStatusUseCase getPixStatusUseCase;
    private final ListPixCommunicationsUseCase listPixCommunicationsUseCase;

    @PostMapping
    public ResponseEntity<PixResponse> create(@Valid @RequestBody PixPaymentRequest request) {
        logger.info("PIX request received: payerKey={}, receiverKey={}, amount={}",
                request.payerKey(), request.receiverKey(), request.amount());

        PixResponse result = processPixUseCase.process(request);

        HttpStatus status = result.status() == PixStatus.REJECTED_FRAUD
                ? HttpStatus.UNPROCESSABLE_ENTITY
                : HttpStatus.ACCEPTED;

        return ResponseEntity.status(status).body(result);
    }

    @GetMapping("/{transactionId}/status")
    public ResponseEntity<PixStatusResponse> getStatus(
            @PathVariable("transactionId") String transactionId
    ) {
        return getPixStatusUseCase.getStatus(transactionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{transactionId}/communications")
    public ResponseEntity<List<PixCommunicationResponse>> listCommunications(
            @PathVariable("transactionId") String transactionId
    ) {
        return listPixCommunicationsUseCase.listByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
