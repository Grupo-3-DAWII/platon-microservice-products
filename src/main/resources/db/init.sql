CREATE TABLE IF NOT EXISTS editorial (
    ideditorial BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS genre (
    idgenre BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS product (
    idproduct BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    author VARCHAR(160) NOT NULL,
    ideditorial BIGINT NOT NULL REFERENCES editorial (ideditorial),
    idgenre BIGINT NOT NULL REFERENCES genre (idgenre),
    publication_year INTEGER NOT NULL CHECK (publication_year BETWEEN 1 AND 3000),
    description VARCHAR(2000) NOT NULL,
    image_data BYTEA,
    image_content_type VARCHAR(100),
    purchase_price NUMERIC(12, 2) NOT NULL CHECK (purchase_price >= 0),
    profit_margin NUMERIC(6, 2) NOT NULL CHECK (profit_margin >= 0),
    sale_price NUMERIC(12, 2) NOT NULL CHECK (sale_price >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    CONSTRAINT uq_product_isbn UNIQUE (isbn)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_product_name_lower ON product (LOWER(name));
CREATE UNIQUE INDEX IF NOT EXISTS ux_product_isbn_lower ON product (LOWER(isbn));

INSERT INTO editorial (name) VALUES ('Alfaguara'), ('Planeta'), ('Penguin Random House')
ON CONFLICT (name) DO NOTHING;

INSERT INTO genre (name) VALUES ('Novela'), ('Poesia'), ('Ensayo')
ON CONFLICT (name) DO NOTHING;

INSERT INTO product (
    name, isbn, author, ideditorial, idgenre, publication_year, description,
    purchase_price, profit_margin, sale_price, active, stock
)
SELECT 'Don Quijote', '978-612-00-0001-1', 'Miguel de Cervantes',
       (SELECT ideditorial FROM editorial WHERE name = 'Alfaguara'),
       (SELECT idgenre FROM genre WHERE name = 'Novela'),
       1605, 'Las aventuras del ingenioso hidalgo.',
       80.00, 60.00, 128.00, TRUE, 5
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE isbn = '978-612-00-0001-1'
);
