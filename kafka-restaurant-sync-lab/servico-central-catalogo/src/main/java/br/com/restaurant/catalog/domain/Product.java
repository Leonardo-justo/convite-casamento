/* Entidade central. @Version é o relógio monotônico usado pelo sincronizador. */
package br.com.restaurant.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "catalog_product")
public class Product {

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

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
    }

    public Product(
            UUID id,
            UUID tenantId,
            UUID storeId,
            String sku,
            String name,
            BigDecimal price,
            boolean active,
            Instant updatedAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.storeId = storeId;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.active = active;
        this.updatedAt = updatedAt;
    }

    public void update(String sku, String name, BigDecimal price, boolean active, Instant updatedAt) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.active = active;
        this.updatedAt = updatedAt;
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

    public long getVersion() {
        return version;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
