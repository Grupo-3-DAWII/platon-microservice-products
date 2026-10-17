CREATE TABLE IF NOT EXISTS editorial (
    ideditorial BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS genre (
    idgenre BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE editorial ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE genre ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
CREATE UNIQUE INDEX IF NOT EXISTS ux_editorial_name_lower ON editorial (LOWER(name));
CREATE UNIQUE INDEX IF NOT EXISTS ux_genre_name_lower ON genre (LOWER(name));

CREATE TABLE IF NOT EXISTS product (
    idproduct BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    author VARCHAR(160) NOT NULL,
    ideditorial BIGINT NOT NULL REFERENCES editorial (ideditorial),
    idgenre BIGINT NOT NULL REFERENCES genre (idgenre),
    publication_year INTEGER NOT NULL CHECK (publication_year BETWEEN 1 AND 3000),
    description VARCHAR(2000) NOT NULL,
    image_url VARCHAR(1000),
    purchase_price NUMERIC(12, 2) NOT NULL CHECK (purchase_price >= 0),
    profit_margin NUMERIC(6, 2) NOT NULL CHECK (profit_margin >= 0),
    sale_price NUMERIC(12, 2) NOT NULL CHECK (sale_price >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_product_isbn UNIQUE (isbn)
);

ALTER TABLE product ADD COLUMN IF NOT EXISTS image_url VARCHAR(1000);
ALTER TABLE product ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE product ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS ux_product_name_lower ON product (LOWER(name));
CREATE UNIQUE INDEX IF NOT EXISTS ux_product_isbn_lower ON product (LOWER(isbn));

CREATE TABLE IF NOT EXISTS stock_movement (
    idmovement BIGSERIAL PRIMARY KEY,
    idproduct BIGINT NOT NULL REFERENCES product (idproduct) ON DELETE RESTRICT,
    type VARCHAR(10) NOT NULL CHECK (type IN ('ENTRY', 'EXIT')),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    movement_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resulting_stock INTEGER NOT NULL CHECK (resulting_stock >= 0),
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_stock_movement_product_date
    ON stock_movement (idproduct, movement_date DESC, idmovement DESC);

INSERT INTO editorial (name) VALUES ('Alfaguara'), ('Planeta'), ('Penguin Random House')
ON CONFLICT (name) DO NOTHING;

INSERT INTO genre (name) VALUES ('Novela'), ('Poesia'), ('Ensayo')
ON CONFLICT (name) DO NOTHING;

INSERT INTO product (
    name, isbn, author, ideditorial, idgenre, publication_year, description,
    purchase_price, profit_margin, sale_price, active, stock, created_at, updated_at
)
SELECT 'Don Quijote', '978-612-00-0001-1', 'Miguel de Cervantes',
       (SELECT ideditorial FROM editorial WHERE name = 'Alfaguara'),
       (SELECT idgenre FROM genre WHERE name = 'Novela'),
        1605, 'Las aventuras del ingenioso hidalgo.',
        80.00, 60.00, 128.00, TRUE, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE isbn = '978-612-00-0001-1'
);

INSERT INTO stock_movement (idproduct, type, quantity, movement_date, resulting_stock, notes)
SELECT idproduct, 'ENTRY', stock, CURRENT_TIMESTAMP, stock, 'Stock inicial'
FROM product
WHERE isbn = '978-612-00-0001-1'
  AND stock > 0
  AND NOT EXISTS (
      SELECT 1 FROM stock_movement movement
      WHERE movement.idproduct = product.idproduct
  );
