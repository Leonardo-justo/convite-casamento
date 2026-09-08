/* Estado completo usado na primeira carga; não substitui eventos incrementais. */
package br.com.restaurant.contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductSnapshot(
        UUID tenantId,
        UUID storeId,
        Instant generatedAt,
        List<ProductPayload> products
) {
}
