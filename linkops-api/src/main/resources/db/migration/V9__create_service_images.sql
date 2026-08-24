CREATE TABLE service_images (
    id UUID PRIMARY KEY,
    service_offering_id UUID NOT NULL,
    url VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_service_images_offering
        FOREIGN KEY (service_offering_id) REFERENCES service_offerings (id) ON DELETE CASCADE
);

CREATE INDEX idx_service_images_offering_created_at
    ON service_images (service_offering_id, created_at);
