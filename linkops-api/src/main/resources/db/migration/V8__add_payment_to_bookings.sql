ALTER TABLE bookings
    ADD COLUMN payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH',
    ADD COLUMN payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ADD CONSTRAINT chk_bookings_payment_method
        CHECK (payment_method IN ('CASH', 'MPESA')),
    ADD CONSTRAINT chk_bookings_payment_status
        CHECK (payment_status IN ('PENDING', 'PAID', 'NOT_CONFIRMED'));

CREATE INDEX idx_bookings_payment_status ON bookings (payment_status);
