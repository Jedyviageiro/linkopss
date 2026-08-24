CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    provider_id UUID NOT NULL,
    service_offering_id UUID NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    address VARCHAR(255) NOT NULL,
    notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_bookings_client
        FOREIGN KEY (client_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_provider
        FOREIGN KEY (provider_id) REFERENCES provider_profiles (id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_service_offering
        FOREIGN KEY (service_offering_id) REFERENCES service_offerings (id) ON DELETE RESTRICT,
    CONSTRAINT chk_bookings_status CHECK (status IN (
        'PENDING', 'ACCEPTED', 'REJECTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
    ))
);

CREATE INDEX idx_bookings_client_created_at ON bookings (client_id, created_at DESC);
CREATE INDEX idx_bookings_provider_created_at ON bookings (provider_id, created_at DESC);
CREATE INDEX idx_bookings_status ON bookings (status);
CREATE INDEX idx_bookings_scheduled_at ON bookings (scheduled_at);
