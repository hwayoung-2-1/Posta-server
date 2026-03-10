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

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
cd "$ROOT_DIR"

docker compose -f docker/docker-compose.dev.yml down -v --remove-orphans --rmi local || true
docker compose -f docker/docker-compose.prod.yml down -v --remove-orphans --rmi local || true

chmod +x ./gradlew

if ./gradlew clean; then
  ok "Gradle clean completed."
else
  err "Gradle clean failed."
  exit 1
fi