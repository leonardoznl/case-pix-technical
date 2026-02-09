package com.example.pix.adapters.out.messaging;

import com.example.pix.application.port.in.usecase.HandlePixResponseUseCase;
import com.example.pix.domain.PixResponseMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PixResponseListener {
    private static final Logger logger = LoggerFactory.getLogger(PixResponseListener.class);

    private final HandlePixResponseUseCase handlePixResponseUseCase;

    @RabbitListener(queues = "${pix.mq.response-queue}")
    public void handleResponse(PixResponseMessage message) {
        logger.info(
                "PIX response received: transactionId={}, status={}, reason={}"
                , message.transactionId(), message.status(), message.reason()
        );
        handlePixResponseUseCase.handle(message);
    }
}
