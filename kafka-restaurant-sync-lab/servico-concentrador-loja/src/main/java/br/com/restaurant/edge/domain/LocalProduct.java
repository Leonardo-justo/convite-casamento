/* Projeção local do produto central; sourceVersion decide qual estado vence. */
package br.com.restaurant.edge.domain;

import br.com.restaurant.contracts.ProductPayload;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "local_product")
public class LocalProduct {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "source_version", nullable = false)
    private long sourceVersion;

    @Column(name = "source_updated_at", nullable = false)
    private Instant sourceUpdatedAt;

    protected LocalProduct() {
    }

    public LocalProduct(UUID tenantId, UUID storeId, ProductPayload payload, Instant sourceUpdatedAt) {
        this.id = payload.productId();
        this.tenantId = tenantId;
        this.storeId = storeId;
        apply(payload, sourceUpdatedAt);
    }

    public void apply(ProductPayload payload, Instant sourceUpdatedAt) {
        this.sku = payload.sku();
        this.name = payload.name();
        this.price = payload.price();
        this.active = payload.active();
        this.sourceVersion = payload.version();
        this.sourceUpdatedAt = sourceUpdatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }

    public long getSourceVersion() {
        return sourceVersion;
    }

    public Instant getSourceUpdatedAt() {
        return sourceUpdatedAt;
    }
}
