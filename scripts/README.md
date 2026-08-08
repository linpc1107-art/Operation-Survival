# Dev Scripts

## Start Full Dev Environment

```bash
chmod +x scripts/start-dev.sh
./scripts/start-dev.sh
```

- Frontend: http://127.0.0.1:8000
- Backend: start on http://127.0.0.1:8080 if free, otherwise start on http://127.0.0.1:8081

Behavior:

- Start frontend static server on port 8000 (if not already running)
- Check backend port 8080 availability
- If 8080 is occupied by another service: start Operation Survival backend on 8081
- If MySQL is unavailable: retry backend startup with built-in H2 dev profile
- Automatically open browser page with explicit apiHost parameter

## Start Production-like Local Environment

```bash
chmod +x scripts/start-prod.sh
APP_JWT_SECRET='replace-with-long-random-secret' ./scripts/start-prod.sh
```

Behavior:

- Always starts backend with `SPRING_PROFILES_ACTIVE=prod`
- Requires `APP_JWT_SECRET`
- Uses `BACKEND_PORT` / `FRONTEND_PORT` if provided (defaults 8080 / 8000)
- If MySQL is unavailable, falls back to H2 by default for local prod-like testing
- Set `PROD_ALLOW_H2_FALLBACK=false` to force MySQL-only startup

## Start ngrok Tunnels For External Testing

```bash
chmod +x scripts/start-ngrok.sh
APP_JWT_SECRET='replace-with-long-random-secret' ./scripts/start-ngrok.sh
```

Behavior:

- Starts production-like local environment first
- Opens two ngrok tunnels: frontend(8000), backend(8080)
- Prints play URL: `https://<frontend-ngrok>/index.html?apiHost=https://<backend-ngrok>`
