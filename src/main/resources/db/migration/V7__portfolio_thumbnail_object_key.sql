ALTER TABLE portfolios
    ADD COLUMN IF NOT EXISTS thumbnail_object_key VARCHAR(512);
