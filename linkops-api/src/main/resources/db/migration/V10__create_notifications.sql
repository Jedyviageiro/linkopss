CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_id UUID NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    reference_id UUID,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_notifications_recipient
        FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_notifications_type CHECK (type IN (
        'BOOKING_CREATED',
        'BOOKING_ACCEPTED',
        'BOOKING_REJECTED',
        'BOOKING_CANCELLED',
        'BOOKING_COMPLETED',
        'REVIEW_RECEIVED'
    ))
);

CREATE INDEX idx_notifications_recipient_created_at
    ON notifications (recipient_id, created_at DESC);
CREATE INDEX idx_notifications_recipient_unread
    ON notifications (recipient_id, read_at)
    WHERE read_at IS NULL;
