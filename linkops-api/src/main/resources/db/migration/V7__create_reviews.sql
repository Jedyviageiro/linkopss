CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL UNIQUE,
    client_id UUID NOT NULL,
    provider_id UUID NOT NULL,
    rating SMALLINT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_reviews_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_client
        FOREIGN KEY (client_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_provider
        FOREIGN KEY (provider_id) REFERENCES provider_profiles (id) ON DELETE RESTRICT,
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_reviews_provider_created_at ON reviews (provider_id, created_at DESC);
CREATE INDEX idx_reviews_client ON reviews (client_id);
