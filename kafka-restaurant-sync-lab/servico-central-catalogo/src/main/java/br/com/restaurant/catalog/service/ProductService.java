package br.com.restaurant.catalog.service;

import br.com.restaurant.catalog.api.ProductResponse;
import br.com.restaurant.catalog.api.ProductUpsertRequest;
import br.com.restaurant.catalog.domain.OutboxEvent;
import br.com.restaurant.catalog.domain.Product;
import br.com.restaurant.catalog.repository.OutboxEventRepository;
import br.com.restaurant.catalog.repository.ProductRepository;
import br.com.restaurant.contracts.EventTypes;
import br.com.restaurant.contracts.ProductChangedEvent;
import br.com.restaurant.contracts.ProductPayload;
import br.com.restaurant.contracts.ProductSnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    /*
     * Orquestra o caso de uso do catálogo. Esta classe é o limite transacional:
     * produto e evento de outbox são gravados juntos no banco central.
     * Se qualquer parte falhar, o Spring faz rollback das duas operações.
     */

    private final ProductRepository productRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final String productTopic;

    @Autowired
    public ProductService(
            ProductRepository productRepository,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.product-changed}") String productTopic
    ) {
        this(productRepository, outboxRepository, objectMapper, Clock.systemUTC(), productTopic);
    }

    ProductService(
            ProductRepository productRepository,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper,
            Clock clock,
            String productTopic
    ) {
        this.productRepository = productRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.productTopic = productTopic;
    }

    @Transactional
    public ProductResponse salvar(ProductUpsertRequest request) {
        // A versão começa em zero no JPA e será enviada no payload do evento.
        Instant now = clock.instant();
        Product product = new Product(
                UUID.randomUUID(),
                request.tenantId(),
                request.storeId(),
                request.sku(),
                request.name(),
                request.price(),
                request.active(),
                now
        );
        productRepository.saveAndFlush(product);
        // Não publicamos diretamente no Kafka; primeiro deixamos uma intenção durável.
        enqueueEvent(product, now);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse atualizar(UUID productId, ProductUpsertRequest request) {
        Product product = productRepository
                .findByIdAndTenantIdAndStoreId(productId, request.tenantId(), request.storeId())
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Instant now = clock.instant();
        product.update(request.sku(), request.name(), request.price(), request.active(), now);
        productRepository.saveAndFlush(product);
        enqueueEvent(product, now);
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listar(UUID tenantId, UUID storeId) {
        return productRepository.findAllByTenantIdAndStoreIdOrderByName(tenantId, storeId)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductSnapshot cargaInicial(UUID tenantId, UUID storeId) {
        List<ProductPayload> products = productRepository
                .findAllByTenantIdAndStoreIdOrderByName(tenantId, storeId)
                .stream()
                .map(this::toPayload)
                .toList();
        return new ProductSnapshot(tenantId, storeId, clock.instant(), products);
    }

    // Aliases antigos mantidos para comparar com exemplos em inglês e preservar compatibilidade.
    @Deprecated public ProductResponse create(ProductUpsertRequest request) { return salvar(request); }
    @Deprecated public ProductResponse update(UUID id, ProductUpsertRequest request) { return atualizar(id, request); }
    @Deprecated public List<ProductResponse> list(UUID tenant, UUID store) { return listar(tenant, store); }
    @Deprecated public ProductSnapshot snapshot(UUID tenant, UUID store) { return cargaInicial(tenant, store); }

    private void enqueueEvent(Product product, Instant occurredAt) {
        // eventId identifica a ocorrência; productId identifica o recurso alterado.
        UUID eventId = UUID.randomUUID();
        String eventType = product.isActive() ? EventTypes.PRODUCT_UPSERTED : EventTypes.PRODUCT_DEACTIVATED;
        ProductChangedEvent event = new ProductChangedEvent(
                eventId,
                1,
                eventType,
                occurredAt,
                "servico-central-catalogo",
                product.getTenantId(),
                product.getStoreId(),
                toPayload(product)
        );

        try {
            outboxRepository.save(new OutboxEvent(
                    eventId,
                    "Product",
                    product.getId(),
                    productTopic,
                    // A chave mantém alterações do mesmo produto na mesma partição.
                    product.getStoreId() + ":" + product.getId(),
                    objectMapper.writeValueAsString(event),
                    occurredAt
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar o evento de produto", exception);
        }
    }

    private ProductPayload toPayload(Product product) {
        return new ProductPayload(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.isActive(),
                product.getVersion()
        );
    }
}
