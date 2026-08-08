#!/usr/bin/env zsh
set -euo pipefail

ROOT_DIR="/Volumes/資料統整區/Operation-Survival"
BACKEND_DIR="$ROOT_DIR/backend/survival"
FRONTEND_DIR="$ROOT_DIR/frontend"

FRONTEND_PORT="${FRONTEND_PORT:-8000}"
BACKEND_PORT="${BACKEND_PORT:-8080}"
PROD_ALLOW_H2_FALLBACK="${PROD_ALLOW_H2_FALLBACK:-true}"

frontend_log="/tmp/operation-survival-frontend-prod.log"
backend_log="/tmp/operation-survival-backend-prod.log"
backend_fallback_log="/tmp/operation-survival-backend-prod-h2.log"

print_status() {
  echo "[Operation-Survival:prod] $1"
}

require_env() {
  local name="$1"
  if [[ -z "${(P)name:-}" ]]; then
    echo "缺少必要環境變數：$name" >&2
    exit 1
  fi
}

wait_for_backend() {
  local port="$1"
  local retries=45

  while (( retries > 0 )); do
    if curl --silent --max-time 2 "http://127.0.0.1:${port}/hello" | grep -q "Hello Spring Boot"; then
      return 0
    fi
    retries=$((retries - 1))
    sleep 1
  done
  return 1
}

start_backend_prod_mysql() {
  (
    cd "$BACKEND_DIR"
    SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=${BACKEND_PORT} >"$backend_log" 2>&1 &
  )
}

start_backend_prod_h2() {
  (
    cd "$BACKEND_DIR"
    SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=${BACKEND_PORT} --spring.datasource.url=jdbc:h2:file:./data/survival-prod-h2;MODE=MYSQL;DATABASE_TO_LOWER=TRUE --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.username=sa --spring.datasource.password= --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect" >"$backend_fallback_log" 2>&1 &
  )
}

if [[ ! -f "$BACKEND_DIR/pom.xml" ]]; then
  echo "Backend 專案不存在：$BACKEND_DIR" >&2
  exit 1
fi

if [[ ! -f "$FRONTEND_DIR/index.html" ]]; then
  echo "Frontend 檔案不存在：$FRONTEND_DIR/index.html" >&2
  exit 1
fi

require_env APP_JWT_SECRET

if lsof -nP -iTCP:${BACKEND_PORT} -sTCP:LISTEN >/dev/null 2>&1; then
  print_status "停止既有後端程序（port ${BACKEND_PORT}）"
  lsof -t -iTCP:${BACKEND_PORT} -sTCP:LISTEN | xargs kill
fi

if ! lsof -nP -iTCP:${FRONTEND_PORT} -sTCP:LISTEN >/dev/null 2>&1; then
  print_status "啟動前端靜態伺服器 (${FRONTEND_PORT})"
  (cd "$FRONTEND_DIR" && python3 -m http.server ${FRONTEND_PORT} >"$frontend_log" 2>&1 &)
else
  print_status "前端靜態伺服器已在 ${FRONTEND_PORT} 執行中"
fi

print_status "啟動後端（prod profile, port ${BACKEND_PORT}）"
start_backend_prod_mysql

if ! wait_for_backend "$BACKEND_PORT"; then
  if [[ "$PROD_ALLOW_H2_FALLBACK" == "true" || "$PROD_ALLOW_H2_FALLBACK" == "1" ]]; then
    print_status "MySQL 啟動失敗，切換到 prod+H2 fallback（本機測試用）"
    if lsof -nP -iTCP:${BACKEND_PORT} -sTCP:LISTEN >/dev/null 2>&1; then
      lsof -t -iTCP:${BACKEND_PORT} -sTCP:LISTEN | xargs kill
    fi
    start_backend_prod_h2
    if ! wait_for_backend "$BACKEND_PORT"; then
      print_status "後端啟動失敗，請檢查 ${backend_log} 或 ${backend_fallback_log}"
      exit 1
    fi
    print_status "目前使用 H2 fallback。正式部署建議設定 DB_URL/DB_USERNAME/DB_PASSWORD 使用 MySQL。"
  else
    print_status "後端啟動失敗，請檢查 ${backend_log}"
    exit 1
  fi
fi

print_status "完成：前端 http://127.0.0.1:${FRONTEND_PORT}"
print_status "完成：後端 http://127.0.0.1:${BACKEND_PORT}"
print_status "Backend log: $backend_log"
print_status "Frontend log: $frontend_log"
if [[ -f "$backend_fallback_log" ]]; then
  print_status "Backend fallback log: $backend_fallback_log"
fi

open "http://127.0.0.1:${FRONTEND_PORT}/index.html?apiHost=http://127.0.0.1:${BACKEND_PORT}" >/dev/null 2>&1 || true