#!/usr/bin/env bash
# =============================================================================
# DemoNet 环境初始化脚本
# =============================================================================
# 用法：bash scripts/setup-env.sh
#
# 首次克隆项目后运行一次，自动生成 .env 文件并填入随机安全值。
# .env 已被 .gitignore 忽略，不会被提交到 Git。
# =============================================================================
set -e

cd "$(dirname "$0")/.."

if [ -f .env ]; then
  echo "⏭️  .env 已存在，跳过初始化。"
  echo "   如需重新生成，请先删除 .env：rm .env"
  exit 0
fi

echo "🔧 正在生成 .env 文件..."

# 生成随机 JWT 密钥（256 位以上）
JWT_KEY=$(openssl rand -base64 64)

cp .env.example .env

# 替换 JWT 密钥
if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -i '' "s|JWT_SECRET=changeme|JWT_SECRET=$JWT_KEY|" .env
else
  sed -i "s|JWT_SECRET=changeme|JWT_SECRET=$JWT_KEY|" .env
fi

echo ""
echo "✅ .env 已生成。"
echo ""
echo "⚠️  还需要手动填写以下内容："
echo "   - TMDB_API_KEY      从 https://www.themoviedb.org/settings/api 获取"
echo "   - STEAM_API_KEY     从 https://steamcommunity.com/dev/apikey 获取（可选）"
echo "   - MYSQL_ROOT_PASSWORD  数据库密码（默认 changeme 仅限本地开发）"
echo "   - RABBITMQ_PASS        RabbitMQ 密码（默认 changeme 仅限本地开发）"
echo ""
echo "编辑 .env 后运行：bash scripts/start-dev.sh"
