CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS skills (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS portfolio_roles (
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (portfolio_id, role_id)
);

CREATE TABLE IF NOT EXISTS portfolio_skills (
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (portfolio_id, skill_id)
);

CREATE TABLE IF NOT EXISTS portfolio_files (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL UNIQUE REFERENCES portfolios(id) ON DELETE CASCADE,
    original_file_name VARCHAR(255) NOT NULL,
    original_file_url VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    page_count INTEGER NOT NULL,
    processing_status VARCHAR(255) NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS portfolio_pages (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    portfolio_file_id UUID NOT NULL REFERENCES portfolio_files(id) ON DELETE CASCADE,
    page_number INTEGER NOT NULL,
    page_image_url VARCHAR(255),
    extracted_text TEXT,
    extraction_status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_portfolio_pages_portfolio_page_number
    ON portfolio_pages (portfolio_id, page_number);

CREATE TABLE IF NOT EXISTS page_owner_notes (
    id UUID PRIMARY KEY,
    portfolio_page_id UUID NOT NULL UNIQUE REFERENCES portfolio_pages(id) ON DELETE CASCADE,
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS portfolio_summaries (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    summary_type VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS suggested_questions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    portfolio_page_id UUID REFERENCES portfolio_pages(id) ON DELETE SET NULL,
    question VARCHAR(255) NOT NULL,
    source VARCHAR(255) NOT NULL,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS saved_portfolios (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, portfolio_id)
);

CREATE TABLE IF NOT EXISTS portfolio_index_jobs (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    job_type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    error_message TEXT,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
