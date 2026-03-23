-- V11: Seed default admin user
-- Email:    admin@marketplace.com
-- Password: Admin@1234
-- This is the ONLY way to get an ADMIN account — self-registration always creates USER.

INSERT INTO tb_users (email, password, role, status)
VALUES (
    'admin@marketplace.com',
    '$2a$10$8QcBoOF2P2hDXeg0VVmgCOVrGw2TpheedCoWvUVFP0WPt0TJCYfKq',
    'ADMIN',
    'ACTIVE'
)
ON CONFLICT (email) DO NOTHING;

-- Also create the matching customer record so /customers/me works for admin
INSERT INTO tb_customers (name, email, address)
VALUES ('Admin', 'admin@marketplace.com', 'HQ')
ON CONFLICT (email) DO NOTHING;
