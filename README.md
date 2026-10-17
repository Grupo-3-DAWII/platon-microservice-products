# microservice-products

Microservicio de catálogo y stock de productos. Las imágenes se representan mediante una URL.

## Modelo inicial

- `idProduct`: identificador generado.
- `name`, `isbn`, `author`, `description`, `publicationYear`.
- `editorialId` y `genreId`, relacionados con tablas de catalogo.
- `image`: URL de la imagen del producto.
- `purchasePrice`, `profitMargin` y `salePrice` calculado.
- `stock` y `active`.
- `createdAt` y `updatedAt`.
- Movimientos de inventario de tipo `ENTRY` o `EXIT`.

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
POST   /api/products/{id}/stock-movements (admin)
GET    /api/products/{id}/stock-movements
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

Los productos se crean con stock cero. El stock solo cambia mediante movimientos de inventario. Una salida que supere el stock disponible es rechazada con HTTP 409. Los movimientos se conservan como historial y un producto con movimientos no debe eliminarse físicamente.

Credenciales locales:

- admin: `admin` / `admin123`
- lectura: `student` / `student123`
