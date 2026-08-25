ALTER TABLE users
    ADD CONSTRAINT chk_users_password_hash_bcrypt
        CHECK (password_hash ~ '^\$2[aby]\$[0-9]{2}\$.{53}$');
