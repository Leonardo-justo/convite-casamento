/* Endpoint operacional para enxergar quantos eventos ainda aguardam publicação. */
package br.com.restaurant.catalog.api;

import br.com.restaurant.catalog.service.OutboxPublicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/outbox")
public class OutboxController {

    private final OutboxPublicationService publicationService;

    public OutboxController(OutboxPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @GetMapping("/status")
    Map<String, Long> status() {
        return Map.of("pendingEvents", publicationService.pendingCount());
    }
}
