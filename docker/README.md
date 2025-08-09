# Docker 部署文件夹

本文件夹包含了ECC Server Demo项目的所有Docker相关文件。

## 📁 文件说明

| 文件名 | 说明 |
|--------|------|
| `Dockerfile` | Docker镜像构建文件 |
| `docker-compose.yml` | 开发环境编排文件（包含MySQL、Redis、RabbitMQ） |
| `docker-compose.prod.yml` | 生产环境编排文件（仅应用服务） |
| `docker-compose.app-only.yml` | 仅应用部署文件（连接外部数据库） |
| `.dockerignore` | Docker构建时忽略的文件列表 |
| `.env.example` | 环境变量配置模板 |
| `.env.app-only.example` | 仅应用部署环境变量模板 |
| `build-and-deploy.bat` | Windows自动化构建部署脚本 |
| `build-and-deploy.sh` | Linux/Mac自动化构建部署脚本 |
| `build-and-deploy-app-only.bat` | Windows仅应用部署脚本 |
| `build-and-deploy-app-only.sh` | Linux/Mac仅应用部署脚本 |
| `DOCKER_DEPLOYMENT.md` | 详细的Docker部署指南 |

## 🚀 快速开始

### 完整环境部署（包含MySQL、Redis等）

#### Windows用户
```cmd
# 进入项目根目录
cd /d E:\project\Ecc-Server-Demo

# 运行自动化脚本
docker\build-and-deploy.bat
```

#### Linux/Mac用户
```bash
# 进入项目根目录
cd /path/to/Ecc-Server-Demo

# 给脚本执行权限
chmod +x docker/build-and-deploy.sh

# 运行自动化脚本
./docker/build-and-deploy.sh
```

### 仅应用部署（连接外部数据库）

#### Windows用户
```cmd
# 进入项目根目录
cd /d E:\project\Ecc-Server-Demo

# 运行仅应用部署脚本
docker\build-and-deploy-app-only.bat
```

#### Linux/Mac用户
```bash
# 进入项目根目录
cd /path/to/Ecc-Server-Demo

# 给脚本执行权限
chmod +x docker/build-and-deploy-app-only.sh

# 运行仅应用部署脚本
./docker/build-and-deploy-app-only.sh
```

## 📖 详细文档

请查看 [`DOCKER_DEPLOYMENT.md`](./DOCKER_DEPLOYMENT.md) 获取完整的部署指南。

## ⚠️ 注意事项

1. **路径问题**: 所有Docker命令都需要在项目根目录下执行
2. **环境配置**: 生产环境部署前请复制并修改 `.env.example` 文件
3. **端口冲突**: 确保8089、3306、6379、5672等端口未被占用
4. **权限问题**: Linux/Mac用户需要给shell脚本执行权限

## 🔧 常用命令

### 完整环境部署
```bash
# 开发环境部署
docker-compose -f docker/docker-compose.yml up -d

# 生产环境部署
docker-compose -f docker/docker-compose.prod.yml up -d

# 停止服务
docker-compose -f docker/docker-compose.yml down

# 查看日志
docker-compose -f docker/docker-compose.yml logs -f ecc-app

# 重新构建
docker-compose -f docker/docker-compose.yml up -d --build
```

### 仅应用部署
```bash
# 仅应用部署（需要.env文件）
docker-compose -f docker/docker-compose.app-only.yml --env-file docker/.env up -d

# 停止应用
docker-compose -f docker/docker-compose.app-only.yml down

# 查看应用日志
docker-compose -f docker/docker-compose.app-only.yml logs -f ecc-app

# 重启应用
docker-compose -f docker/docker-compose.app-only.yml restart ecc-app

# 重新构建应用
docker-compose -f docker/docker-compose.app-only.yml up -d --build
```