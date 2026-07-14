#!/usr/bin/env sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
. "$SCRIPT_DIR/common.sh"

NEW_IMAGE_TAG=${NEW_IMAGE_TAG:?NEW_IMAGE_TAG 未设置}
NEW_DEPLOY_SHA=${NEW_DEPLOY_SHA:?NEW_DEPLOY_SHA 未设置}

validate_ref "$NEW_IMAGE_TAG" NEW_IMAGE_TAG 7
validate_ref "$NEW_DEPLOY_SHA" NEW_DEPLOY_SHA 40

bootstrap_first_deploy

CURRENT_IMAGE_TAG=$(read_state IMAGE_TAG)
CURRENT_DEPLOY_SHA=$(read_state DEPLOY_SHA)

if [ -z "$CURRENT_IMAGE_TAG" ] || [ -z "$CURRENT_DEPLOY_SHA" ]; then
  echo "首次部署缺少上一成功版本；本次失败时只能报告，不能自动回滚" >&2
fi

echo "部署精确版本：$NEW_DEPLOY_SHA"
checkout_exact_sha "$NEW_DEPLOY_SHA"
write_state "$NEW_IMAGE_TAG" "$NEW_DEPLOY_SHA" "$CURRENT_IMAGE_TAG" "$CURRENT_DEPLOY_SHA"

if compose config --quiet &&
   compose pull backend frontend &&
   compose up -d --remove-orphans &&
   "$SCRIPT_DIR/verify.sh"; then
  echo "部署成功：$NEW_DEPLOY_SHA"
  exit 0
fi

echo "新版本验证失败，尝试自动回滚" >&2
if [ -n "$CURRENT_IMAGE_TAG" ] && [ -n "$CURRENT_DEPLOY_SHA" ] &&
   "$SCRIPT_DIR/rollback.sh" "$CURRENT_IMAGE_TAG" "$CURRENT_DEPLOY_SHA"; then
  echo "新版本发布失败，但生产已恢复到 $CURRENT_DEPLOY_SHA" >&2
  exit 1
fi

echo "发布和自动回滚均失败。请使用上一已知可用 SHA 手工恢复。" >&2
exit 2
