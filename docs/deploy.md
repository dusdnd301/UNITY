# Production Deploy

This project is intended to run as one Spring Boot server behind Nginx. Customers scan QR codes that contain the public HTTPS domain, for example `https://order.example.com/table/1`.

## Required

- Public domain such as `order.example.com`
- Cloud VM: Oracle Cloud, AWS EC2, or similar
- Open inbound ports `80` and `443`
- Docker and Docker Compose
- Nginx and Certbot

## 1. Configure Environment

Copy the example file:

```bash
cp .env.example .env
```

Edit `.env`:

```env
APP_BASE_URL=https://order.example.com
CORS_ALLOWED_ORIGINS=https://order.example.com
ADMIN_COOKIE_SECURE=true
SPRING_PROFILES_ACTIVE=prod

DB_USERNAME=festival
DB_PASSWORD=use-a-real-password
MYSQL_ROOT_PASSWORD=use-a-real-root-password
JPA_DDL_AUTO=update

JWT_SECRET=use-a-long-random-secret-at-least-32-bytes
ADMIN_USERNAME=admin
ADMIN_PASSWORD=use-a-real-admin-password
```

For first deployment, keep `JPA_DDL_AUTO=update`. After the database is created and verified, change it to `validate`.

## 2. Run MySQL and Spring Boot

```bash
docker compose up -d --build
docker compose logs -f api
```

The MySQL port is not exposed publicly. Only the Spring Boot app listens on host port `8080`.

## 3. Nginx Reverse Proxy

Copy `deploy/nginx.conf`:

```bash
sudo cp deploy/nginx.conf /etc/nginx/sites-available/festival-order
sudo ln -s /etc/nginx/sites-available/festival-order /etc/nginx/sites-enabled/festival-order
sudo nginx -t
sudo systemctl reload nginx
```

Replace `your-domain.com` in the file with your real domain.

## 4. HTTPS

```bash
sudo certbot --nginx -d order.example.com
```

## 5. Generate QR Codes

Open:

```text
https://order.example.com/admin/login
```

After login, use `현재 주소로 QR 재생성`. Print QR images from the admin table section. QR images point to:

```text
https://order.example.com/table/1
https://order.example.com/table/2
https://order.example.com/table/3
```

## Field Checklist

- Open `https://order.example.com/table/1` on a phone using LTE/5G.
- Add menu items and press `주문 넣기`.
- Confirm `/admin/orders` updates in real time.
- Change order status to `조리`, `픽업`, `완료`.
- Confirm the customer order status page updates in real time.

## Useful Commands

```bash
docker compose ps
docker compose logs -f api
docker compose restart api
docker compose exec mysql mysql -ufestival -p festival_order
```
