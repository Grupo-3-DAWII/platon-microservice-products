# microservice-products

Microservicio de catalogo y stock de productos. Las imagenes se almacenan en PostgreSQL como datos binarios.

## Modelo inicial

- `idProduct`: identificador generado.
- `name`, `isbn`, `author`, `description`, `publicationYear`.
- `editorialId` y `genreId`, relacionados con tablas de catalogo.
- `image`: binario almacenado en PostgreSQL como `BYTEA`.
- `purchasePrice`, `profitMargin` y `salePrice` calculado.
- `stock` y `active`.

## Ejecutar con Docker

```bash
docker compose up --build
```

API: `http://localhost:8081`

Swagger: `http://localhost:8081/swagger-ui.html`

## Endpoints

```text
GET    /api/products?page=0&size=20&search=quijote
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
GET    /api/editorials
GET    /api/editorials/{id}
POST   /api/editorials              (admin)
PUT    /api/editorials/{id}         (admin)
DELETE /api/editorials/{id}         (admin, borrado lógico)
GET    /api/genres
GET    /api/genres/{id}
POST   /api/genres                  (admin)
PUT    /api/genres/{id}             (admin)
DELETE /api/genres/{id}             (admin, borrado lógico)
```

El precio de venta se calcula como `purchasePrice * (1 + profitMargin / 100)` y se redondea a dos decimales. Para aplicar los cambios del esquema en una base existente, ejecuta `docker compose down -v` antes de levantar nuevamente.

Credenciales locales:

- admin: `admin` / `admin123`
- lectura: `student` / `student123`
