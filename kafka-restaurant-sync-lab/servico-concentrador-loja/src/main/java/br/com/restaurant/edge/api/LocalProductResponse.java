/* DTO exposto ao PDV; mantém a entidade local isolada da API. */
package br.com.restaurant.edge.api;

import br.com.restaurant.edge.domain.LocalProduct;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LocalProductResponse(
        UUID id,
        String sku,
        String name,
        BigDecimal price,
        boolean active,
        long sourceVersion,
        Instant sourceUpdatedAt
) {
    public static LocalProductResponse from(LocalProduct product) {
        return new LocalProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.isActive(),
                product.getSourceVersion(),
                product.getSourceUpdatedAt()
        );
    }
}
