#!/bin/bash

# ========================================
# ECC Server Demo 构建和部署脚本
# ========================================

set -e  # 遇到错误立即退出

echo "开始构建 ECC Server Demo..."
echo

# 设置变量
JAR_NAME="CeditWarning.jar"
IMAGE_NAME="ecc-server-demo"
CONTAINER_NAME="ecc-server"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}错误: $1 未安装或未添加到PATH中${NC}"
        exit 1
    fi
}

# 刷新环境变量和命令缓存的函数
refresh_maven_env() {
    # 如果Maven环境变量文件存在，则加载它
    if [ -f "/etc/profile.d/maven.sh" ]; then
        source /etc/profile.d/maven.sh
    fi
    
    # 清除命令缓存
    hash -r
    
    # 确保使用正确的PATH
    if [ -d "/opt/maven/bin" ]; then
        export MAVEN_HOME=/opt/maven
        export PATH=/opt/maven/bin:$PATH
    fi
}

# 检查Maven版本的函数
check_maven_version() {
    local current_version
    local required_version="3.2.5"
    
    # 刷新环境变量
    refresh_maven_env
    
    if command -v mvn &> /dev/null; then
        current_version=$(mvn --version 2>/dev/null | head -n 1 | sed 's/Apache Maven //g' | cut -d' ' -f1)
        echo -e "${GREEN}[信息] 检测到Maven版本: $current_version${NC}"
        echo -e "${GREEN}[信息] Maven路径: $(which mvn)${NC}"
        
        # 简单的版本比较（假设版本格式为x.y.z）
        if [[ "$(printf '%s\n' "$required_version" "$current_version" | sort -V | head -n1)" == "$required_version" ]]; then
            echo -e "${GREEN}[信息] Maven版本满足要求 (>= $required_version)${NC}"
            return 0
        else
            echo -e "${YELLOW}[警告] Maven版本过低 ($current_version < $required_version)${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}[警告] Maven未安装${NC}"
        return 1
    fi
}

# 安装或更新Maven的函数
install_or_update_maven() {
    local action="$1"  # "install" 或 "update"
    
    echo -e "${GREEN}[信息] 开始${action}Maven到最新版本...${NC}"
    
    # 检测操作系统
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux系统 - 手动安装最新版本
        local maven_version="3.9.4"
        local maven_url="https://archive.apache.org/dist/maven/maven-3/${maven_version}/binaries/apache-maven-${maven_version}-bin.tar.gz"
        local install_dir="/opt/maven"
        
        echo -e "${GREEN}[信息] 下载Maven ${maven_version}...${NC}"
        cd /tmp
        wget -q "$maven_url" -O "apache-maven-${maven_version}-bin.tar.gz"
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}[信息] 解压并安装Maven...${NC}"
            sudo tar -xzf "apache-maven-${maven_version}-bin.tar.gz"
            sudo rm -rf "$install_dir" 2>/dev/null
            sudo mv "apache-maven-${maven_version}" "$install_dir"
            
            # 更新环境变量
             echo -e "${GREEN}[信息] 配置环境变量...${NC}"
             echo 'export MAVEN_HOME=/opt/maven' | sudo tee /etc/profile.d/maven.sh
             echo 'export PATH=$MAVEN_HOME/bin:$PATH' | sudo tee -a /etc/profile.d/maven.sh
             sudo chmod +x /etc/profile.d/maven.sh
             
             # 立即加载环境变量
             source /etc/profile.d/maven.sh
             
             # 为当前会话设置环境变量（确保立即生效）
             export MAVEN_HOME=/opt/maven
             export PATH=/opt/maven/bin:$PATH
             
             # 清理下载文件
             rm -f "apache-maven-${maven_version}-bin.tar.gz"
             
             # 验证新版本是否正确加载
             echo -e "${GREEN}[信息] 验证Maven版本...${NC}"
             hash -r  # 清除命令缓存
             which mvn
             mvn --version | head -1
             
             echo -e "${GREEN}[完成] Maven ${maven_version} 安装成功！${NC}"
        else
            echo -e "${YELLOW}[错误] Maven下载失败，尝试使用包管理器...${NC}"
            
            if command -v apt-get &> /dev/null; then
                # Ubuntu/Debian
                echo -e "${GREEN}[信息] 使用apt-get安装Maven...${NC}"
                sudo apt-get update
                sudo apt-get install -y maven
            elif command -v yum &> /dev/null; then
                # CentOS/RHEL
                echo -e "${GREEN}[信息] 使用yum安装Maven...${NC}"
                sudo yum install -y maven
            elif command -v dnf &> /dev/null; then
                # Fedora
                echo -e "${GREEN}[信息] 使用dnf安装Maven...${NC}"
                sudo dnf install -y maven
            else
                echo -e "${RED}[错误] 无法识别的Linux发行版，请手动安装Maven${NC}"
                exit 1
            fi
        fi
        
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS系统
        if command -v brew &> /dev/null; then
            echo -e "${GREEN}[信息] 使用Homebrew安装Maven...${NC}"
            if [ "$action" == "update" ]; then
                brew upgrade maven
            else
                brew install maven
            fi
        else
            echo -e "${RED}[错误] 未找到Homebrew，请先安装Homebrew${NC}"
            echo "安装Homebrew: /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
            exit 1
        fi
    else
        echo -e "${RED}[错误] 不支持的操作系统类型: $OSTYPE${NC}"
        exit 1
    fi
}

# 检查Maven是否安装，如果没有则询问是否自动安装
check_maven() {
    # 检查Maven是否安装并验证版本
    if ! check_maven_version; then
        if command -v mvn &> /dev/null; then
            # Maven已安装但版本过低
            echo
            read -p "是否更新Maven到最新版本？(y/n): " update_maven
            if [[ $update_maven == [Yy]* ]]; then
                install_or_update_maven "update"
            else
                echo -e "${RED}[错误] Maven版本过低，请更新后重新运行脚本${NC}"
                exit 1
            fi
        else
            # Maven未安装
            echo
            read -p "是否安装Maven最新版本？(y/n): " install_maven
            if [[ $install_maven == [Yy]* ]]; then
                install_or_update_maven "install"
            else
                echo -e "${RED}[错误] 请先安装Maven后重新运行脚本${NC}"
                exit 1
            fi
        fi
        
        # 验证安装/更新是否成功
        if check_maven_version; then
            echo -e "${GREEN}[完成] Maven版本验证通过！${NC}"
            mvn --version
        else
            echo -e "${RED}[错误] Maven安装/更新失败，请手动处理${NC}"
            exit 1
        fi
    fi
}

# 检查必要的工具
echo "检查必要工具..."
check_maven
check_command docker

echo -e "${GREEN}✓ 所有必要工具已安装${NC}"
echo

echo "1. 清理之前的构建..."
# 切换到项目根目录（docker脚本的上级目录）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT" || {
    echo -e "${RED}[错误] 无法切换到项目根目录: $PROJECT_ROOT${NC}"
    exit 1
}
echo -e "${GREEN}[信息] 当前工作目录: $(pwd)${NC}"
if mvn clean; then
    echo -e "${GREEN}✓ 清理完成${NC}"
else
    echo -e "${RED}✗ Maven清理失败！${NC}"
    echo "请检查Maven配置和网络连接。"
    exit 1
fi
echo

echo "2. 编译和打包应用..."
if mvn package -DskipTests; then
    echo -e "${GREEN}✓ 打包完成${NC}"
else
    echo -e "${RED}✗ Maven打包失败！${NC}"
    echo "请检查代码编译错误或依赖问题。"
    exit 1
fi
echo

# 检查JAR文件是否存在
if [ ! -f "target/$JAR_NAME" ]; then
    echo -e "${RED}错误: JAR文件不存在: target/$JAR_NAME${NC}"
    exit 1
fi

echo "3. 构建Docker镜像..."
if docker build -f docker/Dockerfile -t $IMAGE_NAME:latest .; then
    echo -e "${GREEN}✓ Docker镜像构建完成${NC}"
else
    echo -e "${RED}✗ Docker镜像构建失败！${NC}"
    echo "请检查Dockerfile配置和Docker服务状态。"
    exit 1
fi
echo

echo "4. 停止并删除现有容器（如果存在）..."
docker stop $CONTAINER_NAME 2>/dev/null || true
docker rm $CONTAINER_NAME 2>/dev/null || true
echo -e "${GREEN}✓ 清理完成${NC}"
echo

echo "5. 选择部署方式:"
echo "   1) 开发环境部署（包含数据库和Redis）"
echo "   2) 生产环境部署（仅应用，需要外部数据库）"
echo "   3) 仅构建，不部署"
read -p "请选择 (1-3): " choice

case $choice in
    1)
        echo
        echo "启动开发环境（包含MySQL、Redis、RabbitMQ）..."
        docker-compose -f docker/docker-compose.yml up -d
        echo
        echo -e "${GREEN}开发环境部署完成！${NC}"
        echo -e "${YELLOW}应用地址: http://localhost:8089/api/v1${NC}"
        echo -e "${YELLOW}MySQL: localhost:3306${NC}"
        echo -e "${YELLOW}Redis: localhost:6379${NC}"
        echo -e "${YELLOW}RabbitMQ管理界面: http://localhost:15672 (admin/admin123)${NC}"
        ;;
    2)
        echo
        echo "启动生产环境..."
        echo -e "${YELLOW}注意: 请确保已正确配置环境变量${NC}"
        docker-compose -f docker/docker-compose.prod.yml up -d
        echo
        echo -e "${GREEN}生产环境部署完成！${NC}"
        echo -e "${YELLOW}应用地址: http://localhost:8089/api/v1${NC}"
        ;;
    3)
        echo
        echo -e "${GREEN}构建完成，镜像名称: $IMAGE_NAME:latest${NC}"
        ;;
    *)
        echo -e "${YELLOW}无效选择，仅完成构建。${NC}"
        ;;
esac

echo
echo "========================================"
echo -e "${GREEN}构建脚本执行完成！${NC}"
echo "========================================"
echo
echo "常用命令:"
echo "  查看容器状态: docker ps"
echo "  查看应用日志: docker logs $CONTAINER_NAME"
echo "  停止服务: docker-compose -f docker/docker-compose.yml down"
echo "  进入容器: docker exec -it $CONTAINER_NAME sh"
echo