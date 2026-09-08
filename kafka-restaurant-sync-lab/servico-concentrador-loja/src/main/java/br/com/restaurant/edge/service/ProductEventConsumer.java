package br.com.restaurant.edge.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {

    /*
     * Adaptador entre Kafka e o domínio. Não coloque regra de negócio aqui:
     * o método apenas extrai payload/metadata e delega ao processor testável.
     */

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);

    private final ProductEventProcessor processor;

    public ProductEventConsumer(ProductEventProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.product-changed}",
            groupId = "${app.kafka.consumer-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumir(ConsumerRecord<String, String> record) {
        // topic/partition/offset ajudam a investigar lag, duplicidade e DLT.
        EventProcessingResult result = processor.processar(
                record.value(),
                new EventMetadata(record.topic(), record.partition(), record.offset())
        );

        if (result != EventProcessingResult.OTHER_STORE) {
            log.info(
                    "Evento key={} topic={} partition={} offset={} resultado={}",
                    record.key(),
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    result
            );
        }
    }
}
