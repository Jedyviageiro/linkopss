CREATE TABLE service_offerings (
    id UUID PRIMARY KEY,
    provider_id UUID NOT NULL,
    category_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2),
    price_type VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_service_offerings_provider
        FOREIGN KEY (provider_id) REFERENCES provider_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_service_offerings_category
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT chk_service_offerings_price_type
        CHECK (price_type IN ('FIXED', 'NEGOTIABLE')),
    CONSTRAINT chk_service_offerings_price
        CHECK ((price_type = 'FIXED' AND price IS NOT NULL AND price > 0)
            OR (price_type = 'NEGOTIABLE' AND (price IS NULL OR price > 0)))
);

CREATE INDEX idx_service_offerings_provider ON service_offerings (provider_id);
CREATE INDEX idx_service_offerings_category ON service_offerings (category_id);
CREATE INDEX idx_service_offerings_active ON service_offerings (active);
