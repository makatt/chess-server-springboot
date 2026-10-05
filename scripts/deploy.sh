#!/usr/bin/env bash
# =============================================================================
#  Деплой образа из GHCR на хост. Запускается по SSH из .github/workflows/cd.yml,
#  но полностью работоспособен и руками:
#
#      APP_DIR=~/apps/chess-server scripts/deploy.sh ghcr.io/makatt/chess-server-springboot:sha-<sha>
#
#  Контракт: скрипт переводит хост на указанный образ и падает с ненулевым кодом,
#  если сервис не поднялся или health-проба не отвечает 200.
# =============================================================================
set -Eeuo pipefail

IMAGE="${1:?usage: deploy.sh <image-ref>}"
APP_DIR="${APP_DIR:-$HOME/apps/chess-server}"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8080/healthz}"
APP_HEALTH_URL="${APP_HEALTH_URL:-http://127.0.0.1:8080/actuator/health}"
TIMEOUT="${TIMEOUT:-180}"

log()  { printf '\033[1;34m[deploy]\033[0m %s\n' "$*"; }
fail() { printf '\033[1;31m[deploy:error]\033[0m %s\n' "$*" >&2; exit 1; }

trap 'fail "обрыв на строке $LINENO"' ERR

command -v docker >/dev/null 2>&1 || fail "docker не найден на хосте"
docker compose version >/dev/null 2>&1 || fail "плагин docker compose не найден"
[[ -d "$APP_DIR/.git" ]] || fail "нет git-копии репозитория в $APP_DIR"
[[ -f "$APP_DIR/.env" ]] || fail "нет $APP_DIR/.env — создайте его из .env.example"

cd "$APP_DIR"

log "обновляю рабочую копию репозитория ($APP_DIR)"
git fetch --prune origin
git reset --hard origin/main

export APP_IMAGE="$IMAGE"
log "тяну образ $IMAGE"
docker compose pull app

log "применяю стек"
docker compose up -d --remove-orphans

log "жду готовности $HEALTH_URL (таймаут ${TIMEOUT}s)"
deadline=$((SECONDS + TIMEOUT))
until curl -fsS "$HEALTH_URL" >/dev/null 2>&1; do
  if (( SECONDS > deadline )); then
    docker compose ps || true
    docker compose logs --tail=80 app || true
    fail "сервис не поднялся за ${TIMEOUT}s"
  fi
  sleep 5
done

log "nginx отдаёт /healthz; проверяю приложение через nginx"
APP_HEALTH="$(curl -fsS "$APP_HEALTH_URL")" || fail "приложение не отвечает через nginx"
log "health: $APP_HEALTH"

docker compose ps
log "деплой завершён: $IMAGE"
