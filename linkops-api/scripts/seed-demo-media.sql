-- Optional LOCAL-ONLY photos for the four demo profiles. No real users are changed.
-- Run after seed-demo-providers.sql. Keeps uploaded photos if a demo was edited.
BEGIN;
INSERT INTO service_images (id, service_offering_id, url, created_at, updated_at)
SELECT ('23000000-0000-0000-0000-' || suffix)::uuid,
       ('22000000-0000-0000-0000-' || suffix)::uuid,
       url, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
 ('000000000002', '/images/ac-service-demo.jpg'),
 ('000000000003', '/images/cleaning-service-demo.jpg'),
 ('000000000004', '/images/photography-service-demo.jpg')
) AS media(suffix,url)
WHERE EXISTS (SELECT 1 FROM service_offerings WHERE id = ('22000000-0000-0000-0000-' || suffix)::uuid)
ON CONFLICT (id) DO NOTHING;

-- Use the optimized copy of the existing plumber image, preserving the original file.
UPDATE service_images SET url = '/images/plumber-service-demo.jpg', updated_at = CURRENT_TIMESTAMP
WHERE id = '23000000-0000-0000-0000-000000000001' AND url = '/images/plumber-service.png';
COMMIT;
