-- Fonte central de verdade. version viaja no evento e protege a ordem.
CREATE TABLE catalog_product (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    store_id UUID NOT NULL,
    sku VARCHAR(80) NOT NULL,
    name VARCHAR(180) NOT NULL,
    price NUMERIC(14, 2) NOT NULL,
    active BOOLEAN NOT NULL,
    version BIGINT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_catalog_product_store_sku UNIQUE (tenant_id, store_id, sku)
);

CREATE INDEX idx_catalog_product_store
    ON catalog_product (tenant_id, store_id);

-- Outbox: intenção de publicar gravada junto com o produto.
CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    topic_name VARCHAR(200) NOT NULL,
    event_key VARCHAR(200) NOT NULL,
    payload TEXT NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error VARCHAR(1000)
);

CREATE INDEX idx_outbox_pending
    ON outbox_event (occurred_at)
    WHERE published_at IS NULL;
