#!/usr/bin/env sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
. "$SCRIPT_DIR/common.sh"

ROLLBACK_IMAGE_TAG=${1:-$(read_state PREVIOUS_IMAGE_TAG)}
ROLLBACK_DEPLOY_SHA=${2:-$(read_state PREVIOUS_DEPLOY_SHA)}
FAILED_IMAGE_TAG=$(read_state IMAGE_TAG)
FAILED_DEPLOY_SHA=$(read_state DEPLOY_SHA)

if [ -z "$ROLLBACK_IMAGE_TAG" ] || [ -z "$ROLLBACK_DEPLOY_SHA" ]; then
  echo "没有可用的上一成功版本，无法自动回滚" >&2
  exit 1
fi

validate_ref "$ROLLBACK_IMAGE_TAG" ROLLBACK_IMAGE_TAG 7
validate_ref "$ROLLBACK_DEPLOY_SHA" ROLLBACK_DEPLOY_SHA 40

echo "回滚到上一成功版本：$ROLLBACK_DEPLOY_SHA"
write_state "$ROLLBACK_IMAGE_TAG" "$ROLLBACK_DEPLOY_SHA" "$FAILED_IMAGE_TAG" "$FAILED_DEPLOY_SHA"
compose pull backend frontend
compose up -d --remove-orphans

if "$SCRIPT_DIR/verify.sh"; then
  # 健康验证必须在切换工作树前完成，兼容上一版本尚未包含部署脚本的首次回滚。
  checkout_exact_sha "$ROLLBACK_DEPLOY_SHA"
  echo "自动回滚成功，生产已恢复到 $ROLLBACK_DEPLOY_SHA"
  exit 0
fi

echo "自动回滚失败，需要人工介入" >&2
exit 1
