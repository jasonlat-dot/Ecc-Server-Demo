# ECC Server Demo Docker 详细部署指南

本文档详细说明如何使用Docker部署ECC Server Demo项目。

## 📋 目录

- [前置要求](#前置要求)
- [快速开始](#快速开始)
- [部署方式](#部署方式)
- [配置说明](#配置说明)
- [常用命令](#常用命令)
- [故障排除](#故障排除)
- [生产环境部署](#生产环境部署)

## 🔧 前置要求

### 必需软件
- **Java 8+** - 用于编译项目
- **Maven 3.6+** - 用于构建项目
- **Docker 20.10+** - 用于容器化部署
- **Docker Compose 1.29+** - 用于多容器编排

### 系统要求
- **内存**: 最少4GB
- **磁盘空间**: 最少2GB可用空间
- **网络**: 需要访问Docker Hub和Maven仓库

## 🚀 快速开始

### 1. 克隆项目（如果还没有）
```bash
git clone <your-repository-url>
cd Ecc-Server-Demo
```

### 2. 使用自动化脚本部署

#### Windows用户
```cmd
# 运行构建和部署脚本
docker\build-and-deploy.bat
```

#### Linux/Mac用户
```bash
# 给脚本执行权限
chmod +x docker/build-and-deploy.sh

# 运行构建和部署脚本
./docker/build-and-deploy.sh
```

### 3. 手动部署步骤

#### 步骤1: 编译项目
```bash
# 清理并编译项目
mvn clean package -DskipTests
```

#### 步骤2: 构建Docker镜像
```bash
# 构建应用镜像
docker build -f docker/Dockerfile -t ecc-server-demo:latest .
```

#### 步骤3: 启动服务
```bash
# 开发环境（包含数据库）
docker-compose -f docker/docker-compose.yml up -d

# 或生产环境（需要外部数据库）
docker-compose -f docker/docker-compose.prod.yml up -d
```

## 🏗️ 部署方式

### 开发环境部署

开发环境包含完整的技术栈：
- **应用服务器**: Spring Boot应用
- **MySQL 8.0**: 数据库服务
- **Redis 7**: 缓存服务
- **RabbitMQ**: 消息队列服务

```bash
# 启动开发环境
docker-compose -f docker/docker-compose.yml up -d

# 查看服务状态
docker-compose -f docker/docker-compose.yml ps

# 查看日志
docker-compose -f docker/docker-compose.yml logs -f ecc-app
```

**访问地址：**
- 应用API: http://localhost:8089/api/v1
- MySQL: localhost:3306
- Redis: localhost:6379
- RabbitMQ管理界面: http://localhost:15672 (admin/admin123)

### 生产环境部署

生产环境仅部署应用，需要连接外部数据库和服务：

```bash
# 复制环境变量配置文件
cp docker/.env.example docker/.env

# 编辑配置文件，设置生产环境参数
vim docker/.env

# 启动生产环境
docker-compose -f docker/docker-compose.prod.yml up -d
```

## ⚙️ 配置说明

### 环境变量配置

创建 `.env` 文件来配置环境变量：

```bash
# 复制模板文件
cp docker/.env.example docker/.env
```

**重要配置项：**

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `JASYPT_ENCRYPTOR_PASSWORD` | Jasypt加密密码 | `lijiaqiang1024@wt1314520` |
| `SPRING_PROFILES_ACTIVE` | Spring环境配置 | `prod` |
| `DB_HOST` | 数据库主机 | `mysql` |
| `DB_USERNAME` | 数据库用户名 | `ecc_user` |
| `DB_PASSWORD` | 数据库密码 | `ecc_password` |
| `REDIS_HOST` | Redis主机 | `redis` |
| `REDIS_PASSWORD` | Redis密码 | - |

### JVM参数配置

应用启动时会自动设置以下JVM参数：

```bash
-Djasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD}
-Xms512m                    # 初始堆内存
-Xmx1024m                   # 最大堆内存
-XX:+UseG1GC               # 使用G1垃圾收集器
-XX:+PrintGCDetails        # 打印GC详情
-XX:+PrintGCTimeStamps     # 打印GC时间戳
-Xloggc:/app/data/log/gc.log # GC日志文件
```

### 数据库初始化

项目启动时会自动执行 `mysql/ecc.sql` 文件来初始化数据库结构。

## 📝 常用命令

### Docker Compose命令

```bash
# 启动所有服务
docker-compose -f docker/docker-compose.yml up -d

# 停止所有服务
docker-compose -f docker/docker-compose.yml down

# 重启特定服务
docker-compose -f docker/docker-compose.yml restart ecc-app

# 查看服务状态
docker-compose -f docker/docker-compose.yml ps

# 查看服务日志
docker-compose -f docker/docker-compose.yml logs -f ecc-app

# 进入容器
docker-compose -f docker/docker-compose.yml exec ecc-app sh

# 重新构建并启动
docker-compose -f docker/docker-compose.yml up -d --build
```

### Docker命令

```bash
# 查看容器状态
docker ps

# 查看应用日志
docker logs -f ecc-server

# 进入应用容器
docker exec -it ecc-server sh

# 查看容器资源使用情况
docker stats ecc-server

# 删除所有停止的容器
docker container prune

# 删除未使用的镜像
docker image prune
```

### 应用管理命令

```bash
# 查看应用健康状态
curl http://localhost:8089/api/v1/actuator/health

# 查看应用信息
curl http://localhost:8089/api/v1/actuator/info

# 查看JVM内存使用情况
docker exec ecc-server cat /proc/meminfo

# 查看应用进程
docker exec ecc-server ps aux
```

## 🔍 故障排除

### 常见问题

#### 1. 应用启动失败

**症状**: 容器启动后立即退出

**解决方案**:
```bash
# 查看详细错误日志
docker logs ecc-server

# 检查JAR文件是否存在
ls -la target/CeditWarning.jar

# 重新构建项目
mvn clean package -DskipTests
docker-compose up -d --build
```

#### 2. 数据库连接失败

**症状**: 应用日志显示数据库连接错误

**解决方案**:
```bash
# 检查MySQL容器状态
docker-compose -f docker/docker-compose.yml ps mysql

# 查看MySQL日志
docker-compose -f docker/docker-compose.yml logs mysql

# 测试数据库连接
docker-compose -f docker/docker-compose.yml exec mysql mysql -u ecc_user -p test

# 重启MySQL服务
docker-compose -f docker/docker-compose.yml restart mysql
```

#### 3. 端口冲突

**症状**: 端口已被占用错误

**解决方案**:
```bash
# 查看端口占用情况
netstat -tulpn | grep :8089

# 修改docker-compose.yml中的端口映射
# 例如: "8090:8089" 改为 "8091:8089"

# 或者停止占用端口的服务 (Linux/Mac)
sudo lsof -ti:8089 | xargs sudo kill -9

# Windows用户可以使用:
# netstat -ano | findstr :8089
# taskkill /PID <PID> /F
```

#### 4. 内存不足

**症状**: 应用运行缓慢或OOM错误

**解决方案**:
```bash
# 调整JVM内存参数
# 在docker/docker-compose.yml中修改JAVA_OPTS
JAVA_OPTS: "-Xms256m -Xmx512m -XX:+UseG1GC"

# 或者增加Docker容器内存限制
deploy:
  resources:
    limits:
      memory: 1G
```

### 日志查看

```bash
# 应用日志
docker logs -f --tail 100 ecc-server

# 数据库日志
docker logs -f --tail 100 ecc-mysql

# Redis日志
docker logs -f --tail 100 ecc-redis

# 所有服务日志
docker-compose -f docker/docker-compose.yml logs -f

# 应用内部日志文件
docker exec ecc-server tail -f /app/data/log/log_info.log
docker exec ecc-server tail -f /app/data/log/log_error.log
```

## 🏭 生产环境部署

### 生产环境最佳实践

#### 1. 安全配置

```bash
# 使用强密码
export JASYPT_ENCRYPTOR_PASSWORD="your-strong-password-here"

# 使用非root用户运行
# Dockerfile中已配置appuser用户

# 限制容器资源
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 2G
```

#### 2. 数据持久化

```bash
# 使用外部数据卷
volumes:
  - /opt/ecc-server/logs:/app/data/log
  - /opt/ecc-server/config:/app/config
```

#### 3. 监控和健康检查

```bash
# 健康检查配置
healthcheck:
  test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8089/api/v1/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

#### 4. 负载均衡

```bash
# 使用nginx作为反向代理
# 创建nginx配置文件
upstream ecc-backend {
    server localhost:8089;
}

server {
    listen 80;
    server_name your-domain.com;
    
    location /api/ {
        proxy_pass http://ecc-backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 部署检查清单

- [ ] 修改默认密码
- [ ] 配置外部数据库
- [ ] 设置适当的JVM参数
- [ ] 配置日志轮转
- [ ] 设置监控和告警
- [ ] 配置备份策略
- [ ] 测试健康检查端点
- [ ] 验证SSL证书配置
- [ ] 检查防火墙规则
- [ ] 配置域名和DNS

## 📞 支持

如果遇到问题，请：

1. 查看本文档的故障排除部分
2. 检查应用日志和容器状态
3. 确认环境变量配置正确
4. 验证网络连接和端口访问

---

**注意**: 生产环境部署前，请务必修改所有默认密码和敏感配置！