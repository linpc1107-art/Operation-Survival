# Operation Survival

Operation Survival 是一個以 Spring Boot + MySQL + 原生 HTML/JavaScript 製作的回合制生存冒險原型。專案包含可直接啟動的後端 API、單頁前端操作介面，以及基本測試與開發腳本。

## 目前內容

- 帳號註冊 / 登入
- JWT 驗證與受保護 API
- 玩家狀態查詢
- 武器查詢與升級
- 技能升級、補血、藥水與商店購買
- 關卡與怪物查詢
- 戰鬥攻擊與技能攻擊
- 進度儲存 / 讀取 / 重置
- 啟動時自動補齊基礎怪物資料

## 技術組成

- 後端：Spring Boot 3、Spring Security、Spring Data JPA
- 資料庫：MySQL 8
- 前端：原生 HTML / CSS / JavaScript
- 測試：JUnit 5、Spring Boot Test、H2（測試環境）

## 專案結構

```text
backend/survival  Spring Boot 後端
frontend          靜態前端頁面
database          本機 MySQL 啟動設定
scripts           開發輔助腳本
docs              額外文件
```

## 快速開始

### 1. 啟動資料庫

如果你有 Docker，建議直接使用專案內建設定：

```bash
cd database
docker compose up -d
```

這會建立：

- Database: survival
- Port: 3306
- Root password: root

如果你使用自己的 MySQL，也可以自行建立 survival 資料庫，再透過環境變數覆蓋連線設定。

### 2. 一鍵啟動前後端

```bash
chmod +x scripts/start-dev.sh
./scripts/start-dev.sh
```

腳本會：

- 啟動 frontend 靜態伺服器於 8000
- 優先啟動 backend 於 8080
- 若 8080 已被占用，改啟動於 8081
- 若 MySQL 不可用，自動改用內建 H2 dev 模式啟動
- 自動開啟帶有正確 apiHost 參數的前端頁面

不想先安裝 MySQL 時，也可以直接執行這支腳本開始遊玩。

### 3. 手動啟動

後端：

```bash
cd backend/survival
./mvnw spring-boot:run
```

若要用免 MySQL 的開發模式：

```bash
cd backend/survival
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

前端：

```bash
cd frontend
python3 -m http.server 8000
```

之後開啟：

```text
http://127.0.0.1:8000/index.html?apiHost=http://127.0.0.1:8080
```

## 正式版啟動（本機）

```bash
chmod +x scripts/start-prod.sh
APP_JWT_SECRET='replace-with-long-random-secret' ./scripts/start-prod.sh
```

說明：

- 會使用 `SPRING_PROFILES_ACTIVE=prod`
- 需要提供 `APP_JWT_SECRET`
- 預設後端 8080、前端 8000，可用 `BACKEND_PORT`、`FRONTEND_PORT` 覆蓋
- 若本機 MySQL 無法連線，`start-prod.sh` 預設會切到 H2 fallback（同為 prod profile，方便對外測試）
- 如要強制只允許 MySQL，可設定 `PROD_ALLOW_H2_FALLBACK=false`

## ngrok 測試流程

```bash
chmod +x scripts/start-ngrok.sh
APP_JWT_SECRET='replace-with-long-random-secret' ./scripts/start-ngrok.sh
```

腳本會輸出可直接外部測試的網址：

```text
https://<frontend-ngrok>/index.html?apiHost=https://<backend-ngrok>
```

如需限制來源，可設定：

```bash
APP_CORS_ALLOWED_ORIGIN_PATTERNS='https://your-frontend-domain,https://*.ngrok-free.app'
```

如需指定正式資料庫（建議正式環境）：

```bash
DB_URL='jdbc:mysql://127.0.0.1:3306/survival?useSSL=false&serverTimezone=Asia/Taipei&allowPublicKeyRetrieval=true' \
DB_USERNAME='root' \
DB_PASSWORD='your-password' \
APP_JWT_SECRET='replace-with-long-random-secret' \
./scripts/start-prod.sh
```

## 環境變數

後端支援以下覆蓋設定：

| 變數 | 預設值 | 用途 |
| --- | --- | --- |
| SERVER_PORT | 8080 | 後端埠號 |
| SERVER_ADDRESS | 0.0.0.0 | 綁定地址 |
| DB_URL | jdbc:mysql://localhost:3306/survival?... | MySQL 連線字串 |
| DB_USERNAME | root | 資料庫帳號 |
| DB_PASSWORD | 空字串 | 資料庫密碼 |
| JPA_DDL_AUTO | update | JPA schema 策略 |
| JPA_SHOW_SQL | true | 是否輸出 SQL |
| JPA_FORMAT_SQL | true | 是否格式化 SQL |
| APP_JWT_SECRET | 內建開發值 | JWT 密鑰 |

範例：

```bash
cd backend/survival
DB_PASSWORD=root APP_JWT_SECRET=my-dev-secret ./mvnw spring-boot:run
```

## 測試

```bash
cd backend/survival
./mvnw test
```

測試環境已改為使用 H2 in-memory database，因此不需要先啟動本機 MySQL。

## API 文件與 Postman

- API 文件：docs/API.md
- Postman Collection：docs/Operation-Survival.postman_collection.json

## 主要 API

- GET /
- GET /hello
- POST /api/auth/register
- POST /api/auth/login
- GET /api/player/me
- GET /api/monster/stage/{stage}
- GET /api/weapon/me
- POST /api/weapon/upgrade
- POST /api/battle/attack
- GET /api/progress
- GET /api/shop/items
- POST /api/progress/save

## 常見問題

### 找不到後端

- 先確認 backend 是否啟動於 8080 或 8081
- 可直接使用 scripts/start-dev.sh 自動帶入 apiHost

### 前端能開但 API 失敗

- 確認瀏覽器使用的網址含有 apiHost 參數
- 確認 JWT 已成功登入並寫入 localStorage

### 啟動時資料庫連不上

- 檢查 MySQL 是否存在 survival 資料庫
- 若使用 docker compose，確認 database 容器 healthy
- 若密碼不是空字串，請設定 DB_PASSWORD
