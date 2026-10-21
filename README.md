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

## Configuracion centralizada

Iniciar primero `platon-config-server` (puerto `8888`). Products carga sus ajustes desde
`platon-config-server/config-repo/platon-microservice-products.yml`.
El archivo local `application.yml` solo contiene el nombre del servicio y la conexion
`configserver:${CONFIG_SERVER_URL:http://localhost:8888}`. Si el Config Server no esta disponible,
el microservicio no inicia.

Definir `DB_PASSWORD` en el entorno de Products; se conservan las variables `DB_URL`,
`DB_USERNAME`, `RABBITMQ_*`, `SERVER_PORT`, `ADMIN_*` y `USER_*`.
El archivo `.local-db.yml` ya no se carga. Las pruebas deshabilitan el cliente Config Server.
Docker usa `http://host.docker.internal:8888` y permite cambiarlo con `CONFIG_SERVER_URL`.

## Ejecutar con Docker

```bash
docker compose up --build
```

API: `http://localhost:8081`

Swagger: `http://localhost:8081/swagger-ui.html`

## Endpoints

Los mismos endpoints estan disponibles con el prefijo `/api/v1` sin autenticacion,
incluidas las operaciones de escritura. Por ejemplo: `GET /api/v1/products`.
Las rutas originales bajo `/api` conservan Basic Auth y sus permisos por rol.
En Postman selecciona **No Auth** para las rutas `/api/v1`.

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

## Validacion de compras por RabbitMQ

Products consume `purchase.requested` del exchange durable `platon.purchase.events` (tipo `topic`), mediante la cola durable `platon.products.purchase.validation`.

El evento JSON contiene `requestId` (UUID), `eventType`, `idProducts`, `numberSold` y `expiresAt` (UTC). El servicio valida la estructura, la vigencia, que el producto exista, este activo y tenga stock suficiente para la cantidad solicitada. Al aprobar, Products descuenta la cantidad, registra un movimiento EXIT y guarda el requestId en products.purchase_stock_deduction en una misma transaccion. Las solicitudes repetidas con el mismo identificador no descuentan de nuevo. No hay reservas ni vencimientos de inventario.

Products devuelve otro evento JSON con `eventType=purchase.availability.checked`, `requestId`, `idProducts`, `numberSold`, `available` y `reason`. La respuesta se publica en el exchange/routing key de `replyTo`, conservando el `correlationId` AMQP. Payments configura una cola de respuestas exclusiva por instancia con routing key `purchase.availability.checked.<uuid>`, y espera hasta 10 segundos antes de responder HTTP 504. Una respuesta tardia no registra la compra. Solo una respuesta positiva permite que Payments guarde la compra y devuelva HTTP 200.

Motivos: `STOCK_DEDUCTED`, `STOCK_ALREADY_RETURNED`, `PRODUCT_NOT_FOUND`, `PRODUCT_INACTIVE`, `INSUFFICIENT_STOCK`, `REQUEST_EXPIRED`, `INVALID_REQUEST`.

Conexion local: `localhost:5672`, `guest` / `guest`. Se puede cambiar con `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`. Este servicio no consume `purchase.created`: ese evento corresponde a una compra ya registrada por Payments.
### Rechazo del pago y devolucion

Products consume `payment.rejected` del mismo exchange `platon.purchase.events`, usando la cola durable `platon.products.payment.rejected`. El JSON incluye `eventId`, `eventType`, `stockDeductionId`, `idProducts` y `numberSold`; puede contener mas campos de la compra.

`StockReturnService` bloquea el descuento original, comprueba producto y cantidad, incrementa stock, registra un movimiento ENTRY y marca el descuento RETURNED en una sola transaccion. Los eventos repetidos no vuelven a sumar stock. Si llega una compensacion antes del descuento, se registra RETURNED sin incrementar inventario para impedir descuentos tardios. Los errores transitorios de procesamiento se reencolan; los mensajes invalidos se descartan.

Aceptar el pago no envia un comando de stock a Products: Payments solo marca PAGADO y asigna el producto al usuario. Rechazar marca CANCELADO en Payments y devuelve stock de manera asincrona mediante RabbitMQ. La creacion del esquema pertenece al proyecto DATABASE. El archivo init/05-products-stock-deduction.sql crea la tabla auxiliar de descuentos. Spring SQL Init esta desactivado (mode: never); el microservicio no ejecuta DDL al arrancar.
## Scripts centralizados en DATABASE

Los scripts de creacion estan en el proyecto hermano `../DATABASE`. El PostgreSQL compartido usa `DATABASE/init/01-create-database-schema.sql` y `DATABASE/init/05-products-stock-deduction.sql`. Para una base existente se aplica solo la migracion nueva, conservando el volumen y los datos.

El Docker Compose independiente de este proyecto monta `../DATABASE/products-standalone/01-create-products-schema.sql` (el bootstrap anterior, conservado integramente) y `../DATABASE/init/05-products-stock-deduction.sql`. Mantener ambos proyectos como carpetas hermanas. No se incluyen scripts DDL en los recursos del microservicio.
