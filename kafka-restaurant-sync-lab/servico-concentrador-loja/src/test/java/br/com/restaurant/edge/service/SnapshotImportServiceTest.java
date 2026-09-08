/* Teste que prova que snapshot antigo não sobrescreve evento mais novo. */
package br.com.restaurant.edge.service;

import br.com.restaurant.contracts.ProductPayload;
import br.com.restaurant.contracts.ProductSnapshot;
import br.com.restaurant.edge.domain.LocalProduct;
import br.com.restaurant.edge.repository.LocalProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SnapshotImportServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID STORE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant SNAPSHOT_AT = Instant.parse("2026-07-23T12:00:00Z");

    @Test
    void doesNotOverwriteARealtimeEventWithAnOlderSnapshot() {
        LocalProductRepository repository = mock(LocalProductRepository.class);
        ProductPayload localVersionFive = product(5, "Preço novo", "29.90");
        LocalProduct local = new LocalProduct(TENANT_ID, STORE_ID, localVersionFive, SNAPSHOT_AT.plusSeconds(1));
        when(repository.findById(PRODUCT_ID)).thenReturn(Optional.of(local));

        ProductSnapshot olderSnapshot = new ProductSnapshot(
                TENANT_ID,
                STORE_ID,
                SNAPSHOT_AT,
                List.of(product(4, "Preço antigo", "24.90"))
        );

        SnapshotImportResult result = new SnapshotImportService(repository).apply(olderSnapshot);

        assertThat(result.ignoredBecauseLocalVersionWasEqualOrNewer()).isEqualTo(1);
        assertThat(local.getName()).isEqualTo("Preço novo");
        verify(repository, never()).save(any());
    }

    private ProductPayload product(long version, String name, String price) {
        return new ProductPayload(
                PRODUCT_ID,
                "X-BURGER",
                name,
                new BigDecimal(price),
                true,
                version
        );
    }
}
