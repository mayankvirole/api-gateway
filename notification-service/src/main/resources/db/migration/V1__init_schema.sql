CREATE TABLE notification_logs (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT,
    message TEXT,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
