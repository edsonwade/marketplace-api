CREATE TABLE tb_customers
(
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50),
    email       VARCHAR(100),
    address     VARCHAR(100)
);

CREATE TABLE tb_products
(
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(255),
    quantity   INT,
    version    INT

);

CREATE TABLE tb_orders
(
    order_id    INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    order_date  TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES tb_customers (customer_id)
);

CREATE TABLE tb_order_items
(
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id      INT,
    product_id    INT,
    quantity      INT,
    FOREIGN KEY (order_id) REFERENCES tb_orders (order_id),
    FOREIGN KEY (product_id) REFERENCES tb_products (product_id)
);

ALTER TABLE tb_customers
    ADD CONSTRAINT uc_tb_customers_email UNIQUE (email);