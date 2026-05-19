ALTER TABLE portfolios
    ADD COLUMN IF NOT EXISTS page_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS pdf_object_key VARCHAR(512),
    ADD COLUMN IF NOT EXISTS pdf_original_filename VARCHAR(255),
    ADD COLUMN IF NOT EXISTS pdf_content_type VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pdf_size BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS like_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS comment_count INTEGER NOT NULL DEFAULT 0;

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS portfolio_context (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id),
    page_number INTEGER NOT NULL,
    content TEXT NOT NULL,
    embedding TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

ALTER TABLE portfolio_context
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_portfolio_context_portfolio_page'
    ) THEN
        ALTER TABLE portfolio_context
            ADD CONSTRAINT uk_portfolio_context_portfolio_page UNIQUE (portfolio_id, page_number);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_portfolio_context_portfolio_id
    ON portfolio_context (portfolio_id);

CREATE INDEX IF NOT EXISTS idx_portfolio_context_portfolio_page
    ON portfolio_context (portfolio_id, page_number);
