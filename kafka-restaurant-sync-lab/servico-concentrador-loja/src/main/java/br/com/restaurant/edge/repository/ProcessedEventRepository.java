/* Persistência da inbox; normalmente consultada por eventId. */
package br.com.restaurant.edge.repository;

import br.com.restaurant.edge.domain.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    Optional<ProcessedEvent> findTopByOrderByProcessedAtDesc();
}
