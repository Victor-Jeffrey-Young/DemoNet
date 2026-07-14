#!/usr/bin/env sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
TEST_ROOT=${TMPDIR:-/tmp}/demonet-deploy-test-$$
mkdir -p "$TEST_ROOT"
trap 'rm -rf "$TEST_ROOT"' EXIT INT TERM

DEPLOY_PATH=$TEST_ROOT
DEPLOY_ENV_FILE=.env.deploy
export DEPLOY_PATH DEPLOY_ENV_FILE
. "$SCRIPT_DIR/common.sh"

assert_equals() {
  expected=$1
  actual=$2
  message=$3
  if [ "$expected" != "$actual" ]; then
    echo "断言失败：$message，期望 '$expected'，实际 '$actual'" >&2
    exit 1
  fi
}

write_state abc1234 abc1234567890abc1234567890abc12345678900 def5678 def5678901234def5678901234def56789012340

assert_equals abc1234 "$(read_state IMAGE_TAG)" "读取当前镜像标签"
assert_equals abc1234567890abc1234567890abc12345678900 "$(read_state DEPLOY_SHA)" "读取当前提交"
assert_equals def5678 "$(read_state PREVIOUS_IMAGE_TAG)" "读取上一镜像标签"
assert_equals def5678901234def5678901234def56789012340 "$(read_state PREVIOUS_DEPLOY_SHA)" "读取上一提交"

permissions=$(stat -c '%a' "$DEPLOY_ENV_FILE" 2>/dev/null || stat -f '%Lp' "$DEPLOY_ENV_FILE")
assert_equals 600 "$permissions" "发布状态文件权限"

validate_ref abc1234 VALID_SHA 7
validate_ref abc1234567890abc1234567890abc12345678900 VALID_FULL_SHA 40
if validate_ref 'latest' INVALID_SHA 7 2>/dev/null; then
  echo "断言失败：latest 不应被接受为不可变版本" >&2
  exit 1
fi

if validate_ref 'ABC1234' UPPERCASE_SHA 7 2>/dev/null; then
  echo "断言失败：大写或非十六进制版本不应被接受" >&2
  exit 1
fi

if validate_ref 'abc12345' WRONG_LENGTH 7 2>/dev/null; then
  echo "断言失败：错误长度的镜像标签不应被接受" >&2
  exit 1
fi

echo "部署状态脚本测试通过"
