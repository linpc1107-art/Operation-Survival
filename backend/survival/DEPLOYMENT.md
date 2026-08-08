# Render deployment guide

## 1. Backend (Render)

### Recommended deploy method
- Use Render Web Service
- Choose Docker
- Point to this folder: `backend/survival`

### Environment variables
- `PORT=8080`
- `DB_URL=jdbc:mysql://<host>:3306/<database>?useSSL=false&serverTimezone=Asia/Taipei&allowPublicKeyRetrieval=true`
- `DB_USERNAME=<username>`
- `DB_PASSWORD=<password>`
- `APP_JWT_SECRET=<strong-random-secret>`
- `JPA_DDL_AUTO=update`

### Build command
Render will use the included Dockerfile automatically.

### Start command
Not needed for Docker mode.

---

## 2. Frontend (Vercel)

1. Deploy the frontend folder to Vercel.
2. Update `frontend/vercel.json` and replace the placeholder backend URL:

```json
{
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://<YOUR-RENDER-BACKEND-URL>.onrender.com/api/:path*"
    }
  ]
}
```

3. Redeploy the Vercel project after changing the URL.
