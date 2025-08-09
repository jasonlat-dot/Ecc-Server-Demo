# 使用官方的OpenJDK 8运行时作为基础镜像
FROM openjdk:8-jre-alpine

# 安装su-exec工具用于安全的用户切换
RUN apk add --no-cache su-exec

# 设置工作目录
WORKDIR /app

# 创建应用用户（安全最佳实践）
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 复制构建好的JAR文件到容器中
COPY target/CeditWarning.jar app.jar

# 创建日志目录并设置权限
RUN mkdir -p /app/data/log && \
    chmod -R 755 /app/data && \
    chown -R appuser:appgroup /app

# 切换到应用用户
USER appuser

# 确保日志目录在容器启动时有正确的权限
# 创建一个启动脚本来处理权限问题
COPY docker/entrypoint.sh /entrypoint.sh
USER root
RUN chmod +x /entrypoint.sh
USER appuser

# 暴露应用端口（根据配置文件，开发环境是8090，生产环境是8089）
EXPOSE 8089 8090

# 使用自定义启动脚本
ENTRYPOINT ["/entrypoint.sh"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8089}/api/v1/actuator/health || exit 1