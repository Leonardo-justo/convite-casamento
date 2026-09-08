package br.com.restaurant.contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductChangedEvent(
        // Envelope estável: metadados permitem roteamento, auditoria e evolução.
        UUID eventId,
        int schemaVersion,
        String eventType,
        Instant occurredAt,
        String source,
        UUID tenantId,
        UUID storeId,
        ProductPayload data
) {
}
