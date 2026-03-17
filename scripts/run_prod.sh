#!/usr/bin/env sh
set -eu

COLOR_RED="$(printf '\033[0;31m')"
COLOR_GREEN="$(printf '\033[0;32m')"
COLOR_RESET="$(printf '\033[0m')"

ok() {
  printf '%s[OK]%s %s\n' "$COLOR_GREEN" "$COLOR_RESET" "$1"
}

err() {
  printf '%s[ERROR]%s %s\n' "$COLOR_RED" "$COLOR_RESET" "$1" >&2
}

stop_container_on_port() {
  PORT="$1"

  CONTAINER_IDS="$(docker ps --filter "publish=${PORT}" --format "{{.ID}}" || true)"

  if [ -n "$CONTAINER_IDS" ]; then
    for CID in $CONTAINER_IDS; do
      NAME="$(docker inspect --format='{{.Name}}' "$CID" | sed 's#/##')"
      printf '%s[INFO]%s Stopping container %s using port %s\n' "$COLOR_RED" "$COLOR_RESET" "$NAME" "$PORT"
      docker stop "$CID"
    done
  fi
}

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
cd "$ROOT_DIR"

ENV_FILE="${ROOT_DIR}/.env.prod"

[ -f "${ENV_FILE}" ] || {
  err "${ENV_FILE} not found. Create it from members"
  exit 1
}

ok "Loaded env file: ${ENV_FILE}"

set -a
. "${ENV_FILE}"
set +a

stop_container_on_port "${APP_PORT}"
stop_container_on_port "${POSTGRES_PORT}"
stop_container_on_port "${REDIS_PORT}"

if docker compose --env-file "${ENV_FILE}" -f docker/docker-compose.prod.yml up -d --build; then
  ok "Production environment started."
else
  err "Failed to start production environment."
  exit 1
fi