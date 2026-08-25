ALTER TABLE provider_profiles
    ADD COLUMN verification_status VARCHAR(30),
    ADD COLUMN verification_requested_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN verification_reviewed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN verification_reviewed_by UUID,
    ADD COLUMN verification_note VARCHAR(500);

UPDATE provider_profiles
SET verification_status = CASE
    WHEN verified THEN 'VERIFIED'
    ELSE 'NOT_REQUESTED'
END;

ALTER TABLE provider_profiles
    ALTER COLUMN verification_status SET NOT NULL,
    ADD CONSTRAINT fk_provider_verification_reviewer
        FOREIGN KEY (verification_reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_provider_verification_status
        CHECK (verification_status IN ('NOT_REQUESTED', 'PENDING', 'VERIFIED', 'REJECTED')),
    ADD CONSTRAINT chk_provider_verified_consistency
        CHECK (verified = (verification_status = 'VERIFIED'));

CREATE INDEX idx_provider_profiles_verification_status
    ON provider_profiles(verification_status);

ALTER TABLE notifications
    DROP CONSTRAINT chk_notifications_type;

ALTER TABLE notifications
    ADD CONSTRAINT chk_notifications_type CHECK (type IN (
        'BOOKING_CREATED',
        'BOOKING_ACCEPTED',
        'BOOKING_REJECTED',
        'BOOKING_CANCELLED',
        'BOOKING_COMPLETED',
        'REVIEW_RECEIVED',
        'PROVIDER_VERIFIED',
        'PROVIDER_VERIFICATION_REJECTED',
        'PROVIDER_VERIFICATION_REVOKED'
    ));
