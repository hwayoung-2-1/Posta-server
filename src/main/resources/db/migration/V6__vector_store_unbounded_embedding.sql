DROP INDEX IF EXISTS idx_vector_store_embedding;

ALTER TABLE vector_store
    ALTER COLUMN embedding TYPE VECTOR USING embedding::VECTOR;
