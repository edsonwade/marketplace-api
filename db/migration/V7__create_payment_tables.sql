-- Payment tables
CREATE TABLE tb_payments (
    payment_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    transaction_id VARCHAR(100),
    payment_gateway_response TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES tb_orders(order_id),
    CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES tb_customers(customer_id)
);

CREATE TABLE tb_payment_methods (
    payment_method_id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    method_type VARCHAR(50) NOT NULL,
    provider VARCHAR(50),
    is_default BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    token VARCHAR(255),
    last_four_digits VARCHAR(4),
    expiry_month VARCHAR(2),
    expiry_year VARCHAR(4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_payment_method_customer FOREIGN KEY (customer_id) REFERENCES tb_customers(customer_id)
);

CREATE INDEX idx_payments_order ON tb_payments(order_id);
CREATE INDEX idx_payments_customer ON tb_payments(customer_id);
CREATE INDEX idx_payments_status ON tb_payments(payment_status);
CREATE INDEX idx_payment_methods_customer ON tb_payment_methods(customer_id);
