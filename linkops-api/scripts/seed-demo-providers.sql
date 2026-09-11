-- LOCAL TEST DATA ONLY. Run explicitly against the development database.
-- Not a Flyway migration: these fictitious profiles must not be seeded in production.
-- Stable IDs make repeated execution safe. Existing records are not overwritten.
BEGIN;

CREATE TEMP TABLE demo_providers (
  suffix text, first_name text, last_name text, email text, phone text,
  category_slug text, title text, description text, price numeric, price_type text
) ON COMMIT DROP;

INSERT INTO demo_providers VALUES
 ('000000000002', 'Ernesto', 'Macamo', 'arcondicionado.demo@linkops.local', '841234569',
  'ar-condicionado', 'Instalação de ar-condicionado', 'Perfil fictício para testes: instalação e manutenção de ar-condicionado em Maputo.', 1500, 'FIXED'),
 ('000000000003', 'Ana', 'Paula', 'limpeza.demo@linkops.local', '841234570',
  'limpeza-de-casas', 'Limpeza de casas', 'Perfil fictício para testes: limpeza de casas e apartamentos em Maputo.', 600, 'FIXED'),
 ('000000000004', 'Studio', 'Luz', 'fotografia.demo@linkops.local', '841234571',
  'fotografia', 'Fotografia de eventos', 'Perfil fictício para testes: fotografia de eventos em Maputo.', NULL, 'NEGOTIABLE');

-- Generate a fresh bcrypt salt/hash per test account; no usable password is exposed.
CREATE EXTENSION IF NOT EXISTS pgcrypto;
INSERT INTO users (id, first_name, last_name, email, phone, password_hash,
  role, status, created_at, updated_at, token_version, email_verified_at)
SELECT ('20000000-0000-0000-0000-' || suffix)::uuid, first_name, last_name, email, phone,
  crypt(encode(gen_random_bytes(32), 'hex'), gen_salt('bf', 10)),
  'PROVIDER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP
FROM demo_providers
ON CONFLICT (id) DO NOTHING;

INSERT INTO provider_profiles (id, user_id, bio, city, latitude, longitude,
  verified, average_rating, completed_jobs, status, created_at, updated_at,
  verification_status, accepts_cash, accepts_mpesa)
SELECT ('21000000-0000-0000-0000-' || suffix)::uuid,
  ('20000000-0000-0000-0000-' || suffix)::uuid, description, 'Maputo', -25.9655, 32.5832,
  FALSE, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
  'NOT_REQUESTED', TRUE, TRUE
FROM demo_providers
ON CONFLICT (id) DO NOTHING;

INSERT INTO service_offerings (id, provider_id, category_id, title, description,
  price, price_type, active, created_at, updated_at)
SELECT ('22000000-0000-0000-0000-' || d.suffix)::uuid,
  ('21000000-0000-0000-0000-' || d.suffix)::uuid, c.id, d.title, d.description,
  d.price, d.price_type, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM demo_providers d JOIN categories c ON c.slug = d.category_slug
ON CONFLICT (id) DO NOTHING;

DO $$
BEGIN
  IF (SELECT count(*) FROM service_offerings WHERE id IN (
    '22000000-0000-0000-0000-000000000002',
    '22000000-0000-0000-0000-000000000003',
    '22000000-0000-0000-0000-000000000004')) <> 3 THEN
    RAISE EXCEPTION 'Test services missing; ensure category migrations have run.';
  END IF;
END $$;
COMMIT;
