ALTER TABLE provider_profiles
    ADD COLUMN accepts_cash BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN accepts_mpesa BOOLEAN NOT NULL DEFAULT TRUE,
    ADD CONSTRAINT chk_provider_profiles_payment_methods
        CHECK (accepts_cash OR accepts_mpesa);
