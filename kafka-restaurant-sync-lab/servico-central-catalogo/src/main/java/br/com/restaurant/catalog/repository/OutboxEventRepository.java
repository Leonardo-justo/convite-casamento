/* Consultas Spring Data para selecionar o lote pendente do outbox. */
package br.com.restaurant.catalog.repository;

import br.com.restaurant.catalog.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();

    long countByPublishedAtIsNull();
}
