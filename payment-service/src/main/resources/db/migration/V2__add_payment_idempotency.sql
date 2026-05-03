ALTER TABLE payments ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255);
CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_idempotency_key ON payments (idempotency_key);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments (order_id);
