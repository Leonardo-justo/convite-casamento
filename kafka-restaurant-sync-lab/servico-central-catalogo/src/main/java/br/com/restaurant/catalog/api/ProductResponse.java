/* DTO de saída: não expõe diretamente a entidade JPA pela API. */
package br.com.restaurant.catalog.api;

import br.com.restaurant.catalog.domain.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID tenantId,
        UUID storeId,
        String sku,
        String name,
        BigDecimal price,
        boolean active,
        long version,
        Instant updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getTenantId(),
                product.getStoreId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.isActive(),
                product.getVersion(),
                product.getUpdatedAt()
        );
    }
}
