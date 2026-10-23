#!/usr/bin/env bash
# =============================================================================
#  Смоук-тест стенда: одна команда — все проверки с понятным вердиктом.
#
#      bash scripts/smoke-test.sh                 # стенд на 127.0.0.1:8080
#      bash scripts/smoke-test.sh http://host:port
#
#  Проверяет то же, что важно в проде: контейнеры живы, nginx отвечает,
#  приложение внутри поднялось, схема БД на месте, API пишет и читает данные,
#  WebSocket проходит через reverse proxy. Возвращает ненулевой код, если
#  хоть одна проверка упала (удобно в CI или после деплоя).
# =============================================================================
set -uo pipefail

BASE_URL="${1:-${BASE_URL:-http://127.0.0.1:8080}}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="${APP_DIR:-$(cd "$SCRIPT_DIR/.." && pwd)}"
SMOKE_USER="smoke_$(date +%s)"
SMOKE_PASSWORD="smoke-password-123"
PASS=0
FAIL=0

ok()   { printf '  \033[1;32m✓\033[0m %s\n' "$*"; PASS=$((PASS + 1)); }
bad()  { printf '  \033[1;31m✗\033[0m %s\n' "$*"; FAIL=$((FAIL + 1)); }
step() { printf '\n\033[1;34m%s\033[0m\n' "$*"; }

have_compose() { command -v docker >/dev/null 2>&1 && (cd "$APP_DIR" && docker compose version >/dev/null 2>&1); }

step "0. Куда смотрим"
printf '  стенд: %s\n  каталог: %s\n' "$BASE_URL" "$APP_DIR"

step "1. Контейнеры стека (docker compose ps)"
if have_compose; then
  while read -r name status; do
    case "$status" in
      *healthy*) ok "$name — $status" ;;
      *)         bad "$name — $status" ;;
    esac
  done < <(cd "$APP_DIR" && docker compose ps --format '{{.Name}} {{.Status}}')
else
  bad "docker compose недоступен (нужна группа docker) — проверку контейнеров пропускаю"
fi

step "2. nginx отвечает"
code=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 15 "$BASE_URL/healthz" 2>/dev/null)
[ "$code" = "200" ] && ok "/healthz → 200" || bad "/healthz → ${code:-нет ответа}"

step "3. Приложение за nginx (actuator)"
body=$(curl -sS --max-time 15 "$BASE_URL/actuator/health" 2>/dev/null)
grep -q '"status":"UP"' <<<"$body" && ok "/actuator/health → UP" || bad "/actuator/health → ${body:-нет ответа}"

step "4. API + БД: регистрация → логин → статистика"
code=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 15 -X POST "$BASE_URL/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$SMOKE_USER\",\"email\":\"$SMOKE_USER@example.com\",\"password\":\"$SMOKE_PASSWORD\"}" 2>/dev/null)
[ "$code" = "200" ] && ok "register → 200 (пользователь $SMOKE_USER)" || bad "register → ${code:-нет ответа}"

login=$(curl -sS --max-time 15 -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$SMOKE_USER\",\"password\":\"$SMOKE_PASSWORD\"}" 2>/dev/null)
uid=$(sed -n 's/.*"user_id":\([0-9]*\).*/\1/p' <<<"$login")
[ -n "$uid" ] && ok "login → user_id=$uid" || bad "login → ${login:-нет ответа}"

if [ -n "$uid" ]; then
  stats=$(curl -sS --max-time 15 "$BASE_URL/api/matches/player/$uid/stats" 2>/dev/null)
  grep -q '"rating"' <<<"$stats" && ok "stats → $stats" || bad "stats → ${stats:-нет ответа}"
fi

step "5. WebSocket через nginx (ожидаем 101 Switching Protocols)"
ws=$(curl -sS -i -N --max-time 5 \
  -H 'Connection: Upgrade' -H 'Upgrade: websocket' \
  -H 'Sec-WebSocket-Version: 13' -H 'Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==' \
  "$BASE_URL/chess" 2>/dev/null | head -1)
grep -q "101" <<<"$ws" && ok "WS /chess → ${ws:-нет ответа}" || bad "WS /chess → ${ws:-нет ответа}"

step "6. Swagger UI"
code=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 15 "$BASE_URL/swagger-ui/index.html" 2>/dev/null)
[ "$code" = "200" ] && ok "/swagger-ui/index.html → 200" || bad "/swagger-ui/index.html → ${code:-нет ответа}"

step "7. Схема БД (таблицы в schema chess)"
if have_compose; then
  tables=$(cd "$APP_DIR" && docker compose exec -T db sh -c \
    'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc "select count(*) from information_schema.tables where table_schema = '"'"'chess'"'"'"' 2>/dev/null | tr -d '[:space:]')
  [ "${tables:-0}" = "5" ] && ok "таблиц в схеме chess: 5 (users, user_profile, rooms, matches, match_moves)" \
                           || bad "таблиц в схеме chess: ${tables:-0} (ожидалось 5)"
fi

printf '\n--------------------------------------------------\n'
printf 'ИТОГ: пройдено %d, провалено %d\n' "$PASS" "$FAIL"
[ "$FAIL" -eq 0 ] || exit 1
printf '\033[1;32mСтенд в порядке.\033[0m\n'
