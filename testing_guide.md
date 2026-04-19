# E-Commerce Microservices Testing Guide

This guide describes how to verify the end-to-end functionality of the system, including service discovery, database migrations, and authenticated routing.

## 1. Prerequisites
- Docker and Docker Compose installed.
- Maven installed (if building locally).

## 2. Start the Environment
Run the following command in the root directory:
```bash
docker-compose up -d --build
```
This will start:
- **Postgres**: Initializes schemas via `init-db.sql`.
- **Discovery Server**: Accessible at `http://localhost:8761`.
- **API Gateway**: Accessible at `http://localhost:8080`.
- **Microservices**: All backend services.

## 3. Verify Service Discovery
Open `http://localhost:8761` in your browser. Wait a few minutes (30-60s) until you see all services registered:
- `API-GATEWAY`
- `USER-SERVICE`
- `PRODUCT-SERVICE`
- `ORDER-SERVICE`
- `PAYMENT-SERVICE`
- `NOTIFICATION-SERVICE`

## 4. End-to-End Test Flow

### Step A: Register a New User
```bash
curl -X POST http://localhost:8080/api/users/register \
-H "Content-Type: application/json" \
-d '{
  "email": "test@example.com",
  "password": "password123",
  "name": "Test User"
}'
```

### Step B: Login to get a JWT
```bash
curl -X POST http://localhost:8080/api/users/login \
-H "Content-Type: application/json" \
-d '{
  "email": "test@example.com",
  "password": "password123"
}'
```
> [!IMPORTANT]
> Copy the `token` from the JSON response. You will need it for the next steps.

### Step C: Create a Product (Admin-only simulated)
```bash
curl -X POST http://localhost:8080/api/products \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3NzY1OTU4MzYsImV4cCI6MTc3NjY4MjIzNn0.6ELweSKGxceSYHfL7EltRBShpHGB_g_XKweWwGUFUyk" \
-H "Content-Type: application/json" \
-d '{
  "name": "Smartphone",
  "description": "Latest flagship model",
  "price": 999.99,
  "inventory": 50
}'
```

### Step D: Place an Order
```bash
curl -X POST http://localhost:8080/api/orders \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3NzY1OTU4MzYsImV4cCI6MTc3NjY4MjIzNn0.6ELweSKGxceSYHfL7EltRBShpHGB_g_XKweWwGUFUyk" \
-H "Content-Type: application/json" \
-d '{
  "userId": 1,
  "orderLineItems": [
    {
      "productId": 1,
      "price": 999.99,
      "quantity": 2
    }
  ]
}'
```

## 5. Verifying Header Propagation
You can check the logs of the `product-service` or `order-service` containers to see the `X-User-Email` and `X-User-Role` headers being logged if you've added logging, or monitor the gateway's traffic.

```bash
docker-compose logs -f product-service
```

## 6. Cleanup
```bash
docker-compose down -v
```
The `-v` flag deletes the volumes, which is useful if you want to reset the database and run migrations again.
