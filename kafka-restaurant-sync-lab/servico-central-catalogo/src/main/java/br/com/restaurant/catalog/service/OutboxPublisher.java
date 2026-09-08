package br.com.restaurant.catalog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    /*
     * Worker que transforma linhas pendentes do outbox em mensagens Kafka.
     * Ele roda por polling porque o banco é a fonte confiável da pendência.
     * Em produção, considere lock/claim para várias instâncias publicadoras.
     */

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxPublicationService publicationService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Clock clock = Clock.systemUTC();
    private final Duration sendTimeout;

    public OutboxPublisher(
            OutboxPublicationService publicationService,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.outbox.send-timeout}") Duration sendTimeout
    ) {
        this.publicationService = publicationService;
        this.kafkaTemplate = kafkaTemplate;
        this.sendTimeout = sendTimeout;
    }

    @Scheduled(
            fixedDelayString = "${app.outbox.fixed-delay}",
            initialDelayString = "${app.outbox.initial-delay}"
    )
    public void publishPendingEvents() {
        // O lote limitado evita monopolizar a thread e controla a pressão no broker.
        for (OutboxPublicationService.PendingOutboxEvent event : publicationService.loadPending()) {
            try {
                // get() faz esta iteração esperar a confirmação do broker.
                // Sem confirmação, não marcamos o outbox como publicado.
                kafkaTemplate.send(event.topic(), event.key(), event.payload())
                        .get(sendTimeout.toMillis(), TimeUnit.MILLISECONDS);
                publicationService.markPublished(event.id(), clock.instant());
                log.info("Evento {} publicado no tópico {}", event.id(), event.topic());
            } catch (Exception exception) {
                // A linha continua sem published_at e será tentada no próximo ciclo.
                publicationService.registerFailure(event.id(), exception);
                log.warn("Kafka indisponível ou envio rejeitado; evento {} permanece no outbox", event.id(), exception);
            }
        }
    }
}
