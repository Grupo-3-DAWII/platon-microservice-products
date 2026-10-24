# platon-microservice-products

Microservicio de catálogo y stock de productos (libros).

## Requisitos

- Java 25 y Maven 3.9+ (para ejecución local) o Docker con Compose.
- Proyecto `DATABASE` levantado (PostgreSQL y RabbitMQ).
- `platon-config-server` en ejecución: si no está disponible, el microservicio no inicia.

## Configuración

Toda la configuración (puerto, conexiones, credenciales) se carga desde `platon-config-server/config-repo/platon-microservice-products.yml`. El `application.yml` local solo define el nombre del servicio y la URL del Config Server (`CONFIG_SERVER_URL`, por defecto `http://localhost:8888`).

Los valores se pueden sobrescribir con variables de entorno (`SERVER_PORT`, `RABBITMQ_*`, etc.).

## Ejecutar

```bash
# Local
mvn spring-boot:run

# Docker
docker compose up --build
```

El servicio queda en <http://localhost:8081>.

## Swagger

Con el servicio en ejecución, abre:

- Interfaz: <http://localhost:8081/swagger-ui.html>
- Especificación OpenAPI (JSON): <http://localhost:8081/v3/api-docs>

La autenticación (JWT) la valida `platon-api-gateway`.

## Eventos de compra (RabbitMQ)

**Validación de compra.** Products consume `purchase.requested` (exchange `platon.purchase.events`, cola `platon.products.purchase.validation`). Si el producto existe, está activo y tiene stock suficiente, descuenta la cantidad y registra un movimiento `EXIT`. Después responde con `purchase.availability.checked` al `replyTo` del mensaje, conservando el `correlationId`.

- Las solicitudes repetidas con el mismo `requestId` no descuentan de nuevo.
- Payments espera la respuesta hasta 10 segundos; si no llega, responde HTTP 504 y la compra no se registra.
- Motivos posibles: `STOCK_DEDUCTED`, `STOCK_ALREADY_RETURNED`, `PRODUCT_NOT_FOUND`, `PRODUCT_INACTIVE`, `INSUFFICIENT_STOCK`, `REQUEST_EXPIRED`, `INVALID_REQUEST`.

**Rechazo de pago.** Products consume `payment.rejected` (cola `platon.products.payment.rejected`) y devuelve el stock con un movimiento `ENTRY`. Los eventos repetidos no suman stock dos veces. Aceptar un pago no envía ningún comando a Products.

Products no consume `purchase.created`; ese evento es para compras ya registradas por Payments.

## Orden recomendado

1. `platon-eureka-server`
2. `platon-config-server`
3. Microservicios
4. `platon-api-gateway`
