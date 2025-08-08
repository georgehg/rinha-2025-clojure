CREATE TABLE payments_processing (
    processing_id BIGINT GENERATED ALWAYS AS IDENTITY,
    correlation_id VARCHAR(64) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    requested_at TIMESTAMP WITH time zone default (now() at time zone('utc')) NOT NULL,
    status VARCHAR(16) NOT NULL,
    processor VARCHAR(8)
);
--;;

CREATE INDEX idx_hash_payments_processing_processing_id on PAYMENTS_PROCESSING using hash (processing_id);
--;;

CREATE INDEX idx_btree_payments_processing_requested_at ON PAYMENTS_PROCESSING USING BTREE (requested_at);

