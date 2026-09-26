package com.astra.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ConditionalOnProperty(name = "astra.rabbit.enabled", havingValue = "true")
public class RabbitConfig {
    public static final String EXCHANGE = "astra.events";
    public static final String PRODUCT_QUEUE = "astra.product.events";
    public static final String PRODUCT_ROUTING_KEY = "product.*";

    @Bean
    TopicExchange astraEventExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue productEventQueue() {
        return new Queue(PRODUCT_QUEUE, true);
    }

    @Bean
    Binding productEventBinding(Queue productEventQueue, TopicExchange astraEventExchange) {
        return BindingBuilder.bind(productEventQueue)
                .to(astraEventExchange)
                .with(PRODUCT_ROUTING_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
