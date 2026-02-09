package com.example.pix.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public DirectExchange pixExchange(@Value("${pix.mq.exchange}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange(@Value("${pix.mq.dead-letter-exchange}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue pixRequestQueue(
            @Value("${pix.mq.request-queue}") String queue,
            @Value("${pix.mq.dead-letter-exchange}") String dlx,
            @Value("${pix.mq.request-dlq}") String dlq
    ) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlx);
        args.put("x-dead-letter-routing-key", dlq);
        return new Queue(queue, true, false, false, args);
    }

    @Bean
    public Queue pixResponseQueue(
            @Value("${pix.mq.response-queue}") String queue,
            @Value("${pix.mq.dead-letter-exchange}") String dlx,
            @Value("${pix.mq.response-dlq}") String dlq
    ) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlx);
        args.put("x-dead-letter-routing-key", dlq);
        return new Queue(queue, true, false, false, args);
    }

    @Bean
    public Queue pixResponseDlq(@Value("${pix.mq.response-dlq}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    public Queue pixRequestDlq(@Value("${pix.mq.request-dlq}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    public Binding pixRequestBinding(
            @Qualifier("pixExchange") DirectExchange pixExchange,
            @Qualifier("pixRequestQueue") Queue pixRequestQueue,
            @Value("${pix.mq.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(pixRequestQueue).to(pixExchange).with(routingKey);
    }

    @Bean
    public Binding pixResponseDlqBinding(
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange,
            @Qualifier("pixResponseDlq") Queue pixResponseDlq,
            @Value("${pix.mq.response-dlq}") String routingKey
    ) {
        return BindingBuilder.bind(pixResponseDlq).to(deadLetterExchange).with(routingKey);
    }

    @Bean
    public Binding pixRequestDlqBinding(
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange,
            @Qualifier("pixRequestDlq") Queue pixRequestDlq,
            @Value("${pix.mq.request-dlq}") String routingKey
    ) {
        return BindingBuilder.bind(pixRequestDlq).to(deadLetterExchange).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
