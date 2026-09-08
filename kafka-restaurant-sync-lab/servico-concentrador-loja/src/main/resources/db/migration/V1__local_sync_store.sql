-- Cópia local consultada pelo PDV offline.
CREATE TABLE local_product (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    store_id UUID NOT NULL,
    sku VARCHAR(80) NOT NULL,
    name VARCHAR(180) NOT NULL,
    price NUMERIC(14, 2) NOT NULL,
    active BOOLEAN NOT NULL,
    source_version BIGINT NOT NULL,
    source_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_local_product_store_sku UNIQUE (tenant_id, store_id, sku)
);

CREATE INDEX idx_local_product_store
    ON local_product (tenant_id, store_id, active);

-- Inbox/idempotência: eventId já aplicado e metadados para auditoria.
CREATE TABLE processed_event (
    event_id UUID PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    topic_name VARCHAR(200) NOT NULL,
    topic_partition INTEGER NOT NULL,
    topic_offset BIGINT NOT NULL
);

CREATE INDEX idx_processed_event_time
    ON processed_event (processed_at);
