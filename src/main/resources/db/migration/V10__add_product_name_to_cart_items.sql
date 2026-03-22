-- V10: Add product_name column to tb_cart_items
-- The CartItem entity maps product_name but it was missing from the original schema
ALTER TABLE tb_cart_items
    ADD COLUMN IF NOT EXISTS product_name VARCHAR(255);
