/* Testes do consumidor: duplicata, loja errada, versão e contrato inválido. */
package br.com.restaurant.edge.service;

import br.com.restaurant.contracts.ProductChangedEvent;
import br.com.restaurant.contracts.ProductPayload;
import br.com.restaurant.edge.domain.LocalProduct;
import br.com.restaurant.edge.domain.ProcessedEvent;
import br.com.restaurant.edge.repository.LocalProductRepository;
import br.com.restaurant.edge.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductEventProcessorTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID STORE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID EVENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant OCCURRED_AT = Instant.parse("2026-07-23T12:00:00Z");
    private static final Instant PROCESSED_AT = Instant.parse("2026-07-23T12:00:01Z");

    private LocalProductRepository productRepository;
    private ProcessedEventRepository processedEventRepository;
    private ObjectMapper objectMapper;
    private ProductEventProcessor processor;

    @BeforeEach
    void setUp() {
        productRepository = mock(LocalProductRepository.class);
        processedEventRepository = mock(ProcessedEventRepository.class);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        processor = new ProductEventProcessor(
                productRepository,
                processedEventRepository,
                objectMapper,
                TENANT_ID,
                STORE_ID,
                Clock.fixed(PROCESSED_AT, ZoneOffset.UTC)
        );
    }

    @Test
    void appliesEventAndRegistersInboxEntryAtomically() throws Exception {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        EventProcessingResult result = processor.process(
                json(event(1, STORE_ID, EVENT_ID, 7)),
                new EventMetadata("restaurant.catalog.product.v1", 2, 19)
        );

        ArgumentCaptor<LocalProduct> productCaptor = ArgumentCaptor.forClass(LocalProduct.class);
        ArgumentCaptor<ProcessedEvent> processedCaptor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(productRepository).save(productCaptor.capture());
        verify(processedEventRepository).save(processedCaptor.capture());

        assertThat(result).isEqualTo(EventProcessingResult.APPLIED);
        assertThat(productCaptor.getValue().getSourceVersion()).isEqualTo(7);
        assertThat(processedCaptor.getValue().getEventId()).isEqualTo(EVENT_ID);
        assertThat(processedCaptor.getValue().getOffset()).isEqualTo(19);
    }

    @Test
    void ignoresAnAlreadyProcessedEvent() throws Exception {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(true);

        EventProcessingResult result = processor.process(
                json(event(1, STORE_ID, EVENT_ID, 7)),
                new EventMetadata("restaurant.catalog.product.v1", 0, 1)
        );

        assertThat(result).isEqualTo(EventProcessingResult.DUPLICATE);
        verify(productRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void acknowledgesButDoesNotStoreEventsForAnotherStore() throws Exception {
        UUID anotherStore = UUID.fromString("99999999-9999-9999-9999-999999999999");

        EventProcessingResult result = processor.process(
                json(event(1, anotherStore, EVENT_ID, 7)),
                new EventMetadata("restaurant.catalog.product.v1", 0, 1)
        );

        assertThat(result).isEqualTo(EventProcessingResult.OTHER_STORE);
        verify(processedEventRepository, never()).existsById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void rejectsUnknownSchemaVersionSoTheErrorHandlerCanRetryAndSendToDlt() throws Exception {
        assertThatThrownBy(() -> processor.process(
                json(event(2, STORE_ID, EVENT_ID, 7)),
                new EventMetadata("restaurant.catalog.product.v1", 0, 1)
        ))
                .isInstanceOf(InvalidEventException.class)
                .hasMessageContaining("schemaVersion");
    }

    private ProductChangedEvent event(
            int schemaVersion,
            UUID storeId,
            UUID eventId,
            long sourceVersion
    ) {
        return new ProductChangedEvent(
                eventId,
                schemaVersion,
                "ProductUpserted",
                OCCURRED_AT,
                "servico-central-catalogo",
                TENANT_ID,
                storeId,
                new ProductPayload(
                        PRODUCT_ID,
                        "X-BURGER",
                        "X-Burger",
                        new BigDecimal("24.90"),
                        true,
                        sourceVersion
                )
        );
    }

    private String json(ProductChangedEvent event) throws Exception {
        return objectMapper.writeValueAsString(event);
    }
}
