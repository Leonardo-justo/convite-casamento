package br.com.restaurant.edge.service;

import br.com.restaurant.contracts.EventTypes;
import br.com.restaurant.contracts.ProductChangedEvent;
import br.com.restaurant.edge.domain.LocalProduct;
import br.com.restaurant.edge.domain.ProcessedEvent;
import br.com.restaurant.edge.repository.LocalProductRepository;
import br.com.restaurant.edge.repository.ProcessedEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductEventProcessor {

    /*
     * Regra de aplicação do evento na loja. O @Transactional é essencial:
     * produto local e inbox são confirmados juntos. Se houver erro, ambos
     * sofrem rollback e Kafka entrega a mensagem novamente.
     */

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            EventTypes.PRODUCT_UPSERTED,
            EventTypes.PRODUCT_DEACTIVATED
    );

    private final LocalProductRepository productRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final UUID expectedTenantId;
    private final UUID expectedStoreId;
    private final Clock clock;

    @Autowired
    public ProductEventProcessor(
            LocalProductRepository productRepository,
            ProcessedEventRepository processedEventRepository,
            ObjectMapper objectMapper,
            @Value("${app.identity.tenant-id}") UUID expectedTenantId,
            @Value("${app.identity.store-id}") UUID expectedStoreId
    ) {
        this(
                productRepository,
                processedEventRepository,
                objectMapper,
                expectedTenantId,
                expectedStoreId,
                Clock.systemUTC()
        );
    }

    ProductEventProcessor(
            LocalProductRepository productRepository,
            ProcessedEventRepository processedEventRepository,
            ObjectMapper objectMapper,
            UUID expectedTenantId,
            UUID expectedStoreId,
            Clock clock
    ) {
        this.productRepository = productRepository;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
        this.expectedTenantId = expectedTenantId;
        this.expectedStoreId = expectedStoreId;
        this.clock = clock;
    }

    @Transactional
    public EventProcessingResult processar(String json, EventMetadata metadata) {
        // JSON inválido é erro permanente de contrato e seguirá para a DLT.
        ProductChangedEvent event = deserialize(json);
        validateRouting(event);

        if (!expectedTenantId.equals(event.tenantId()) || !expectedStoreId.equals(event.storeId())) {
            // O tópico pode ser compartilhado; cada edge filtra sua própria loja.
            return EventProcessingResult.OTHER_STORE;
        }

        validateContent(event);

        if (processedEventRepository.existsById(event.eventId())) {
            // At-least-once exige que reentrega seja segura.
            return EventProcessingResult.DUPLICATE;
        }

        EventProcessingResult result;
        LocalProduct product = productRepository.findById(event.data().productId()).orElse(null);
        if (product == null) {
            productRepository.save(new LocalProduct(
                    event.tenantId(),
                    event.storeId(),
                    event.data(),
                    event.occurredAt()
            ));
            result = EventProcessingResult.APPLIED;
        } else if (product.getSourceVersion() >= event.data().version()) {
            // Ordem de chegada não é suficiente; a versão protege contra evento atrasado.
            result = EventProcessingResult.STALE_VERSION;
        } else {
            product.apply(event.data(), event.occurredAt());
            productRepository.save(product);
            result = EventProcessingResult.APPLIED;
        }

        processedEventRepository.save(new ProcessedEvent(
                event.eventId(),
                clock.instant(),
                metadata.topic(),
                metadata.partition(),
                metadata.offset()
        ));
        return result;
    }

    // Alias antigo para manter os testes e exemplos anteriores funcionando.
    @Deprecated public EventProcessingResult process(String json, EventMetadata metadata) {
        return processar(json, metadata);
    }

    private ProductChangedEvent deserialize(String json) {
        try {
            return objectMapper.readValue(json, ProductChangedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new InvalidEventException("JSON de evento inválido", exception);
        }
    }

    private void validateRouting(ProductChangedEvent event) {
        if (event.eventId() == null
                || event.tenantId() == null
                || event.storeId() == null
                || event.occurredAt() == null) {
            throw new InvalidEventException("Evento sem campos obrigatórios de roteamento");
        }
    }

    private void validateContent(ProductChangedEvent event) {
        if (event.data() == null || event.data().productId() == null) {
            throw new InvalidEventException("Evento sem campos obrigatórios");
        }
        if (event.schemaVersion() != 1) {
            throw new InvalidEventException("schemaVersion não suportada: " + event.schemaVersion());
        }
        if (!SUPPORTED_EVENT_TYPES.contains(event.eventType())) {
            throw new InvalidEventException("eventType não suportado: " + event.eventType());
        }
    }
}
