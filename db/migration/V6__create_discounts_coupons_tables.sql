-- Discounts and Coupons tables
CREATE TABLE tb_discounts (
    discount_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL, -- PERCENTAGE, FIXED
    discount_value DECIMAL(10,2) NOT NULL,
    min_purchase_amount DECIMAL(10,2) DEFAULT 0,
    max_discount_amount DECIMAL(10,2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0
);

CREATE TABLE tb_coupons (
    coupon_id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_id BIGINT NOT NULL,
    usage_limit INTEGER,
    usage_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_coupon_discount FOREIGN KEY (discount_id) REFERENCES tb_discounts(discount_id)
);

CREATE TABLE tb_coupon_usages (
    usage_id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    order_id BIGINT,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_coupon_usage_coupon FOREIGN KEY (coupon_id) REFERENCES tb_coupons(coupon_id),
    CONSTRAINT fk_coupon_usage_customer FOREIGN KEY (customer_id) REFERENCES tb_customers(customer_id),
    CONSTRAINT fk_coupon_usage_order FOREIGN KEY (order_id) REFERENCES tb_orders(order_id)
);

CREATE INDEX idx_discounts_active ON tb_discounts(is_active, start_date, end_date);
CREATE INDEX idx_coupons_code ON tb_coupons(code);
CREATE INDEX idx_coupons_active ON tb_coupons(is_active);
CREATE INDEX idx_coupon_usages_coupon ON tb_coupon_usages(coupon_id);
CREATE INDEX idx_coupon_usages_customer ON tb_coupon_usages(customer_id);
