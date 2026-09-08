package br.com.restaurant.contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductPayload(
        // version vem da entidade central e é usada para idempotência/ordenação.
        UUID productId,
        String sku,
        String name,
        BigDecimal price,
        boolean active,
        long version
) {
}
