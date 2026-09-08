/* Testes de unidade documentam a garantia produto + outbox na mesma operação. */
package br.com.restaurant.catalog.service;

import br.com.restaurant.catalog.api.ProductUpsertRequest;
import br.com.restaurant.catalog.domain.OutboxEvent;
import br.com.restaurant.catalog.domain.Product;
import br.com.restaurant.catalog.repository.OutboxEventRepository;
import br.com.restaurant.catalog.repository.ProductRepository;
import br.com.restaurant.contracts.ProductChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID STORE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-07-23T12:00:00Z");

    @Test
    void createsProductAndOutboxEventInTheSameUseCase() throws Exception {
        ProductRepository productRepository = mock(ProductRepository.class);
        OutboxEventRepository outboxRepository = mock(OutboxEventRepository.class);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductService service = new ProductService(
                productRepository,
                outboxRepository,
                objectMapper,
                Clock.fixed(NOW, ZoneOffset.UTC),
                "restaurant.catalog.product.v1"
        );

        var response = service.create(new ProductUpsertRequest(
                TENANT_ID,
                STORE_ID,
                "X-BURGER",
                "X-Burger",
                new BigDecimal("24.90"),
                true
        ));

        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(productRepository).saveAndFlush(any(Product.class));
        verify(outboxRepository).save(eventCaptor.capture());

        OutboxEvent outbox = eventCaptor.getValue();
        ProductChangedEvent event = objectMapper.readValue(outbox.getPayload(), ProductChangedEvent.class);
        assertThat(response.id()).isEqualTo(event.data().productId());
        assertThat(outbox.getEventKey()).isEqualTo(STORE_ID + ":" + response.id());
        assertThat(event.eventType()).isEqualTo("ProductUpserted");
        assertThat(event.occurredAt()).isEqualTo(NOW);
        assertThat(event.data().price()).isEqualByComparingTo("24.90");
    }
}
