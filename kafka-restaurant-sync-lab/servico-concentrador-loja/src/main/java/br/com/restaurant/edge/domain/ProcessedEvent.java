/* Inbox local: eventId é a chave que torna reentrega segura. */
package br.com.restaurant.edge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_event")
public class ProcessedEvent {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @Column(name = "topic_name", nullable = false, length = 200)
    private String topic;

    @Column(name = "topic_partition", nullable = false)
    private int partition;

    @Column(name = "topic_offset", nullable = false)
    private long offset;

    protected ProcessedEvent() {
    }

    public ProcessedEvent(UUID eventId, Instant processedAt, String topic, int partition, long offset) {
        this.eventId = eventId;
        this.processedAt = processedAt;
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }
}
