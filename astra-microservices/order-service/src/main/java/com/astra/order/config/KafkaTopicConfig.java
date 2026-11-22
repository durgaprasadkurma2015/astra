package com.astra.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentSuccessDlt() {
        return TopicBuilder.name("payment.success.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedDlt() {
        return TopicBuilder.name("payment.failed.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockReservedDlt() {
        return TopicBuilder.name("stock.reserved.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockRejectedDlt() {
        return TopicBuilder.name("stock.reservation-rejected.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}