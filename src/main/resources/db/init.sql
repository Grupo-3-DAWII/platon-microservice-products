CREATE TABLE IF NOT EXISTS product (
    idproduct BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL UNIQUE,
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_product_name_lower ON product (LOWER(name));
