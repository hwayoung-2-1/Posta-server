#!/usr/bin/env sh
set -eu

COLOR_RED="$(printf '\033[0;31m')"
COLOR_GREEN="$(printf '\033[0;32m')"
COLOR_ORANGE="$(printf '\033[0;33m')"

COLOR_RESET="$(printf '\033[0m')"

ok() {
  printf '%s[OK]%s %s\n' "$COLOR_GREEN" "$COLOR_RESET" "$1"
}

err() {
  printf '%s[ERROR]%s %s\n' "$COLOR_RED" "$COLOR_RESET" "$1" >&2
}

validate_ddl_strategy() {
  case "$1" in
    create|create-drop|update|validate|none)
      ;;
    *)
      err "Unsupported DDL strategy: $1"
      err "Supported strategies: create, create-drop, update, validate, none"
      exit 1
      ;;
  esac
}

stop_container_on_port() {
  PORT="$1"

  CONTAINER_IDS="$(docker ps --filter "publish=${PORT}" --format "{{.ID}}" || true)"

  if [ -n "$CONTAINER_IDS" ]; then
    for CID in $CONTAINER_IDS; do
      NAME="$(docker inspect --format='{{.Name}}' "$CID" | sed 's#/##')"
      printf '%s[INFO]%s Stopping container %s using port %s\n' "$COLOR_ORANGE" "$COLOR_RESET" "$NAME" "$PORT"
      docker stop "$CID"
    done
  fi
}

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
cd "$ROOT_DIR"

ENV_FILE="${ROOT_DIR}/.env.dev"

[ -f "${ENV_FILE}" ] || {
  err "${ENV_FILE} not found. Create it from members"
  exit 1
}

ok "Loaded env file: ${ENV_FILE}"

set -a
. "${ENV_FILE}"
set +a

if [ -z "${SPRING_ACTIVE_PROFILES:-}" ]; then
  APP_PROFILE="${SPRING_PROFILE:-dev}"
  DDL_STRATEGY="${1:-${DB_DDL_STRATEGY:-create-drop}}"
  validate_ddl_strategy "${DDL_STRATEGY}"
  SPRING_ACTIVE_PROFILES="${APP_PROFILE},ddl-${DDL_STRATEGY}"
fi

export SPRING_ACTIVE_PROFILES

ok "Using Spring profiles: ${SPRING_ACTIVE_PROFILES}"

stop_container_on_port "${APP_PORT}"
stop_container_on_port "${POSTGRES_PORT}"
stop_container_on_port "${REDIS_PORT}"

if docker compose --env-file "${ENV_FILE}" -f docker/docker-compose.dev.yml up --build; then
  ok "Development environment started."
else
  err "Failed to start development environment."
  exit 1
fi
