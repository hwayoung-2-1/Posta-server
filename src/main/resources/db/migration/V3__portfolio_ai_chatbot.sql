CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    content TEXT,
    metadata JSON,
    embedding VECTOR(1536)
);

CREATE INDEX IF NOT EXISTS idx_vector_store_embedding
    ON vector_store USING HNSW (embedding vector_cosine_ops);

CREATE TABLE IF NOT EXISTS chat_sessions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id),
    viewer_user_id UUID NOT NULL REFERENCES users(id),
    last_message_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_sessions_portfolio_id
    ON chat_sessions (portfolio_id);

CREATE INDEX IF NOT EXISTS idx_chat_sessions_viewer_user_id
    ON chat_sessions (viewer_user_id);

CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY,
    chat_session_id UUID NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    answerable BOOLEAN NOT NULL DEFAULT TRUE,
    analyzable BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id
    ON chat_messages (chat_session_id);

CREATE INDEX IF NOT EXISTS idx_chat_messages_analyzable
    ON chat_messages (analyzable);

CREATE TABLE IF NOT EXISTS chat_message_sources (
    id UUID PRIMARY KEY,
    chat_message_id UUID NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE,
    vector_document_id VARCHAR(255) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_id VARCHAR(80),
    page_number INTEGER,
    score DOUBLE PRECISION,
    snippet TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_message_sources_message_id
    ON chat_message_sources (chat_message_id);

CREATE TABLE IF NOT EXISTS question_clusters (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id),
    title VARCHAR(255) NOT NULL,
    summary TEXT NOT NULL,
    category VARCHAR(60) NOT NULL,
    question_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    first_asked_at TIMESTAMP NOT NULL,
    last_asked_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_question_clusters_portfolio_id
    ON question_clusters (portfolio_id);

CREATE INDEX IF NOT EXISTS idx_question_clusters_status
    ON question_clusters (status);

CREATE TABLE IF NOT EXISTS question_cluster_items (
    id UUID PRIMARY KEY,
    question_cluster_id UUID NOT NULL REFERENCES question_clusters(id) ON DELETE CASCADE,
    chat_message_id UUID NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE,
    similarity_score DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_question_cluster_items_cluster_id
    ON question_cluster_items (question_cluster_id);

CREATE INDEX IF NOT EXISTS idx_question_cluster_items_message_id
    ON question_cluster_items (chat_message_id);
