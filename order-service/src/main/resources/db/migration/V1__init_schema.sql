CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    order_status VARCHAR(255),
    total_price DECIMAL(19, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_line_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    price DECIMAL(19, 2),
    quantity INTEGER,
    order_id BIGINT,
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id)
);
