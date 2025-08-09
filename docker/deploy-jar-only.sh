#!/bin/bash

# 部署已有JAR包的脚本
# 作者: AI Assistant
# 描述: 此脚本用于部署已经构建好的JAR包，跳过Maven构建步骤

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 脚本目录和项目根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    ECC Server JAR包部署脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}[信息] 脚本目录: $SCRIPT_DIR${NC}"
echo -e "${GREEN}[信息] 项目根目录: $PROJECT_ROOT${NC}"

# 检查操作系统类型
check_os() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "linux"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        echo "macos"
    elif [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
        echo "windows"
    else
        echo "unknown"
    fi
}

# 检查Docker是否安装
check_docker() {
    echo -e "${GREEN}[信息] 检查Docker安装状态...${NC}"
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}[错误] Docker未安装，请先安装Docker${NC}"
        echo -e "${YELLOW}[提示] 请访问 https://docs.docker.com/get-docker/ 下载安装${NC}"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        echo -e "${RED}[错误] Docker服务未启动，请启动Docker服务${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}[完成] Docker已安装并运行${NC}"
}

# 检查docker-compose是否安装
check_docker_compose() {
    echo -e "${GREEN}[信息] 检查docker-compose安装状态...${NC}"
    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}[错误] docker-compose未安装，请先安装docker-compose${NC}"
        echo -e "${YELLOW}[提示] 请访问 https://docs.docker.com/compose/install/ 下载安装${NC}"
        exit 1
    fi
    echo -e "${GREEN}[完成] docker-compose已安装${NC}"
}

# 检查JAR文件是否存在
check_jar_file() {
    echo -e "${GREEN}[信息] 检查JAR文件...${NC}"
    
    # 切换到项目根目录
    cd "$PROJECT_ROOT" || {
        echo -e "${RED}[错误] 无法切换到项目根目录: $PROJECT_ROOT${NC}"
        exit 1
    }
    
    # 查找target目录下的JAR文件
    if [ ! -d "target" ]; then
        echo -e "${RED}[错误] target目录不存在，请先构建项目${NC}"
        echo -e "${YELLOW}[提示] 请运行 mvn clean package -DskipTests 构建项目${NC}"
        exit 1
    fi
    
    JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" | head -1)
    
    if [ -z "$JAR_FILE" ]; then
        echo -e "${RED}[错误] 在target目录中未找到JAR文件${NC}"
        echo -e "${YELLOW}[提示] 请先构建项目生成JAR文件${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}[完成] 找到JAR文件: $JAR_FILE${NC}"
}

# 构建Docker镜像
build_docker_image() {
    echo -e "${GREEN}[信息] 构建Docker镜像...${NC}"
    
    # 切换到docker目录
    cd "$SCRIPT_DIR" || {
        echo -e "${RED}[错误] 无法切换到docker目录${NC}"
        exit 1
    }
    
    # 构建Docker镜像
    if docker build -t ecc-server:latest -f Dockerfile ..; then
        echo -e "${GREEN}[完成] Docker镜像构建成功${NC}"
    else
        echo -e "${RED}[错误] Docker镜像构建失败${NC}"
        exit 1
    fi
}

# 停止并删除现有容器
stop_existing_containers() {
    echo -e "${GREEN}[信息] 停止并删除现有容器...${NC}"
    
    # 停止现有容器
    if docker ps -q --filter "name=ecc-server" | grep -q .; then
        echo -e "${YELLOW}[信息] 停止现有的ecc-server容器...${NC}"
        docker stop ecc-server
    fi
    
    # 删除现有容器
    if docker ps -aq --filter "name=ecc-server" | grep -q .; then
        echo -e "${YELLOW}[信息] 删除现有的ecc-server容器...${NC}"
        docker rm ecc-server
    fi
    
    echo -e "${GREEN}[完成] 容器清理完成${NC}"
}

# 选择部署方式
choose_deployment_method() {
    echo -e "${BLUE}[选择] 请选择部署方式:${NC}"
    echo -e "${YELLOW}1) 仅应用 (app-only)${NC}"
    echo -e "${YELLOW}2) 完整部署 (包含数据库)${NC}"
    echo -e "${YELLOW}3) 生产环境部署${NC}"
    
    read -p "请输入选择 (1-3): " choice
    
    case $choice in
        1)
            deploy_app_only
            ;;
        2)
            deploy_full
            ;;
        3)
            deploy_production
            ;;
        *)
            echo -e "${RED}[错误] 无效选择，请输入1-3${NC}"
            choose_deployment_method
            ;;
    esac
}

# 仅应用部署
deploy_app_only() {
    echo -e "${GREEN}[信息] 开始仅应用部署...${NC}"
    
    # 检查环境变量文件
    if [ ! -f ".env.app-only.example" ]; then
        echo -e "${RED}[错误] 环境变量示例文件 .env.app-only.example 不存在${NC}"
        exit 1
    fi
    
    if [ ! -f ".env.app-only" ]; then
        echo -e "${YELLOW}[警告] .env.app-only 文件不存在，从示例文件复制...${NC}"
        cp .env.app-only.example .env.app-only
        echo -e "${YELLOW}[提示] 请编辑 .env.app-only 文件配置相关参数${NC}"
    fi
    
    # 确保日志目录存在并有正确权限
    echo -e "${GREEN}[信息] 准备日志目录...${NC}"
    mkdir -p "$PROJECT_ROOT/data/log"
    
    # 使用docker-compose部署
    if docker-compose -f docker-compose.app-only.yml --env-file .env.app-only up -d; then
        echo -e "${GREEN}[完成] 应用部署成功！${NC}"
        echo -e "${BLUE}[信息] 应用访问地址: http://localhost:8089${NC}"
        echo -e "${YELLOW}[提示] 如果遇到权限问题，容器会自动修复日志目录权限${NC}"
    else
        echo -e "${RED}[错误] 应用部署失败${NC}"
        exit 1
    fi
}

# 完整部署
deploy_full() {
    echo -e "${GREEN}[信息] 开始完整部署...${NC}"
    
    # 检查环境变量文件
    if [ ! -f ".env.example" ]; then
        echo -e "${RED}[错误] 环境变量示例文件 .env.example 不存在${NC}"
        exit 1
    fi
    
    if [ ! -f ".env" ]; then
        echo -e "${YELLOW}[警告] .env 文件不存在，从示例文件复制...${NC}"
        cp .env.example .env
        echo -e "${YELLOW}[提示] 请编辑 .env 文件配置相关参数${NC}"
    fi
    
    # 使用docker-compose部署
    if docker-compose --env-file .env up -d; then
        echo -e "${GREEN}[完成] 完整部署成功！${NC}"
        echo -e "${BLUE}[信息] 应用访问地址: http://localhost:8080${NC}"
        echo -e "${BLUE}[信息] 数据库访问地址: localhost:3306${NC}"
    else
        echo -e "${RED}[错误] 完整部署失败${NC}"
        exit 1
    fi
}

# 生产环境部署
deploy_production() {
    echo -e "${GREEN}[信息] 开始生产环境部署...${NC}"
    
    # 检查生产环境配置文件
    if [ ! -f "docker-compose.prod.yml" ]; then
        echo -e "${RED}[错误] 生产环境配置文件 docker-compose.prod.yml 不存在${NC}"
        exit 1
    fi
    
    # 使用生产环境配置部署
    if docker-compose -f docker-compose.prod.yml up -d; then
        echo -e "${GREEN}[完成] 生产环境部署成功！${NC}"
    else
        echo -e "${RED}[错误] 生产环境部署失败${NC}"
        exit 1
    fi
}

# 主函数
main() {
    echo -e "${GREEN}[开始] JAR包部署流程启动...${NC}"
    
    # 检查系统环境
    OS_TYPE=$(check_os)
    echo -e "${GREEN}[信息] 检测到操作系统: $OS_TYPE${NC}"
    
    # 检查必要工具
    check_docker
    check_docker_compose
    
    # 检查JAR文件
    check_jar_file
    
    # 构建Docker镜像
    build_docker_image
    
    # 停止现有容器
    stop_existing_containers
    
    # 选择部署方式
    choose_deployment_method
    
    echo -e "${GREEN}[完成] 部署流程完成！${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# 执行主函数
main "$@"