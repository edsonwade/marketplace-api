CREATE TABLE IF NOT EXISTS tb_stocks
(
    stock_id   BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    quantity   INT NOT NULL,
    location   VARCHAR(255),
    version    INT,
    FOREIGN    KEY (product_id) REFERENCES tb_products (product_id)
);
