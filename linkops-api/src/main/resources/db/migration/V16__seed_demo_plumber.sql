-- Single marketplace fixture used by the client home while the catalogue is being built.
-- The account cannot be used interactively; its password is an unknown generated BCrypt value.
INSERT INTO users (
    id, first_name, last_name, email, phone, password_hash, role, status,
    created_at, updated_at, token_version, email_verified_at
) VALUES (
    '20000000-0000-0000-0000-000000000001',
    'Carlos',
    'Mucavele',
    'canalizador.demo@linkops.local',
    '841234568',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'PROVIDER',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0,
    CURRENT_TIMESTAMP
);

INSERT INTO provider_profiles (
    id, user_id, bio, profile_image_url, city, latitude, longitude,
    verified, average_rating, completed_jobs, status, created_at, updated_at,
    verification_status, verification_requested_at, verification_reviewed_at,
    verification_reviewed_by, verification_note, accepts_cash, accepts_mpesa
) VALUES (
    '21000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    'Canalizador residencial com experiência em fugas, tubagens, torneiras e instalações sanitárias.',
    NULL,
    'Maputo',
    -25.9655,
    32.5832,
    TRUE,
    4.90,
    48,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'VERIFIED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL,
    'Prestador de demonstração verificado para testes do catálogo.',
    TRUE,
    TRUE
);

INSERT INTO service_offerings (
    id, provider_id, category_id, title, description, price, price_type,
    active, created_at, updated_at
) VALUES (
    '22000000-0000-0000-0000-000000000001',
    '21000000-0000-0000-0000-000000000001',
    '11000000-0000-0000-0000-000000000002',
    'Canalização residencial',
    'Reparação de fugas, torneiras, tubagens e instalações sanitárias em Maputo.',
    800.00,
    'FIXED',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO service_images (
    id, service_offering_id, url, created_at, updated_at
) VALUES (
    '23000000-0000-0000-0000-000000000001',
    '22000000-0000-0000-0000-000000000001',
    '/images/plumber-service.png',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
