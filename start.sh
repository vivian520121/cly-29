#!/bin/bash

echo "========================================"
echo " 企业级项目协作管理平台 - 一键启动脚本"
echo "========================================"
echo ""

echo "[1/4] 启动 MySQL 和 Redis..."
docker-compose up -d mysql redis

echo ""
echo "[2/4] 等待 MySQL 初始化完成（约30秒）..."
sleep 30

echo ""
echo "[3/4] 启动后端服务..."
docker-compose up -d backend

echo ""
echo "[4/4] 启动前端服务..."
docker-compose up -d frontend

echo ""
echo "========================================"
echo " 启动完成！"
echo "========================================"
echo ""
echo "访问地址："
echo "  - 前端: http://localhost"
echo "  - 后端API: http://localhost:8080/api"
echo "  - 接口文档: http://localhost:8080/doc.html"
echo ""
echo "默认账号："
echo "  - 管理员: admin / admin123"
echo "  - 普通用户: user / user123"
echo ""
echo "查看日志: docker-compose logs -f [服务名]"
echo "停止服务: docker-compose down"
