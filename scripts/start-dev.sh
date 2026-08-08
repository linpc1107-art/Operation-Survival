#!/usr/bin/env zsh
set -euo pipefail

ROOT_DIR="/Volumes/資料統整區/Operation-Survival"
BACKEND_DIR="$ROOT_DIR/backend/survival"
FRONTEND_DIR="$ROOT_DIR/frontend"
FRONTEND_PORT=8000
BACKEND_PRIMARY_PORT=8080
BACKEND_FALLBACK_PORT=8081

frontend_log="/tmp/operation-survival-frontend.log"
backend_log="/tmp/operation-survival-backend.log"

start_backend() {
  local port="$1"
  local profile="${2:-default}"

  if [[ "$profile" == "dev" ]]; then
    (cd "$BACKEND_DIR" && SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=${port} >"$backend_log" 2>&1 &)
  else
    (cd "$BACKEND_DIR" && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=${port} >"$backend_log" 2>&1 &)
  fi
}

start_backend_with_fallback() {
  local port="$1"

  print_status "啟動後端 API (${port})..."
  start_backend "$port"
  if wait_for_backend "$port"; then
    print_status "後端已連上 MySQL 模式"
    return 0
  fi

  if grep -q "Unable to determine Dialect\|Communications link failure\|Connection refused\|Access denied for user" "$backend_log" 2>/dev/null; then
    print_status "MySQL 無法使用，改以內建 H2 dev 模式啟動"
    start_backend "$port" "dev"
    if wait_for_backend "$port"; then
      print_status "後端已切換為 H2 dev 模式"
      return 0
    fi
  fi

  return 1
}

print_status() {
  echo "[Operation-Survival] $1"
}

wait_for_backend() {
  local port="$1"
  local retries=30

  while (( retries > 0 )); do
    if curl --silent --max-time 2 "http://127.0.0.1:${port}/" | grep -q "Operation Survival Backend"; then
      return 0
    fi

    retries=$((retries - 1))
    sleep 1
  done

  return 1
}

if [[ ! -f "$BACKEND_DIR/pom.xml" ]]; then
  echo "Backend 專案不存在：$BACKEND_DIR" >&2
  exit 1
fi

if [[ ! -f "$FRONTEND_DIR/index.html" ]]; then
  echo "Frontend 檔案不存在：$FRONTEND_DIR/index.html" >&2
  exit 1
fi

print_status "啟動前端靜態伺服器 (${FRONTEND_PORT})..."
if ! lsof -nP -iTCP:${FRONTEND_PORT} -sTCP:LISTEN >/dev/null 2>&1; then
  (cd "$FRONTEND_DIR" && python3 -m http.server ${FRONTEND_PORT} >"$frontend_log" 2>&1 &)
fi

selected_backend_port=""

print_status "檢查後端 API (${BACKEND_PRIMARY_PORT})..."
if lsof -nP -iTCP:${BACKEND_PRIMARY_PORT} -sTCP:LISTEN >/dev/null 2>&1; then
  print_status "${BACKEND_PRIMARY_PORT} 已被占用，啟動本專案後端於 ${BACKEND_FALLBACK_PORT}"
  if start_backend_with_fallback "$BACKEND_FALLBACK_PORT"; then
    selected_backend_port="$BACKEND_FALLBACK_PORT"
  else
    print_status "後端啟動失敗，請檢查 $backend_log"
    exit 1
  fi
else
  if start_backend_with_fallback "$BACKEND_PRIMARY_PORT"; then
    selected_backend_port="$BACKEND_PRIMARY_PORT"
  else
    print_status "後端啟動失敗，請檢查 $backend_log"
    exit 1
  fi
fi

print_status "完成：前端 http://127.0.0.1:${FRONTEND_PORT}，後端 http://127.0.0.1:${selected_backend_port}"
print_status "Backend log: $backend_log"
print_status "Frontend log: $frontend_log"

open "http://127.0.0.1:${FRONTEND_PORT}/index.html?apiHost=http://127.0.0.1:${selected_backend_port}" >/dev/null 2>&1 || true
