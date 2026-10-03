# microservice-products

Microservicio de catalogo y stock de productos. Las imagenes son responsabilidad del frontend y no se almacenan en la base de datos.

## Modelo inicial

- `idProduct`: identificador generado.
- `name`: nombre unico del producto.
- `stock`: cantidad disponible, siempre mayor o igual a cero.

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
```

Credenciales locales:

- admin: `admin` / `admin123`
- lectura: `student` / `student123`
