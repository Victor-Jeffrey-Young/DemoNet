#!/usr/bin/env sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
. "$SCRIPT_DIR/common.sh"

VERIFY_ATTEMPTS=${VERIFY_ATTEMPTS:-18}
VERIFY_INTERVAL_SECONDS=${VERIFY_INTERVAL_SECONDS:-5}

diagnostics() {
  echo "部署验证失败，输出有限诊断信息：" >&2
  compose ps >&2 || true
  compose logs --tail 80 backend frontend >&2 || true
}

container_is_healthy() {
  container=$1
  [ "$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}missing{{end}}' "$container" 2>/dev/null || true)" = "healthy" ]
}

smoke_test() {
  docker exec demonet-backend wget -q -O /dev/null http://localhost:8080/actuator/health/readiness &&
    docker exec demonet-frontend wget -q -O /dev/null http://localhost/ &&
    docker exec demonet-frontend wget -q -O /dev/null http://localhost/api/categories
}

attempt=1
while [ "$attempt" -le "$VERIFY_ATTEMPTS" ]; do
  if container_is_healthy demonet-backend &&
     container_is_healthy demonet-frontend &&
     smoke_test; then
    echo "部署验证成功（第 ${attempt} 次检查）"
    exit 0
  fi

  echo "等待服务就绪：${attempt}/${VERIFY_ATTEMPTS}"
  attempt=$((attempt + 1))
  sleep "$VERIFY_INTERVAL_SECONDS"
done

diagnostics
exit 1

