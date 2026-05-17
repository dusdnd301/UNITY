# Temporary Demo With ngrok

Use this when you want phones to scan QR codes without deploying to AWS or Oracle Cloud.

## 1. Install ngrok

macOS:

```bash
brew install ngrok/ngrok/ngrok
```

Then connect your ngrok account token:

```bash
ngrok config add-authtoken YOUR_NGROK_TOKEN
```

## 2. Start Spring Boot Locally

The app starts a development MySQL container automatically through `compose.dev.yml`.

```bash
./gradlew bootRun
```

Keep this terminal open.

## 3. Start ngrok

Open a second terminal:

```bash
ngrok http 8080
```

Copy the HTTPS forwarding URL. It looks like:

```text
https://abcd-1234.ngrok-free.app
```

## 4. Restart Spring Boot With the ngrok URL

Stop Spring Boot with `Ctrl+C`, then run:

```bash
APP_BASE_URL=https://abcd-1234.ngrok-free.app \
CORS_ALLOWED_ORIGINS=https://abcd-1234.ngrok-free.app,http://localhost:8080 \
ADMIN_COOKIE_SECURE=true \
./gradlew bootRun
```

Use your real ngrok URL.

## 5. Regenerate QR Codes

Open:

```text
https://abcd-1234.ngrok-free.app/admin/login
```

Default demo login:

```text
admin / admin1234
```

Click `현재 주소로 QR 재생성`, then open `QR 보기`.

## 6. Phone Test

On a phone using LTE/5G, scan the QR or open:

```text
https://abcd-1234.ngrok-free.app/table/1
```

Add menu items and press `주문 넣기`. The order should appear at:

```text
https://abcd-1234.ngrok-free.app/admin/orders
```

## Notes

- Free ngrok URLs change every time unless you reserve a domain.
- Whenever the ngrok URL changes, restart Spring Boot with the new `APP_BASE_URL` and regenerate QR codes.
- This is suitable for demos, not final festival operation.
