# 🛒 Marketplace API

> **Full-stack enterprise marketplace** — Spring Boot REST API + React SPA, with PostgreSQL, Redis, Kafka, MongoDB and MailHog, all containerised with Docker Compose.

---

## 📋 Table of Contents

- [🏗️ Architecture](#-architecture)
- [⚙️ Tech Stack](#-tech-stack)
- [🚀 Quick Start](#-quick-start)
- [🌐 Service URLs](#-service-urls)
- [🔐 Authentication](#-authentication)
- [👤 Customers](#-customers)
- [📦 Products](#-products)
- [🛒 Cart](#-cart)
- [📋 Orders](#-orders)
- [💳 Payments](#-payments)
- [🏷️ Discounts & Coupons](#-discounts--coupons)
- [📦 Stock Management](#-stock-management)
- [🔔 Notifications & Kafka](#-notifications--kafka)
- [🔄 Complete Flow](#-complete-flow)
- [🔒 Role-Based Access Control](#-role-based-access-control)
- [🗄️ Database Schema](#-database-schema)
- [📊 Kafka Topics](#-kafka-topics)
- [🐳 Docker Infrastructure](#-docker-infrastructure)
- [⚠️ Error Reference](#-error-reference)
- [🧪 Testing](#-testing)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT BROWSER                           │
│                    React 19 + Vite + Tailwind                   │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP (port 3000)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                        NGINX (port 80)                          │
│                    Static files + Reverse proxy                 │
│                    /api/* → backend:8080                        │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP (port 8080/8081)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│               SPRING BOOT (marketplace-service)                 │
│                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────────┐  │
│  │  REST    │  │  JWT     │  │  Kafka   │  │   Flyway      │  │
│  │Controllers│  │Security  │  │Producer/ │  │   Migrations  │  │
│  └────┬─────┘  └──────────┘  │Consumer  │  └───────────────┘  │
│       │                      └────┬─────┘                      │
│  ┌────▼────────────────────────────────────────────────────┐   │
│  │                    SERVICE LAYER                        │   │
│  │  CartService · PaymentService · OrderService ·         │   │
│  │  ProductService · CustomerService · StockService ·     │   │
│  │  NotificationService · AuthenticationService           │   │
│  └─────────────────────────────────────────────────────────┘   │
└────┬──────────────┬──────────────┬──────────────┬──────────────┘
     │              │              │              │
     ▼              ▼              ▼              ▼
┌─────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐
│Postgres │  │  Redis   │  │ MongoDB  │  │    Kafka     │
│  :5432  │  │  :6379   │  │  :27017  │  │    :9092     │
│ Tables  │  │  Cache   │  │ Notifs   │  │  4 Topics    │
│ Flyway  │  │  Tokens  │  │  Audit   │  │  + Zookeeper │
└─────────┘  └──────────┘  └──────────┘  └──────────────┘
```

---

## ⚙️ Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Spring Boot | 3.2.5 | Framework |
| Spring Security | 6.x | JWT Auth + RBAC |
| Spring Data JPA | 3.x | ORM / Postgres |
| Spring Kafka | 3.x | Event streaming |
| Spring Data MongoDB | 3.x | Notification storage |
| Spring Data Redis | 3.x | Cache + Rate limiting |
| Flyway | 8.x | DB migrations |
| Lombok | latest | Boilerplate reduction |
| SpringDoc OpenAPI | 2.x | Swagger UI |
| Jackson | 2.x | JSON serialisation |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| React | 19 | UI framework |
| TypeScript | 5.x | Type safety |
| Vite | 6.x | Build tool |
| Tailwind CSS | 4.x | Styling |
| TanStack Query | 5.x | Server state |
| Zustand | 4.x | Client state |
| React Router | 7.x | Navigation |
| Recharts | 2.x | Dashboard charts |
| Lucide React | 0.38x | Icons |

### Infrastructure
| Service | Image | Purpose |
|---|---|---|
| PostgreSQL | 15-alpine | Primary database |
| Redis | 7.2-alpine | Cache + sessions |
| Kafka | confluentinc 7.6.0 | Event bus |
| Zookeeper | confluentinc 7.6.0 | Kafka coordination |
| MongoDB | 7.0-jammy | Notification log |
| MailHog | latest | Email testing (SMTP) |
| Kafka UI | provectuslabs | Kafka topic viewer |
| Prometheus | latest | Metrics scraping |
| Grafana | latest | Metrics dashboard |

---

## 🚀 Quick Start

### Prerequisites
- Docker Desktop
- Docker Compose v2+

### First run (fresh install)
```bash
# Clone the project
git clone <repo-url>
cd marketplace-api

# Start everything
docker compose up --build
```

### Subsequent runs (code changes)
```bash
docker compose up --build
```

### Dependency changes only (pom.xml / package.json)
```bash
docker compose build --no-cache
docker compose up
```

### Clean restart (keeps all data)
```bash
docker compose down
docker compose up
```

### ⚠️ Full reset (DESTROYS ALL DATA — use with caution)
```bash
docker compose down -v
docker volume rm marketplace-api_kafka-data marketplace-api_zookeeper-data marketplace-api_zookeeper-logs
docker compose up
```

> **Note:** Never use `docker compose down -v` for routine maintenance. This destroys all volumes including user accounts, orders and payment history.

---

## 🌐 Service URLs

| Service | URL | Credentials |
|---|---|---|
| 🖥️ Frontend (React) | http://localhost:3000 | — |
| 🔧 Backend API | http://localhost:8081 | — |
| 📖 Swagger UI | http://localhost:8081/swagger-ui.html | — |
| 📊 Kafka UI | http://localhost:8090 | — |
| 📧 MailHog (email inbox) | http://localhost:8025 | — |
| 🗄️ PostgreSQL | localhost:5433 | user: `postgres` / pass: `postgres` |
| 🔴 Redis | localhost:6379 | pass: `redis_pass_2024` |
| 🍃 MongoDB | localhost:27017 | user: `mongo` / pass: `mongo` |

---

## 🔐 Authentication

Base path: `/api/v1/auth`

All endpoints except `/auth/**` require a JWT Bearer token.

### Endpoints

#### `POST /api/v1/auth/register`
Register a new user account. Always creates a `USER` role — admin accounts are only created via database seed.

**Request:**
```json
{
  "email": "joao@empresa.com",
  "password": "Senha@123"
}
```
**Password requirements:** minimum 8 characters, at least one uppercase, one lowercase, one digit, one special character.

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

#### `POST /api/v1/auth/login`
Authenticate and receive tokens.

**Request:**
```json
{
  "email": "joao@empresa.com",
  "password": "Senha@123"
}
```

**Response:** same structure as register.

| Token | Lifetime | Storage |
|---|---|---|
| `access_token` | 15 minutes | In-memory (Zustand) |
| `refresh_token` | 7 days | HttpOnly cookie |

The frontend automatically refreshes the access token when it expires.

---

#### `POST /api/v1/auth/refresh-token`
Exchange a valid refresh token for a new access token. Called automatically by the frontend.

---

#### `POST /api/v1/auth/change-password`
Change password for the currently authenticated user.

**Request:**
```json
{
  "currentPassword": "OldPass@123",
  "newPassword": "NewPass@456"
}
```

**Requires:** `Authorization: Bearer {access_token}`

---

### 🔑 Default Admin Account

| Field | Value |
|---|---|
| Email | `admin@marketplace.com` |
| Password | `Admin@1234` |
| Role | `ADMIN` |

> Created via Flyway migration V11. This is the only way to get an ADMIN account — self-registration always creates `USER`.

---

### Using the API with Swagger
1. Open http://localhost:8081/swagger-ui.html
2. Call `POST /api/v1/auth/login` and copy the `access_token`
3. Click **Authorize** (padlock icon, top right)
4. Enter: `Bearer {your_access_token}`
5. All subsequent calls will include the token

### Using with Postman / curl
```bash
# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@marketplace.com","password":"Admin@1234"}'

# Use token
curl http://localhost:8081/api/v1/products \
  -H "Authorization: Bearer eyJhbGci..."
```

---

## 👤 Customers

Base path: `/api/v1/customers`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/me` | 🔓 Any user | Get the current user's customer record |
| `GET` | `/` | 🔒 ADMIN/MANAGER | List all customers |
| `GET` | `/{id}` | 🔒 ADMIN/MANAGER | Get customer by ID |
| `POST` | `/` | 🔒 ADMIN/MANAGER | Create a customer |
| `PUT` | `/{id}` | 🔒 ADMIN/MANAGER | Update a customer |
| `DELETE` | `/{id}` | 🔒 ADMIN/MANAGER | Delete a customer |

### `GET /api/v1/customers/me`
Used by the frontend immediately after login to resolve the `customerId` from the JWT email. Auto-creates a customer record if one doesn't exist for this email.

**Response:**
```json
{
  "customerId": 7,
  "name": "joao",
  "email": "joao@empresa.com",
  "address": ""
}
```

### `POST /api/v1/customers`
```json
{
  "name": "João Silva",
  "email": "joao@empresa.com",
  "address": "Rua Principal 123, Lisboa"
}
```

---

## 📦 Products

Base path: `/api/v1/products`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/` | 🔓 Any user | List all products |
| `GET` | `/{id}` | 🔓 Any user | Get product by ID |
| `POST` | `/` | 🔒 ADMIN/MANAGER | Create a product |
| `PUT` | `/{id}` | 🔒 ADMIN/MANAGER | Update a product (requires `If-Match` header) |
| `DELETE` | `/{id}` | 🔒 ADMIN/MANAGER | Delete a product |

### `GET /api/v1/products`
**Response:**
```json
[
  {
    "productId": 1,
    "name": "Product A",
    "quantity": 10,
    "price": 4.99,
    "version": 1
  }
]
```

### `POST /api/v1/products`
```json
{
  "name": "Wireless Headphones",
  "quantity": 50,
  "price": 29.99
}
```

### `PUT /api/v1/products/{id}`
Uses **optimistic locking** to prevent conflicting concurrent edits.

**Required header:** `If-Match: {current_version}`

```json
{
  "name": "Wireless Headphones Pro",
  "quantity": 45,
  "price": 34.99
}
```

> Returns `409 Conflict` if `If-Match` version doesn't match the stored version. Fetch the product again to get the latest version before retrying.

---

## 🛒 Cart

Base path: `/api/v1/carts`

The cart is **persisted in PostgreSQL** — it survives page refreshes, browser restarts and re-logins.

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/` | 🔓 Any | List all carts |
| `GET` | `/{id}` | 🔓 Any | Get cart by ID |
| `GET` | `/customer/{customerId}` | 🔓 Any | Get active cart for customer |
| `POST` | `/?customerId={id}` | 🔓 Any | Create a cart |
| `POST` | `/items?customerId={id}` | 🔓 Any | Add item to cart |
| `PUT` | `/items/{productId}?customerId={id}&quantity={n}` | 🔓 Any | Update item quantity |
| `DELETE` | `/items/{productId}?customerId={id}` | 🔓 Any | Remove item from cart |
| `DELETE` | `/clear?customerId={id}` | 🔓 Any | Clear entire cart |
| `POST` | `/checkout-order?customerId={id}` | 🔓 Any | Checkout → creates Order |

### `POST /api/v1/carts/items?customerId=3`
```json
{
  "productId": 5,
  "quantity": 2
}
```

**Validations:**
- Product must exist
- Product cannot be out of stock
- Cannot add more than available stock (including quantity already in cart)

**Response:**
```json
{
  "id": 1,
  "customerId": 3,
  "status": "ACTIVE",
  "items": [
    {
      "id": 1,
      "productId": 5,
      "productName": "Wireless Headphones",
      "quantity": 2,
      "price": 29.99,
      "subtotal": 59.98
    }
  ],
  "totalAmount": 59.98
}
```

### `PUT /api/v1/carts/items/{productId}?customerId=3&quantity=0`
> Setting `quantity=0` automatically removes the item from the cart.

### `POST /api/v1/carts/checkout-order?customerId=3`
Atomically: marks cart as `CHECKED_OUT`, creates an `Order` with all cart items, publishes an order notification to Kafka.

**Response:**
```json
{
  "orderId": 12,
  "localDateTime": "2026-03-23T14:30:00",
  "customer": { "customerId": 3, "name": "João Silva" },
  "orderItems": [
    { "product": { "name": "Wireless Headphones" }, "quantity": 2 }
  ],
  "totalAmount": 59.98
}
```

---

## 📋 Orders

Base path: `/api/v1/orders`

Orders are created automatically via cart checkout. Manual creation is available for ADMIN only.

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/` | 🔒 ADMIN/MANAGER | List all orders |
| `GET` | `/{id}` | 🔓 Any | Get order by ID |
| `GET` | `/customer/{customerId}` | 🔓 Any | Get all orders for a customer |
| `GET` | `/customer/{customerId}/unpaid` | 🔓 Any | Get unpaid orders for a customer |
| `POST` | `/` | 🔒 ADMIN/MANAGER | Manually create an order |
| `PUT` | `/{id}` | 🔒 ADMIN/MANAGER | Update an order |
| `DELETE` | `/{id}` | 🔒 ADMIN/MANAGER | Delete an order |

### `GET /api/v1/orders/customer/{customerId}`
Returns all orders for the given customer, with full order item details including product info.

### `GET /api/v1/orders/customer/{customerId}/unpaid`
Returns only orders that do not have a `COMPLETED` payment. Used by the Payments page to populate the "Select Order" dropdown for regular users.

**Response:**
```json
[
  {
    "orderId": 12,
    "localDateTime": "2026-03-23T14:30:00",
    "customer": { "customerId": 3, "name": "João Silva" },
    "orderItems": [
      {
        "orderItemId": 8,
        "product": { "productId": 5, "name": "Wireless Headphones", "price": 29.99 },
        "quantity": 2
      }
    ]
  }
]
```

---

## 💳 Payments

Base path: `/api/v1/payments`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/` | 🔒 ADMIN/MANAGER | List all payments |
| `GET` | `/{id}` | 🔓 Any | Get payment by ID |
| `GET` | `/order/{orderId}` | 🔒 ADMIN/MANAGER | Get payments for an order |
| `GET` | `/customer/{customerId}` | 🔓 Any | Get payments for a customer |
| `POST` | `/` | 🔒 ADMIN/MANAGER | Create a payment record |
| `POST` | `/process` | 🔓 Any | **Process a payment** |
| `PATCH` | `/{id}/status` | 🔒 ADMIN/MANAGER | Update payment status |
| `DELETE` | `/{id}` | 🔒 ADMIN/MANAGER | Delete a payment |
| `GET` | `/methods/customer/{customerId}` | 🔓 Any | Get payment methods |
| `GET` | `/methods/{id}` | 🔓 Any | Get payment method by ID |
| `POST` | `/methods` | 🔒 ADMIN/MANAGER | Add payment method |
| `PUT` | `/methods/{id}/default?customerId={id}` | 🔓 Any | Set default method |
| `DELETE` | `/methods/{id}` | 🔒 ADMIN/MANAGER | Delete payment method |

### `POST /api/v1/payments/process`
The main payment endpoint. Validates ownership, prevents double payment, calculates amount from order items, reduces stock, and publishes Kafka notification.

**Query params:** `orderId`, `customerId`, `paymentMethod`, `token` (optional)

```
POST /api/v1/payments/process?orderId=12&customerId=3&paymentMethod=CREDIT_CARD
```

**Available payment methods:**
- `CREDIT_CARD`
- `DEBIT_CARD`
- `PAYPAL`
- `BANK_TRANSFER`

**Response:**
```json
{
  "id": 7,
  "orderId": 12,
  "customerId": 3,
  "paymentMethod": "CREDIT_CARD",
  "paymentStatus": "COMPLETED",
  "amount": 59.98,
  "currency": "USD",
  "transactionId": "a3f9c1e2-4b8d-11ef-9a21-0242ac120002",
  "createdAt": "2026-03-23T14:32:00"
}
```

**Payment status flow:** `PROCESSING` → `COMPLETED` or `FAILED`

**Side effects on COMPLETED:**
1. ✅ Stock reduced for every item in the order (`tb_products.quantity` + `tb_stocks.quantity`)
2. ✅ Kafka event published to `marketplace-email-notifications` → email sent via MailHog
3. ✅ Kafka event published to `marketplace-payment-notifications` → logged

**Validations:**
- Order must exist
- Order must belong to the requesting customer
- Order must not already be paid (`400 Bad Request` if already COMPLETED)

---

## 🏷️ Discounts & Coupons

Base path: `/api/v1/discounts`

### Discounts

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/` | 🔓 Any | List all discounts |
| `GET` | `/active` | 🔓 Any | List currently active discounts |
| `GET` | `/{id}` | 🔓 Any | Get discount by ID |
| `POST` | `/` | 🔒 ADMIN/MANAGER | Create a discount |
| `PUT` | `/{id}` | 🔒 ADMIN/MANAGER | Update a discount |
| `DELETE` | `/{id}` | 🔒 ADMIN/MANAGER | Delete a discount |

### `POST /api/v1/discounts`
```json
{
  "name": "Summer Sale 2026",
  "description": "20% off everything",
  "discountType": "PERCENTAGE",
  "discountValue": 20.00,
  "minPurchaseAmount": 10.00,
  "maxDiscountAmount": 50.00,
  "startDate": "2026-06-01T00:00:00",
  "endDate": "2026-08-31T23:59:59",
  "isActive": true
}
```

**Discount types:** `PERCENTAGE`, `FIXED`

### Coupons

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/coupons` | 🔓 Any | List all coupons |
| `GET` | `/coupons/active` | 🔓 Any | List active coupons |
| `GET` | `/coupons/{id}` | 🔓 Any | Get coupon by ID |
| `GET` | `/coupons/code/{code}` | 🔓 Any | Get coupon by code |
| `POST` | `/coupons?discountId={id}` | 🔒 ADMIN/MANAGER | Create a coupon |
| `PUT` | `/coupons/{id}` | 🔒 ADMIN/MANAGER | Update a coupon |
| `DELETE` | `/coupons/{id}` | 🔒 ADMIN/MANAGER | Delete a coupon |
| `POST` | `/coupons/validate` | 🔓 Any | Validate a coupon code |
| `POST` | `/coupons/apply` | 🔓 Any | Apply coupon and get discount amount |

### `POST /api/v1/discounts/coupons/validate`
```json
{ "code": "SUMMER20" }
```
**Response:** `{ "valid": true }`

### `POST /api/v1/discounts/coupons/apply`
```json
{
  "code": "SUMMER20",
  "amount": 100.00,
  "customerId": 3
}
```
**Response:**
```json
{
  "discount": 20.00,
  "finalAmount": 80.00
}
```

---

## 📦 Stock Management

Base path: `/api/v1/stocks`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/` | 🔓 Any | List all stock entries |
| `GET` | `/product/{productId}` | 🔓 Any | Get stock for a product |
| `POST` | `/` | 🔒 ADMIN/MANAGER | Create stock entry |
| `PATCH` | `/product/{productId}?quantity={n}` | 🔒 ADMIN/MANAGER | Update stock quantity |
| `DELETE` | `/{id}` | 🔒 ADMIN/MANAGER | Delete stock entry |

### `POST /api/v1/stocks`
```json
{
  "productId": 5,
  "quantity": 100,
  "location": "Warehouse A, Shelf 3"
}
```

**Stock is automatically reduced** when a payment is completed — no manual intervention needed.

**Stock status thresholds (UI):**
| Quantity | Status |
|---|---|
| 0 | 🔴 Out of Stock |
| 1–9 | 🟡 Low Stock |
| 10+ | 🟢 In Stock |

---

## 🔔 Notifications & Kafka

### Kafka Topics

| Topic | Producer | Consumer | Trigger |
|---|---|---|---|
| `marketplace-email-notifications` | `PaymentService` | `NotificationService` | Payment COMPLETED |
| `marketplace-order-notifications` | `CartService` | `NotificationService` | Order created at checkout |
| `marketplace-payment-notifications` | `PaymentService` | `NotificationService` | Payment COMPLETED (audit log) |
| `marketplace-notifications` | General | `NotificationService` | General events |

### Event Format
All topics use a consistent JSON string format:

```json
{
  "email": "customer@example.com",
  "subject": "Payment Confirmation - Order #12",
  "message": "Dear João,\n\nYour payment of $59.98 USD...",
  "type": "PAYMENT_COMPLETED"
}
```

**Event types:** `ORDER_CREATED`, `PAYMENT_COMPLETED`, `ORDER_SHIPPED`

### Notification Flow

```
PaymentService.processPayment()
        │
        ├── notificationProducer.sendEmailNotification()
        │       └── → topic: marketplace-email-notifications
        │               └── NotificationService.handleEmailNotification()
        │                       ├── EmailService.sendEmail()  →  MailHog
        │                       └── NotificationRepository.save()  →  MongoDB
        │
        └── notificationProducer.sendPaymentNotification()
                └── → topic: marketplace-payment-notifications
                        └── NotificationService.handlePaymentNotification()
                                └── logger.info() (audit log)

CartService.checkoutAndCreateOrder()
        │
        └── notificationProducer.sendOrderNotification()
                └── → topic: marketplace-order-notifications
                        └── NotificationService.handleOrderNotification()
                                ├── EmailService.sendEmail()  →  MailHog
                                └── NotificationRepository.save()  →  MongoDB
```

### Viewing Emails (MailHog)
All emails sent by the system are captured by MailHog. Open http://localhost:8025 to view the inbox.

### Viewing Kafka Messages
Open http://localhost:8090 (Kafka UI) → Topics → select a topic → click **Start Consuming**.

### Notifications stored in MongoDB
Notifications (sent or failed) are saved in the `notifications` collection in MongoDB.

```json
{
  "_id": "...",
  "recipient": "customer@example.com",
  "subject": "Payment Confirmation - Order #12",
  "message": "...",
  "type": "PAYMENT_COMPLETED",
  "status": "SENT",
  "createdAt": "2026-03-23T14:32:00"
}
```

---

## 🔄 Complete Flow

End-to-end user journey from registration to order history:

```
1. REGISTER / LOGIN
   └─ POST /auth/register  →  POST /auth/login
      └─ access_token (15 min) + refresh_token cookie (7 days)

2. RESOLVE CUSTOMER ID
   └─ GET /customers/me
      └─ Returns customerId linked to the authenticated email

3. BROWSE PRODUCTS
   └─ GET /products
      └─ Lists all products with name, quantity, price, stock status

4. ADD TO CART
   └─ POST /carts/items?customerId={id}
      Body: { "productId": 5, "quantity": 2 }
      └─ Auto-creates ACTIVE cart if none exists
      └─ Validates stock availability (frontend + backend)
      └─ Cart badge in header updates immediately

5. ADJUST QUANTITIES (optional)
   └─ PUT /carts/items/{productId}?customerId={id}&quantity={n}
      └─ quantity=0 removes the item

6. CHECKOUT → CREATE ORDER
   └─ POST /carts/checkout-order?customerId={id}
      └─ Cart status → CHECKED_OUT
      └─ Order created with all items
      └─ Kafka → marketplace-order-notifications → email sent
      └─ Returns orderId + totalAmount

7. PAY
   └─ POST /payments/process?orderId={id}&customerId={id}&paymentMethod=CREDIT_CARD
      └─ Verifies order ownership
      └─ Prevents double payment
      └─ Calculates amount from product prices
      └─ Status: PROCESSING → COMPLETED
      └─ Stock reduced for every product
      └─ Kafka → marketplace-email-notifications → confirmation email
      └─ Redirected to Orders page

8. VIEW ORDER HISTORY
   └─ GET /orders/customer/{customerId}
      └─ Full order history with items and dates

9. VIEW PAYMENT HISTORY
   └─ GET /payments/customer/{customerId}
      └─ All payments with status, amount, method
```

---

## 🔒 Role-Based Access Control

### Roles

| Role | Access Level |
|---|---|
| `USER` | Own cart, own orders, own payments, products (read-only) |
| `ADMIN` | Everything — full CRUD on all resources |
| `MANAGER` | Same as ADMIN |

### Access Matrix

| Resource | USER | ADMIN/MANAGER |
|---|---|---|
| Products (read) | ✅ | ✅ |
| Products (write) | ❌ | ✅ |
| Cart (own) | ✅ | ✅ |
| Orders (own) | ✅ | ✅ |
| Orders (all) | ❌ | ✅ |
| Payments (own) | ✅ | ✅ |
| Payments (all) | ❌ | ✅ |
| Customers (all) | ❌ | ✅ |
| Stock management | ❌ | ✅ |
| Discounts (write) | ❌ | ✅ |
| Coupons (write) | ❌ | ✅ |
| Payment methods (write) | ❌ | ✅ |

### Frontend Route Protection

Routes are protected at the React Router level:
- `/stocks`, `/customers`, `/discounts` → `AdminRoute` (redirects `USER` to `/dashboard`)
- `/payments`, `/orders` → accessible by all, but filtered by role (USER sees only own data)
- Dashboard KPIs → USER sees personal stats; ADMIN sees platform-wide stats

### JWT Structure
The access token contains a `role` claim:
```json
{
  "sub": "user@example.com",
  "role": "USER",
  "iat": 1711200000,
  "exp": 1711200900
}
```

---

## 🗄️ Database Schema

### PostgreSQL Tables (managed by Flyway)

```
tb_users               — auth accounts (email, password, role, status)
tb_tokens              — JWT refresh tokens (revoked, expired flags)
tb_customers           — customer profiles (name, email, address)
tb_products            — product catalogue (name, quantity, price, version)
tb_orders              — orders (customer_id, order_date)
tb_order_items         — order line items (order_id, product_id, quantity)
tb_carts               — shopping carts (customer_id, status, total_amount)
tb_cart_items          — cart line items (cart_id, product_id, quantity, price, product_name)
tb_stocks              — warehouse stock (product_id, quantity, location)
tb_payments            — payment records (order_id, customer_id, method, status, amount)
tb_payment_methods     — saved payment methods (customer_id, type, last4, expiry)
tb_discounts           — discount campaigns (type, value, dates, active flag)
tb_coupons             — coupon codes (code, discount_id, usage limits)
tb_coupon_usages       — coupon redemption log (coupon_id, customer_id, order_id)
```

### Key relationships
```
tb_users (1) ──────────── (N) tb_tokens
tb_customers (1) ──────── (N) tb_orders
tb_customers (1) ──────── (N) tb_carts
tb_orders (1) ─────────── (N) tb_order_items
tb_order_items (N) ─────── (1) tb_products
tb_carts (1) ──────────── (N) tb_cart_items
tb_cart_items (N) ─────── (1) tb_products
tb_orders (1) ─────────── (N) tb_payments
tb_discounts (1) ──────── (N) tb_coupons
```

### Flyway Migrations

| Version | Description |
|---|---|
| V1 | Create core tables (customers, products, orders, order_items) |
| V2 | Seed initial data (6 customers, 10 products, 7 orders) |
| V3 | Create users and tokens tables |
| V4 | Create stock table |
| V5 | Create cart tables with indexes |
| V6 | Create discounts and coupons tables |
| V7 | Create payment tables and payment_methods |
| V8 | Add total_amount column to tb_carts |
| V9 | Add price column to tb_products with seed prices |
| V10 | Add product_name column to tb_cart_items |
| V11 | Seed default admin user (admin@marketplace.com) |

### MongoDB Collections

| Collection | Purpose |
|---|---|
| `notifications` | All email notifications sent (or failed), with status, type, timestamps |

---

## 📊 Kafka Topics

### Configuration

| Setting | Value |
|---|---|
| Partitions per topic | 3 |
| Replication factor | 1 (single broker) |
| Retention | 168 hours (7 days) |
| Producer serializer | `StringSerializer` |
| Consumer deserializer | `StringDeserializer` |
| Consumer group | `notification-service` |
| Ack mode | `RECORD` (auto-commit after successful processing) |
| Auto create topics | `true` |

### Topic Details

#### `marketplace-email-notifications`
- **Producer:** `PaymentService` (after COMPLETED payment)
- **Consumer:** `NotificationService.handleEmailNotification()`
- **Action:** Sends email via MailHog + saves to MongoDB

#### `marketplace-order-notifications`
- **Producer:** `CartService` (after checkout creates order)
- **Consumer:** `NotificationService.handleOrderNotification()`
- **Action:** Sends order confirmation email + saves to MongoDB

#### `marketplace-payment-notifications`
- **Producer:** `PaymentService` (after COMPLETED payment)
- **Consumer:** `NotificationService.handlePaymentNotification()`
- **Action:** Logs payment confirmation for audit

#### `marketplace-notifications`
- **Producer:** Available for general events
- **Consumer:** `NotificationService.handleGeneralNotification()`
- **Action:** Logs general events

---

## 🐳 Docker Infrastructure

### Service Dependencies

```
zookeeper
    └── kafka (depends on zookeeper)
            └── kafka-ui (depends on kafka)
postgres (with healthcheck)
    └── marketplace-service (waits for: postgres healthy, redis healthy, kafka healthy, mongodb started)
            └── frontend (waits for: marketplace-service healthy)
redis (with healthcheck)
mongodb (with healthcheck)
mailhog
prometheus
    └── grafana (depends on prometheus)
```

### Persistent Volumes

| Volume | Content | Safe to delete? |
|---|---|---|
| `postgres-data` | All application data | ❌ Never (loses users, orders, payments) |
| `redis-data` | Cache and sessions | ✅ Yes (auto-rebuilt) |
| `mongodb-data` | Notification logs | ✅ Yes (loses notification history) |
| `kafka-data` | Kafka log segments | ⚠️ Only with zookeeper volumes together |
| `zookeeper-data` | Zookeeper state + Cluster ID | ⚠️ Only with kafka-data together |
| `zookeeper-logs` | Zookeeper transaction logs | ⚠️ Only with kafka-data together |
| `prometheus-data` | Metrics history | ✅ Yes |
| `grafana-data` | Dashboard configs | ✅ Yes |

> ⚠️ **Critical:** `kafka-data` and `zookeeper-data` must always be deleted together. Deleting only one causes a Cluster ID mismatch that prevents Kafka from starting.

### Health Checks

| Service | Check | Interval |
|---|---|---|
| postgres | `pg_isready -U postgres` | 10s |
| redis | `redis-cli ping` | 10s |
| kafka | `kafka-topics --list` | 10s / 60s start |
| mongodb | `db.adminCommand('ping')` | 10s |
| marketplace-service | `/actuator/health` | 20s / 60s start |
| prometheus | `/-/ready` | 20s |
| grafana | `/api/health` | 20s |

### Exposed Ports

| Container | Internal | External |
|---|---|---|
| frontend | 80 | **3000** |
| marketplace-service | 8080 | **8081** |
| postgres | 5432 | **5433** |
| redis | 6379 | **6379** |
| kafka | 9092 | — (internal only) |
| kafka | 29092 | **29092** (external clients) |
| kafka-ui | 8080 | **8090** |
| mongodb | 27017 | **27017** |
| mailhog SMTP | 1025 | **1025** |
| mailhog UI | 8025 | **8025** |
| prometheus | 9090 | — (internal only) |
| grafana | 3000 | — (monitoring network only) |

---

## ⚠️ Error Reference

### HTTP Status Codes

| Code | Meaning | Common Cause |
|---|---|---|
| `200 OK` | Success | — |
| `201 Created` | Resource created | POST requests |
| `400 Bad Request` | Invalid input | Out of stock, already paid, empty cart |
| `401 Unauthorized` | Missing/expired token | Token expired, not logged in |
| `403 Forbidden` | Insufficient role | USER trying to access ADMIN endpoint |
| `404 Not Found` | Resource missing | Wrong ID, no active cart |
| `409 Conflict` | Version mismatch | `If-Match` header doesn't match product version |
| `500 Internal Server Error` | Server error | URI creation failure |

### Common Errors & Solutions

| Error | Cause | Solution |
|---|---|---|
| `403` on `/api/v1/orders` | USER role trying admin endpoint | Use `/orders/customer/{id}` instead |
| `404` on `/carts/customer/{id}` | No active cart | Add a product to the cart first |
| `400` "Cannot checkout empty cart" | Cart has no items | Add items before calling checkout |
| `400` "Order already paid" | Duplicate payment attempt | Check payments list for existing COMPLETED payment |
| `400` "Order does not belong to you" | Wrong customerId | Use the customerId from `/customers/me` |
| `409 Conflict` on product update | Stale version | Fetch product again, use current `version` in `If-Match` |
| `400` "Not enough stock" | Requesting more than available | Reduce quantity or wait for restock |
| Kafka `InconsistentClusterIdException` | kafka-data/zookeeper-data mismatch | Delete both volumes and restart |

---

## 🧪 Testing

Tests are located in `src/test/java/code/vanilson/marketplace/service/`.

### Run tests
```bash
# Run all tests (skipped during Docker build)
mvn test

# Run a specific test class
mvn test -Dtest=PaymentServiceTest

# Run with verbose output
mvn test -Dtest=CartServiceTest -pl . -am
```

### Test coverage

| Service | Tests |
|---|---|
| `AuthenticationServiceTest` | Register, login, token generation, role claim, change password |
| `CartServiceTest` | Add item, update quantity, stock validation, checkout |
| `PaymentServiceTest` | Process payment, ownership check, double payment, stock reduction, Kafka notification |
| `OrderServiceImplTest` | Find all, find by customer, find unpaid, create, update, delete |

---

## 📁 Project Structure

```
marketplace-api/
├── src/
│   ├── main/
│   │   ├── java/code/vanilson/marketplace/
│   │   │   ├── config/          — Security, JWT, Kafka, Redis configuration
│   │   │   ├── controller/      — REST controllers (auth, cart, order, payment…)
│   │   │   ├── dto/             — Data Transfer Objects
│   │   │   ├── exception/       — Custom exceptions + global handler
│   │   │   ├── mapper/          — Entity ↔ DTO mappers
│   │   │   ├── model/           — JPA entities + MongoDB documents
│   │   │   ├── repository/      — Spring Data JPA + MongoDB repositories
│   │   │   └── service/         — Business logic layer
│   │   └── resources/
│   │       ├── db/migration/    — Flyway SQL scripts (V1–V11)
│   │       ├── application.yml         — Local development config
│   │       └── application-docker.yml  — Docker environment config
│   └── test/                    — Unit tests (Mockito + JUnit 5)
├── frontend/
│   ├── src/
│   │   ├── api/         — API client, types
│   │   ├── components/  — Reusable UI components
│   │   ├── contexts/    — React contexts (theme, notifications)
│   │   ├── hooks/       — Custom hooks + route guards
│   │   ├── pages/       — Page components (dashboard, products, cart…)
│   │   ├── services/    — TanStack Query hooks per domain
│   │   ├── store/       — Zustand global state
│   │   └── utils/       — JWT decode, cookies
│   ├── Dockerfile       — Multi-stage: Vite build → Nginx serve
│   └── nginx.conf       — SPA routing + API proxy
├── db/migration/        — Flyway scripts (mirrored for Docker flyway service)
├── prometheus/          — Prometheus scrape config
├── docker-compose.yml   — Full infrastructure definition
├── Dockerfile           — Multi-stage: Maven build → JRE runtime
├── GUIDE.md             — Usage guide (português)
└── README.md            — This file
```

---

<div align="center">

Built with ❤️ using Spring Boot · React · Kafka · PostgreSQL · Docker

</div>
