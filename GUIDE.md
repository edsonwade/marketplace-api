# 📖 Guia de Utilização — Marketplace API

## Como o serviço funciona — Fluxo completo

---

## 🏗️ Arquitectura do sistema

```
Browser (React)
    │
    │  HTTP requests com Bearer Token
    ▼
Nginx (porta 3000)
    │  proxy /api/* →
    ▼
Spring Boot (porta 8080/8081)
    │
    ├── PostgreSQL  — produtos, clientes, pedidos, pagamentos, stock
    ├── Redis       — cache + rate limiting
    ├── MongoDB     — notificações
    └── Kafka       — eventos assíncronos
```

---

## 🔑 1. Autenticação (obrigatória antes de tudo)

### Registar conta
```
POST /api/v1/auth/register
{
  "email": "joao@empresa.com",
  "password": "Senha@123"
}
```
**Requisitos da password:** 8+ caracteres, maiúscula, minúscula, número, carácter especial.

### Fazer login
```
POST /api/v1/auth/login
{
  "email": "joao@empresa.com",
  "password": "Senha@123"
}
```
**Resposta:**
```json
{
  "access_token": "eyJ...",
  "refresh_token": "eyJ..."
}
```
- O `access_token` dura ~15 minutos (em memória)
- O `refresh_token` dura 7 dias (em cookie HttpOnly)
- O frontend renova o access_token automaticamente quando expira

---

## 📦 2. Produtos

| Ação | Método | Endpoint |
|------|--------|----------|
| Ver todos | GET | `/api/v1/products` |
| Ver um | GET | `/api/v1/products/{id}` |
| Criar | POST | `/api/v1/products` |
| Actualizar | PUT | `/api/v1/products/{id}` (requer header `If-Match: {version}`) |
| Apagar | DELETE | `/api/v1/products/{id}` |

**Criar produto:**
```json
POST /api/v1/products
{
  "name": "Headphones Pro",
  "quantity": 50,
  "price": 29.99
}
```

> ⚠️ O campo `version` é usado para **optimistic locking** — evita conflitos de edição simultânea.
> Ao fazer PUT, enviar `If-Match: {version_actual}` no header.

---

## 🛒 3. Carrinho de Compras (backend-persistido)

O carrinho é guardado na base de dados — **não se perde ao fazer refresh da página**.

| Ação | Método | Endpoint |
|------|--------|----------|
| Ver carrinho do cliente | GET | `/api/v1/carts/customer/{customerId}` |
| Criar carrinho | POST | `/api/v1/carts?customerId={id}` |
| Adicionar item | POST | `/api/v1/carts/items?customerId={id}` |
| Actualizar quantidade | PUT | `/api/v1/carts/items/{productId}?customerId={id}&quantity={n}` |
| Remover item | DELETE | `/api/v1/carts/items/{productId}?customerId={id}` |
| Limpar carrinho | DELETE | `/api/v1/carts/clear?customerId={id}` |
| **Checkout (cria Order)** | POST | `/api/v1/carts/checkout-order?customerId={id}` |

**Adicionar produto ao carrinho:**
```json
POST /api/v1/carts/items?customerId=3
{
  "productId": 5,
  "quantity": 2
}
```

**Checkout** — transforma o carrinho num Pedido:
```
POST /api/v1/carts/checkout-order?customerId=3
```
Resposta:
```json
{
  "orderId": 12,
  "localDateTime": "2024-03-21T18:30:00",
  "customer": { "customerId": 3, "name": "João" },
  "orderItems": [...],
  "totalAmount": 59.98
}
```

---

## 📋 4. Pedidos (Orders)

Os pedidos são criados automaticamente pelo checkout.
**Não é necessário criar pedidos manualmente.**

| Ação | Método | Endpoint |
|------|--------|----------|
| Ver todos | GET | `/api/v1/orders` |
| Ver um | GET | `/api/v1/orders/{id}` |
| Actualizar | PUT | `/api/v1/orders/{id}` |
| Apagar | DELETE | `/api/v1/orders/{id}` |

---

## 💳 5. Pagamentos

Após o checkout criar um Order, paga-se usando o `orderId` devolvido.

| Ação | Método | Endpoint |
|------|--------|----------|
| Ver todos | GET | `/api/v1/payments` |
| Ver por order | GET | `/api/v1/payments/order/{orderId}` |
| **Processar pagamento** | POST | `/api/v1/payments/process` |
| Actualizar status | PATCH | `/api/v1/payments/{id}/status` |
| Apagar | DELETE | `/api/v1/payments/{id}` |

**Processar pagamento:**
```
POST /api/v1/payments/process
  ?orderId=12
  &customerId=3
  &paymentMethod=CREDIT_CARD
```
Métodos disponíveis: `CREDIT_CARD`, `DEBIT_CARD`, `PAYPAL`, `BANK_TRANSFER`

**Resposta:**
```json
{
  "id": 7,
  "orderId": 12,
  "customerId": 3,
  "paymentMethod": "CREDIT_CARD",
  "paymentStatus": "COMPLETED",
  "amount": 59.98,
  "currency": "USD",
  "transactionId": "uuid-gerado"
}
```

Status possíveis: `PROCESSING` → `COMPLETED` / `FAILED`

---

## 👤 6. Clientes

| Ação | Método | Endpoint |
|------|--------|----------|
| Ver todos | GET | `/api/v1/customers` |
| Ver um | GET | `/api/v1/customers/{id}` |
| Criar | POST | `/api/v1/customers` |
| Actualizar | PUT | `/api/v1/customers/{id}` |
| Apagar | DELETE | `/api/v1/customers/{id}` |

```json
POST /api/v1/customers
{
  "name": "João Silva",
  "email": "joao@empresa.com",
  "address": "Rua Principal 123, Lisboa"
}
```

---

## 🏷️ 7. Descontos e Cupões

**Descontos:**
| Ação | Endpoint |
|------|----------|
| Listar todos | GET `/api/v1/discounts` |
| Activos | GET `/api/v1/discounts/active` |
| Criar | POST `/api/v1/discounts` |
| Actualizar | PUT `/api/v1/discounts/{id}` |
| Apagar | DELETE `/api/v1/discounts/{id}` |

**Cupões:**
| Ação | Endpoint |
|------|----------|
| Listar | GET `/api/v1/discounts/coupons` |
| Por código | GET `/api/v1/discounts/coupons/code/{code}` |
| Criar | POST `/api/v1/discounts/coupons?discountId={id}` |
| Validar | POST `/api/v1/discounts/coupons/validate` → `{"code": "SUMMER20"}` |
| Aplicar | POST `/api/v1/discounts/coupons/apply` → `{"code": "SUMMER20", "amount": 100, "customerId": 3}` |

---

## 📦 8. Stock

| Ação | Método | Endpoint |
|------|--------|----------|
| Ver todos | GET | `/api/v1/stocks` |
| Por produto | GET | `/api/v1/stocks/product/{productId}` |
| Criar entrada | POST | `/api/v1/stocks` |
| Actualizar quantidade | PATCH | `/api/v1/stocks/product/{productId}?quantity={n}` |
| Apagar | DELETE | `/api/v1/stocks/{id}` |

```json
POST /api/v1/stocks
{
  "productId": 5,
  "quantity": 100,
  "location": "Armazém A, Prateleira 3"
}
```

---

## 💳 9. Métodos de Pagamento (por cliente)

| Ação | Endpoint |
|------|----------|
| Por cliente | GET `/api/v1/payments/methods/customer/{customerId}` |
| Adicionar | POST `/api/v1/payments/methods` |
| Definir como default | PUT `/api/v1/payments/methods/{id}/default?customerId={id}` |
| Apagar | DELETE `/api/v1/payments/methods/{id}` |

---

## 🔄 Fluxo completo passo a passo

```
┌─────────────────────────────────────────────────────────┐
│                    FLUXO COMPLETO                        │
└─────────────────────────────────────────────────────────┘

 1. REGISTAR/LOGIN
    └── POST /auth/register → POST /auth/login
        └── Guarda access_token (memória) + refresh_token (cookie)

 2. VER PRODUTOS
    └── GET /products
        └── Lista com nome, quantidade, preço, status de stock

 3. ADICIONAR AO CARRINHO
    └── POST /carts/items?customerId={id}
        Body: { productId, quantity }
        └── Cria carrinho ACTIVE automaticamente se não existir

 4. AJUSTAR QUANTIDADES (opcional)
    └── PUT /carts/items/{productId}?customerId={id}&quantity={n}
        └── quantity=0 remove o item automaticamente

 5. CHECKOUT → CRIA ORDER
    └── POST /carts/checkout-order?customerId={id}
        └── Carrinho → status CHECKED_OUT
        └── Cria Order com todos os items
        └── Devolve orderId + totalAmount

 6. PAGAR
    └── POST /payments/process
        Params: orderId, customerId, paymentMethod
        └── Calcula amount a partir dos produtos do pedido
        └── Grava transação com UUID único
        └── Status: PROCESSING → COMPLETED (ou FAILED)

 7. VER PEDIDO
    └── GET /orders → lista todos os pedidos com items
    └── GET /orders/{id} → detalhes do pedido específico

 8. VER PAGAMENTO
    └── GET /payments → lista todos os pagamentos
    └── GET /payments/order/{orderId} → pagamento deste pedido
```

---

## ⚠️ Erros comuns e soluções

| Erro | Causa | Solução |
|------|-------|---------|
| `403 Forbidden` | Token expirado ou ausente | Fazer login novamente |
| `404 Not Found` no cart | Sem carrinho activo | Adicionar um produto primeiro |
| `400 Bad Request` no checkout | Carrinho vazio | Adicionar produtos antes de fazer checkout |
| `404 Not Found` no payment | orderId inválido | Usar o orderId devolvido pelo checkout |
| `409 Conflict` no PUT produto | Versão desactualizada | Buscar produto novamente e usar version actual |
| `400` ao registar | Email já existe | Usar email diferente |

---

## 🔐 Autenticação nas chamadas manuais (Swagger/Postman)

Todas as rotas (excepto `/auth/**`) requerem o header:
```
Authorization: Bearer {access_token}
```

No Swagger UI (disponível em `http://localhost:8081/swagger-ui.html`):
1. Clica no botão **Authorize** (cadeado)
2. Introduz: `Bearer {access_token}`
3. Confirma — todas as chamadas passam a enviar o token automaticamente

---

## 🌐 URLs do sistema

| Serviço | URL |
|---------|-----|
| Frontend (React) | http://localhost:3000 |
| Backend API | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| Kafka UI | http://localhost:8090 |
| MailHog | http://localhost:8025 |
| PostgreSQL | localhost:5433 |
