#!/bin/sh
set -e

cd "$(dirname "$0")/.."

chmod +x ./gradlew 2>/dev/null || true
sh ./gradlew :fabric:shadowJar --no-daemon

cd docker
docker compose down -v
docker compose build

docker compose run --rm test-ubuntu
docker compose run --rm test-debian
docker compose run --rm test-alpine
docker compose run --rm test-rhel
docker compose run --rm test-windows
docker compose run --rm test-macos

docker compose down -v