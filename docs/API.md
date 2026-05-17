# Festival QR Order API

## Public

- `GET /api/menus`
- `GET /api/menus/{id}`
- `POST /api/orders`
- `GET /api/orders/{id}`
- `GET /api/orders/{id}/stream`
- `GET /api/orders/table/{tableId}`

## Admin

Use `Authorization: Bearer <token>`.

- `POST /api/admin/login`
- `GET /api/admin/orders`
- `GET /api/admin/orders/stream`
- `PATCH /api/admin/orders/{id}/status`
- `POST /api/admin/menus`
- `PUT /api/admin/menus/{id}`
- `POST /api/admin/tables`
- `GET /api/admin/tables`

## Thymeleaf Pages

- `GET /table/{tableNumber}`
- `GET /cart`
- `GET /payment/success`
- `GET /payment/fail`
- `GET /orders/{orderId}`
- `GET /admin/login`
- `GET /admin/orders`

## Current Order Flow

Payment is disabled. The cart page calls `POST /api/orders` directly, then redirects to `/orders/{orderId}`. Admin receives the new order through SSE at `/api/admin/orders/stream`.
