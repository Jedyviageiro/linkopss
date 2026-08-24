CREATE TABLE provider_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    bio TEXT,
    profile_image_url VARCHAR(2048),
    city VARCHAR(100) NOT NULL,
    latitude NUMERIC(9, 6),
    longitude NUMERIC(9, 6),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    average_rating NUMERIC(3, 2) NOT NULL DEFAULT 0,
    completed_jobs INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_provider_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_provider_profiles_latitude
        CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_provider_profiles_longitude
        CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    CONSTRAINT chk_provider_profiles_location_pair
        CHECK ((latitude IS NULL) = (longitude IS NULL)),
    CONSTRAINT chk_provider_profiles_rating
        CHECK (average_rating BETWEEN 0 AND 5),
    CONSTRAINT chk_provider_profiles_completed_jobs
        CHECK (completed_jobs >= 0),
    CONSTRAINT chk_provider_profiles_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED'))
);

CREATE INDEX idx_provider_profiles_status
    ON provider_profiles (status);

CREATE INDEX idx_provider_profiles_city
    ON provider_profiles (city);
