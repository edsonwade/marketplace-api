-- V9: Add price column to tb_products
-- Default 9.99 so existing rows are not null
ALTER TABLE tb_products
    ADD COLUMN IF NOT EXISTS price DECIMAL(10, 2) NOT NULL DEFAULT 9.99;

-- Update seed data with realistic prices
UPDATE tb_products SET price = 4.99  WHERE name = 'Product A';
UPDATE tb_products SET price = 2.49  WHERE name = 'Juice - Tomato, 10 Oz';
UPDATE tb_products SET price = 1.99  WHERE name = 'Grapefruit - Pink';
UPDATE tb_products SET price = 8.99  WHERE name = 'Chicken - Wings, Tip Off';
UPDATE tb_products SET price = 3.49  WHERE name = 'Fenngreek Seed';
UPDATE tb_products SET price = 2.29  WHERE name = 'Carrots - Mini Red Organic';
UPDATE tb_products SET price = 5.99  WHERE name = 'Rice - Sushi';
UPDATE tb_products SET price = 12.99 WHERE name = 'Beef - Top Butt Aaa';
UPDATE tb_products SET price = 0.99  WHERE name = 'Foil - 4oz Custard Cup';
UPDATE tb_products SET price = 3.99  WHERE name = 'Brownies - Two Bite, Chocolate';

