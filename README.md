# E-Commerce Backend Microservices

Spring Boot microservices backend with Eureka service discovery, Spring Cloud Gateway, PostgreSQL, Redis, Kafka, Zipkin tracing, shared security/common utilities, and a saga-style order/payment flow.

## Services

| Service | Port | Purpose |
| --- | ---: | --- |
| API Gateway | 8080 | Public entry point and route forwarding |
| Discovery Server | 8761 | Eureka service registry |
| User Service | 8081 | Registration, login, JWT issuing, user profile |
| Product Service | 8082 | Product catalog and inventory |
| Order Service | 8083 | Order creation and payment saga compensation |
| Payment Service | 8084 | Payment processing events and idempotency |
| Notification Service | 8085 | Payment notification logging |
| PostgreSQL | 5432 | Shared database with per-service schemas |
| Kafka | 9092 | Event bus |
| Redis | 6379 | Product cache |
| Zipkin | 9411 | Distributed trace UI |

## Prerequisites

Install these on the target system:

- Java JDK 21 or newer
- Maven 3.9+
- Docker and Docker Compose
- Git

The Dockerfiles currently use `eclipse-temurin:25-jre`, so Docker-based service containers run with Java 25. Local Maven builds target Java 21 from the parent `pom.xml`.

## Clone

```bash
git clone <repository-url>
cd project
```

## Configuration

The application has sensible local defaults, but these environment variables can be overridden on any system:

| Variable | Default |
| --- | --- |
| `JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `JWT_EXPIRATION` | `86400000` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `password` |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `EUREKA_DEFAULT_ZONE` | `http://localhost:8761/eureka/` |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |
| `PAYMENTS_MAX_AUTO_APPROVAL_AMOUNT` | `1000000` |

PostgreSQL schemas are created by `infrastructure/postgres/init-db.sql`:

- `user_schema`
- `product_schema`
- `order_schema`
- `payment_schema`
- `notification_schema`

## Run Everything With Docker Compose

Build the jars first. The service Dockerfiles copy `target/*.jar`, so this step is required before `docker compose up`.

```bash
mvn clean package
docker compose up --build
```

To run in the background:

```bash
docker compose up --build -d
```

To stop the stack:

```bash
docker compose down
```

To remove persisted PostgreSQL data as well:

```bash
docker compose down -v
```

## Run Locally For Development

Start only the infrastructure dependencies:

```bash
docker compose up -d postgres zookeeper kafka redis zipkin
```

Build and test:

```bash
mvn clean test
```

Start services in this order, each in a separate terminal:

```bash
mvn -pl discovery-server spring-boot:run
mvn -pl user-service spring-boot:run
mvn -pl product-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl payment-service spring-boot:run
mvn -pl notification-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

If running services locally while infrastructure is in Docker, the default `localhost` database, Kafka, Redis, and Eureka settings should work.

## Useful URLs

- Gateway: `http://localhost:8080`
- Eureka dashboard: `http://localhost:8761`
- Zipkin dashboard: `http://localhost:9411`
- User service health: `http://localhost:8081/actuator/health`
- Product service health: `http://localhost:8082/actuator/health`
- Order service health: `http://localhost:8083/actuator/health`
- Payment service health: `http://localhost:8084/actuator/health`
- Notification service health: `http://localhost:8085/actuator/health`

Swagger/OpenAPI UI is available on services that include SpringDoc:

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`
- `http://localhost:8085/swagger-ui.html`

## API Quick Start

Register a user:

```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123"}'
```

Login and copy the returned token:

```bash
curl -X POST http://localhost:8080/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

Call a protected endpoint:

```bash
curl http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer <token>"
```

Create an admin-only product with an admin JWT:

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Keyboard","description":"Mechanical keyboard","price":99.99,"inventory":25}'
```

Place an order:

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"orderLineItems":[{"productId":1,"quantity":2}]}'
```

## Tests

Run all tests:

```bash
mvn test
```

Run one module:

```bash
mvn -pl order-service test
```

Compile without tests:

```bash
mvn -DskipTests compile
```

## Troubleshooting

If Docker Compose cannot build a service because no jar exists, run:

```bash
mvn clean package
```

If a service cannot connect to Eureka in Docker, confirm `discovery-server` is healthy and the service has:

```text
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-server:8761/eureka/
```

If Kafka consumers do not receive events immediately after startup, wait for Kafka broker initialization and restart the dependent service:

```bash
docker compose restart order-service payment-service notification-service
```

If database migrations fail after schema changes during development, reset local Docker data:

```bash
docker compose down -v
docker compose up --build
```
