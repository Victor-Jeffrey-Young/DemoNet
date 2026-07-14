#!/usr/bin/env sh

set -eu

DEPLOY_PATH=${DEPLOY_PATH:?DEPLOY_PATH 未设置}
COMPOSE_FILE=${COMPOSE_FILE:-docker-compose.ci.yml}
BASE_ENV_FILE=${BASE_ENV_FILE:-.env}
DEPLOY_ENV_FILE=${DEPLOY_ENV_FILE:-.env.deploy}

cd "$DEPLOY_PATH"

compose() {
  docker compose --env-file "$BASE_ENV_FILE" --env-file "$DEPLOY_ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

read_state() {
  key=$1
  if [ ! -f "$DEPLOY_ENV_FILE" ]; then
    return 0
  fi
  grep -m1 "^${key}=" "$DEPLOY_ENV_FILE" | sed "s/^${key}=//"
}

# 首次在新服务器上部署：尚不存在 .env.deploy 时，由调用方（rollback.sh、CI 等）
# 通过 write_state 写入初始状态前，提供一个占位初始状态，避免 read_state 空串
# 流入 validate_ref 触发长度校验失败。
bootstrap_first_deploy() {
  if [ -f "$DEPLOY_ENV_FILE" ]; then
    return 0
  fi
  echo "首次部署：生成 $DEPLOY_ENV_FILE 初始占位状态回滚锚点。" >&2
  write_state "" "" "" ""
}

validate_ref() {
  value=$1
  name=$2
  expected_length=$3
  case "$value" in
    ''|*[!0-9a-f]*)
      echo "$name 必须是小写十六进制 Git SHA" >&2
      return 1
      ;;
  esac
  if [ "${#value}" -ne "$expected_length" ]; then
    echo "$name 长度必须为 $expected_length，实际为 ${#value}" >&2
    return 1
  fi
}

write_state() {
  image_tag=$1
  deploy_sha=$2
  previous_image_tag=${3:-}
  previous_deploy_sha=${4:-}
  tmp_file="${DEPLOY_ENV_FILE}.tmp.$$"

  umask 077
  {
    printf 'IMAGE_TAG=%s\n' "$image_tag"
    printf 'DEPLOY_SHA=%s\n' "$deploy_sha"
    printf 'PREVIOUS_IMAGE_TAG=%s\n' "$previous_image_tag"
    printf 'PREVIOUS_DEPLOY_SHA=%s\n' "$previous_deploy_sha"
  } > "$tmp_file"
  mv "$tmp_file" "$DEPLOY_ENV_FILE"
}

checkout_exact_sha() {
  deploy_sha=$1
  git fetch --quiet origin "$deploy_sha"
  git cat-file -e "${deploy_sha}^{commit}"
  git checkout --quiet --detach "$deploy_sha"
}
