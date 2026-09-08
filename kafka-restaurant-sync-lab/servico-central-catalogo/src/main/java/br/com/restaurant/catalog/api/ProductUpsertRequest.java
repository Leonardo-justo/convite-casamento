/* DTO de entrada: valida formato antes de chegar ao domínio. */
package br.com.restaurant.catalog.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpsertRequest(
        @NotNull UUID tenantId,
        @NotNull UUID storeId,
        @NotBlank @Size(max = 80) String sku,
        @NotBlank @Size(max = 180) String name,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        boolean active
) {
}
