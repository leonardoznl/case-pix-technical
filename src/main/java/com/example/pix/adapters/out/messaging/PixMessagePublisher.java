package com.example.pix.adapters.out.messaging;

import com.example.pix.application.port.out.usecase.PixMessagePublisherPort;
import com.example.pix.domain.PixMessage;
import com.example.pix.exception.MessagingException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PixMessagePublisher implements PixMessagePublisherPort {
    private static final Logger logger = LoggerFactory.getLogger(PixMessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${pix.mq.exchange}")
    private String exchange;

    @Value("${pix.mq.routing-key}")
    private String routingKey;

    @Override
    public void publish(PixMessage message) {
        try {
            CorrelationData correlation = new CorrelationData();
            Optional.ofNullable(message.transactionId()).ifPresent(correlation::setId);
            rabbitTemplate.convertAndSend(exchange, routingKey, message, correlation);
            logger.info("PIX message published: transactionId={}", message.transactionId());
        } catch (Exception ex) {
            logger.error("Failed to publish PIX message", ex);
            throw new MessagingException("mq-publish-error", ex);
        }
    }
}
