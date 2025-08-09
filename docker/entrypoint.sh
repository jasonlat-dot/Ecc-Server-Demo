#!/bin/sh

# Docker容器启动脚本
# 作者: AI Assistant
# 描述: 处理容器启动时的权限问题，确保日志目录可写

set -e

echo "[INFO] 容器启动中..."
echo "[INFO] 当前用户: $(whoami)"
echo "[INFO] 当前工作目录: $(pwd)"

# 检查并创建日志目录
if [ ! -d "/app/data/log" ]; then
    echo "[INFO] 创建日志目录..."
    mkdir -p /app/data/log
fi

# 检查日志目录权限
echo "[INFO] 检查日志目录权限..."
ls -la /app/data/

# 如果是root用户启动，需要修复权限后切换用户
if [ "$(id -u)" = "0" ]; then
    echo "[INFO] 以root用户启动，修复权限后切换到appuser..."
    
    # 确保目录存在
    mkdir -p /app/data/log
    
    # 设置正确的权限
    chown -R appuser:appgroup /app/data
    chmod -R 755 /app/data
    
    echo "[INFO] 权限修复完成，切换到appuser用户..."
    
    # 切换到appuser用户并执行Java应用
    exec su-exec appuser java \
        "-Djasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD:-lijiaqiang1024@wt1314520}" \
        "-Xms512m" \
        "-Xmx1024m" \
        "-XX:+UseG1GC" \
        "-XX:+PrintGCDetails" \
        "-XX:+PrintGCTimeStamps" \
        "-Xloggc:/app/data/log/gc.log" \
        "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}" \
        -jar app.jar
else
    echo "[INFO] 以非root用户启动，直接启动应用..."
    
    # 检查是否有写权限
    if [ ! -w "/app/data/log" ]; then
        echo "[ERROR] 日志目录没有写权限，请检查Docker卷挂载配置"
        echo "[ERROR] 当前目录权限:"
        ls -la /app/data/
        echo "[ERROR] 建议在docker-compose.yml中添加用户映射或以root用户启动容器"
        exit 1
    fi
    
    # 直接执行Java应用
    exec java \
        "-Djasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD:-lijiaqiang1024@wt1314520}" \
        "-Xms512m" \
        "-Xmx1024m" \
        "-XX:+UseG1GC" \
        "-XX:+PrintGCDetails" \
        "-XX:+PrintGCTimeStamps" \
        "-Xloggc:/app/data/log/gc.log" \
        "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}" \
        -jar app.jar
fi