/* Fachada transacional do outbox; separa persistência do worker Kafka. */
package br.com.restaurant.catalog.service;

import br.com.restaurant.catalog.domain.OutboxEvent;
import br.com.restaurant.catalog.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OutboxPublicationService {

    private final OutboxEventRepository repository;

    public OutboxPublicationService(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PendingOutboxEvent> loadPending() {
        return repository.findTop100ByPublishedAtIsNullOrderByOccurredAtAsc()
                .stream()
                .map(event -> new PendingOutboxEvent(
                        event.getId(),
                        event.getTopicName(),
                        event.getEventKey(),
                        event.getPayload()
                ))
                .toList();
    }

    @Transactional
    public void markPublished(UUID eventId, Instant publishedAt) {
        repository.findById(eventId).ifPresent(event -> event.markPublished(publishedAt));
    }

    @Transactional
    public void registerFailure(UUID eventId, Throwable error) {
        repository.findById(eventId)
                .ifPresent(event -> event.registerFailure(rootMessage(error)));
    }

    @Transactional(readOnly = true)
    public long pendingCount() {
        return repository.countByPublishedAtIsNull();
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getClass().getSimpleName() + ": " + current.getMessage();
    }

    public record PendingOutboxEvent(UUID id, String topic, String key, String payload) {
    }
}
