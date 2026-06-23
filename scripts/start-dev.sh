#!/usr/bin/env bash
# =============================================================================
# DemoNet 一键启动开发环境
# =============================================================================
# 用法：bash scripts/start-dev.sh
#
# 自动完成：
#   1. 加载 .env 环境变量
#   2. 启动 Docker 基础设施（MySQL / Redis / RabbitMQ）
#   3. 启动 Spring Boot 后端（:8080）
#   4. 启动 Vite 前端（:5173）
#
# Ctrl+C 停止所有服务。
# =============================================================================
set -e

cd "$(dirname "$0")/.."

# ---- 检查 .env ----
if [ ! -f .env ]; then
  echo "❌ 未找到 .env 文件。请先运行：bash scripts/setup-env.sh"
  exit 1
fi

# ---- 加载环境变量 ----
echo "📦 加载 .env 环境变量..."
set -a
source .env
set +a

# ---- 启动 Docker 服务 ----
echo "🐳 启动 Docker 基础设施..."
docker compose up -d

# ---- 等待 MySQL 就绪 ----
echo "⏳ 等待 MySQL 就绪..."
until docker exec demonet-mysql mysqladmin ping -uroot -p"${MYSQL_ROOT_PASSWORD:-changeme}" --silent 2>/dev/null; do
  sleep 1
done
echo "✅ MySQL 已就绪"

# ---- 清理函数 ----
cleanup() {
  echo ""
  echo "🛑 正在停止服务..."
  kill $BACKEND_PID 2>/dev/null || true
  kill $FRONTEND_PID 2>/dev/null || true
  echo "✅ 已停止。Docker 容器仍在后台运行。"
  echo "   停止 Docker：docker compose down"
  exit 0
}
trap cleanup INT TERM

# ---- 启动后端 ----
echo "🔧 启动后端（Spring Boot :8080）..."
cd backend
mvn spring-boot:run -q &
BACKEND_PID=$!
cd ..

# ---- 启动前端 ----
echo "🎨 启动前端（Vite :5173）..."
cd frontend
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  DemoNet 开发环境已启动"
echo "  🌐 前端: http://localhost:5173"
echo "  🔌 后端: http://localhost:8080"
echo "  📨 RabbitMQ: http://localhost:15672"
echo ""
echo "  按 Ctrl+C 停止"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

wait
