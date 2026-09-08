/* Endpoint operacional de saúde e posição conhecida do sincronizador. */
package br.com.restaurant.edge.api;

import br.com.restaurant.edge.domain.ProcessedEvent;
import br.com.restaurant.edge.repository.LocalProductRepository;
import br.com.restaurant.edge.repository.ProcessedEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/interno/sincronizacao")
public class SyncStatusController {

    private final LocalProductRepository productRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final UUID tenantId;
    private final UUID storeId;
    private final String consumerGroup;

    public SyncStatusController(
            LocalProductRepository productRepository,
            ProcessedEventRepository processedEventRepository,
            @Value("${app.identity.tenant-id}") UUID tenantId,
            @Value("${app.identity.store-id}") UUID storeId,
            @Value("${app.kafka.consumer-group}") String consumerGroup
    ) {
        this.productRepository = productRepository;
        this.processedEventRepository = processedEventRepository;
        this.tenantId = tenantId;
        this.storeId = storeId;
        this.consumerGroup = consumerGroup;
    }

    @GetMapping("/status")
    SyncStatus consultarStatus() {
        ProcessedEvent latest = processedEventRepository.findTopByOrderByProcessedAtDesc().orElse(null);
        return new SyncStatus(
                tenantId,
                storeId,
                consumerGroup,
                productRepository.countByTenantIdAndStoreId(tenantId, storeId),
                processedEventRepository.count(),
                latest == null ? null : latest.getProcessedAt(),
                latest == null ? null : latest.getTopic(),
                latest == null ? null : latest.getPartition(),
                latest == null ? null : latest.getOffset()
        );
    }

    record SyncStatus(
            UUID tenantId,
            UUID storeId,
            String consumerGroup,
            long localProducts,
            long processedEvents,
            Instant lastProcessedAt,
            String lastTopic,
            Integer lastPartition,
            Long lastOffset
    ) {
    }
}
