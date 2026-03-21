-- Add total_amount column to tb_carts
ALTER TABLE tb_carts ADD COLUMN IF NOT EXISTS total_amount DECIMAL(10,2) DEFAULT 0;
