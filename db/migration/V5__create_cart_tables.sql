-- Cart tables
CREATE TABLE tb_carts (
    cart_id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES tb_customers(customer_id)
);

CREATE TABLE tb_cart_items (
    cart_item_id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES tb_carts(cart_id),
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES tb_products(product_id)
);

CREATE INDEX idx_cart_customer ON tb_carts(customer_id);
CREATE INDEX idx_cart_status ON tb_carts(status);
CREATE INDEX idx_cart_item_cart ON tb_cart_items(cart_id);
CREATE INDEX idx_cart_item_product ON tb_cart_items(product_id);
