package br.com.restaurant.edge.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfiguration {

    /*
     * Aqui definimos o comportamento quando o listener lança uma exceção.
     * O listener só confirma a mensagem depois que o processamento termina.
     * Após as tentativas configuradas, o recoverer publica no tópico DLT.
     */

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.retry.delay-ms}") long retryDelay,
            @Value("${app.kafka.retry.max-retries}") long maxRetries
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (ConsumerRecord<?, ?> record, Exception exception) ->
                        // Convenção padrão: mesmo nome + .dlt e mesma partição.
                        new TopicPartition(record.topic() + ".dlt", record.partition())
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                // maxRetries é o número de novas tentativas após a primeira entrega.
                new FixedBackOff(retryDelay, maxRetries)
        );
        errorHandler.setAckAfterHandle(true);

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, consumerFactory);
        // Substitui o handler padrão do Spring por retry + DLT.
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
