ALTER TABLE portfolio_context
    ALTER COLUMN embedding TYPE TEXT USING embedding::TEXT;
