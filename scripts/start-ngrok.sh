#!/usr/bin/env zsh
set -euo pipefail

ROOT_DIR="/Volumes/資料統整區/Operation-Survival"
FRONTEND_PORT="${FRONTEND_PORT:-8000}"
BACKEND_PORT="${BACKEND_PORT:-8080}"
NGROK_API="http://127.0.0.1:4040/api/tunnels"
NGROK_CONFIG_FILE="/tmp/operation-survival-ngrok.yml"
DEFAULT_NGROK_CONFIG="$HOME/Library/Application Support/ngrok/ngrok.yml"

ngrok_log="/tmp/operation-survival-ngrok.log"

print_status() {
  echo "[Operation-Survival:ngrok] $1"
}

if ! command -v ngrok >/dev/null 2>&1; then
  echo "找不到 ngrok，請先安裝後再執行。" >&2
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "找不到 python3，無法解析 ngrok tunnel URL。" >&2
  exit 1
fi

if [[ ! -f "$DEFAULT_NGROK_CONFIG" ]]; then
  echo "找不到 ngrok 預設設定檔：$DEFAULT_NGROK_CONFIG" >&2
  echo "請先執行：ngrok config add-authtoken <YOUR_NGROK_TOKEN>" >&2
  exit 1
fi

ngrok_authtoken=$(awk -F': *' '/authtoken:/{print $2; exit}' "$DEFAULT_NGROK_CONFIG" | tr -d '"' | tr -d "'")
if [[ -z "$ngrok_authtoken" ]]; then
  echo "ngrok 設定檔內沒有 authtoken，請先執行：ngrok config add-authtoken <YOUR_NGROK_TOKEN>" >&2
  exit 1
fi

print_status "先啟動正式版服務（prod profile）"
"$ROOT_DIR/scripts/start-prod.sh"

print_status "停止既有 ngrok 程序（若存在）"
pkill -f "ngrok http" >/dev/null 2>&1 || true
pkill -f "ngrok start --all" >/dev/null 2>&1 || true

print_status "建立 ngrok 設定檔（frontend + backend）"
cat > "$NGROK_CONFIG_FILE" <<EOF
version: "2"
authtoken: ${ngrok_authtoken}
tunnels:
  frontend:
    proto: http
    addr: ${FRONTEND_PORT}
  backend:
    proto: http
    addr: ${BACKEND_PORT}
EOF

print_status "啟動 ngrok tunnels"
ngrok start --all --config "$NGROK_CONFIG_FILE" --log=stdout >"$ngrok_log" 2>&1 &

sleep 3

urls=$(curl --silent "$NGROK_API" | FRONTEND_PORT="${FRONTEND_PORT}" BACKEND_PORT="${BACKEND_PORT}" python3 -c '
import json
import os
import sys

raw = sys.stdin.read().strip()
if not raw:
  print("")
  print("")
  raise SystemExit(0)

data = json.loads(raw)
tunnels = data.get("tunnels", [])
frontend_port = os.getenv("FRONTEND_PORT", "8000")
backend_port = os.getenv("BACKEND_PORT", "8080")

frontend = ""
backend = ""
for t in tunnels:
  public = t.get("public_url", "")
  cfg = t.get("config", {})
  name = str(t.get("name", ""))
  addr = str(cfg.get("addr", ""))
  if name == "frontend" and public and not frontend:
    frontend = public
  if name == "backend" and public and not backend:
    backend = public
  if addr.endswith(f":{frontend_port}") and not frontend:
    frontend = public
  if addr.endswith(f":{backend_port}") and not backend:
    backend = public

print(frontend)
print(backend)
')

frontend_url=$(printf '%s\n' "$urls" | sed -n '1p')
backend_url=$(printf '%s\n' "$urls" | sed -n '2p')

if [[ -z "$frontend_url" || -z "$backend_url" ]]; then
  if grep -q "ERR_NGROK_4018" "$ngrok_log" 2>/dev/null; then
    print_status "ngrok 尚未設定 authtoken（ERR_NGROK_4018）"
    print_status "請先執行：ngrok config add-authtoken <YOUR_NGROK_TOKEN>"
  fi
  print_status "無法自動取得 ngrok URL，請檢查 ${ngrok_log}"
  print_status "你也可以開啟 http://127.0.0.1:4040 直接查看 tunnel URL"
  exit 1
fi

play_url="${frontend_url}/index.html?apiHost=${backend_url}"

print_status "Frontend URL: ${frontend_url}"
print_status "Backend URL: ${backend_url}"
print_status "Play URL: ${play_url}"
print_status "ngrok log: ${ngrok_log}"

open "$play_url" >/dev/null 2>&1 || true