
package com.astra.inventory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name("order.created")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic stockReservedTopic() {
        return TopicBuilder.name("stock.reserved")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic stockRejectedTopic() {
        return TopicBuilder.name("stock.reservation-rejected")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic stockConfirmedTopic() {
        return TopicBuilder.name("stock.confirmed")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic stockReleasedTopic() {
        return TopicBuilder.name("stock.released")
            .partitions(3)
            .replicas(1)
            .build();
    }
}
