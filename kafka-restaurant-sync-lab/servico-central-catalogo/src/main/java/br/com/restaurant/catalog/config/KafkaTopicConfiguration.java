/* Infraestrutura Kafka: o serviço central é dono da criação dos tópicos. */
package br.com.restaurant.catalog.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    NewTopic productEventsTopic(
            @Value("${app.kafka.topics.product-changed}") String topicName,
            @Value("${app.kafka.topic.partitions}") int partitions,
            @Value("${app.kafka.topic.replication-factor}") int replicationFactor
    ) {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, replicationFactor == 1 ? "1" : "2")
                .build();
    }

    @Bean
    NewTopic productEventsDeadLetterTopic(
            @Value("${app.kafka.topics.product-changed}") String topicName,
            @Value("${app.kafka.topic.partitions}") int partitions,
            @Value("${app.kafka.topic.replication-factor}") int replicationFactor
    ) {
        return TopicBuilder.name(topicName + ".dlt")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, replicationFactor == 1 ? "1" : "2")
                .build();
    }
}
