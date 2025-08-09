#!/bin/bash

# 设置脚本在遇到错误时退出
set -e

echo "========================================"
echo "    ECC Server Demo - 仅应用部署脚本"
echo "========================================"
echo

# 检查是否在正确的目录
if [ ! -f "pom.xml" ]; then
    echo "[错误] 请在项目根目录下运行此脚本！"
    echo "当前目录: $(pwd)"
    exit 1
fi

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
        echo "[信息] 检测到Maven版本: $current_version"
        echo "[信息] Maven路径: $(which mvn)"
        
        # 简单的版本比较（假设版本格式为x.y.z）
        if [[ "$(printf '%s\n' "$required_version" "$current_version" | sort -V | head -n1)" == "$required_version" ]]; then
            echo "[信息] Maven版本满足要求 (>= $required_version)"
            return 0
        else
            echo "[警告] Maven版本过低 ($current_version < $required_version)"
            return 1
        fi
    else
        echo "[警告] Maven未安装"
        return 1
    fi
}

# 安装或更新Maven的函数
install_or_update_maven() {
    local action="$1"  # "install" 或 "update"
    
    echo "[信息] 开始${action}Maven到最新版本..."
    
    # 检测操作系统
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux系统 - 手动安装最新版本
        local maven_version="3.9.4"
        local maven_url="https://archive.apache.org/dist/maven/maven-3/${maven_version}/binaries/apache-maven-${maven_version}-bin.tar.gz"
        local install_dir="/opt/maven"
        
        echo "[信息] 下载Maven ${maven_version}..."
        cd /tmp
        wget -q "$maven_url" -O "apache-maven-${maven_version}-bin.tar.gz"
        
        if [ $? -eq 0 ]; then
            echo "[信息] 解压并安装Maven..."
            sudo tar -xzf "apache-maven-${maven_version}-bin.tar.gz"
            sudo rm -rf "$install_dir" 2>/dev/null
            sudo mv "apache-maven-${maven_version}" "$install_dir"
            
            # 更新环境变量
            echo "[信息] 配置环境变量..."
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
            echo "[信息] 验证Maven版本..."
            hash -r  # 清除命令缓存
            which mvn
            mvn --version | head -1
            
            echo "[完成] Maven ${maven_version} 安装成功！"
        else
            echo "[错误] Maven下载失败，尝试使用包管理器..."
            
            if command -v apt-get &> /dev/null; then
                # Ubuntu/Debian
                echo "[信息] 使用apt-get安装Maven..."
                sudo apt-get update
                sudo apt-get install -y maven
            elif command -v yum &> /dev/null; then
                # CentOS/RHEL
                echo "[信息] 使用yum安装Maven..."
                sudo yum install -y maven
            elif command -v dnf &> /dev/null; then
                # Fedora
                echo "[信息] 使用dnf安装Maven..."
                sudo dnf install -y maven
            else
                echo "[错误] 无法识别的Linux发行版，请手动安装Maven"
                exit 1
            fi
        fi
        
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS系统
        if command -v brew &> /dev/null; then
            echo "[信息] 使用Homebrew安装Maven..."
            if [ "$action" == "update" ]; then
                brew upgrade maven
            else
                brew install maven
            fi
        else
            echo "[错误] 未找到Homebrew，请先安装Homebrew"
            echo "安装Homebrew: /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
            exit 1
        fi
    else
        echo "[错误] 不支持的操作系统类型: $OSTYPE"
        exit 1
    fi
}

# 检查Maven是否安装并验证版本
if ! check_maven_version; then
    if command -v mvn &> /dev/null; then
        # Maven已安装但版本过低
        echo
        read -p "是否更新Maven到最新版本？(y/n): " update_maven
        if [[ $update_maven == [Yy]* ]]; then
            install_or_update_maven "update"
        else
            echo "[错误] Maven版本过低，请更新后重新运行脚本"
            exit 1
        fi
    else
        # Maven未安装
        echo
        read -p "是否安装Maven最新版本？(y/n): " install_maven
        if [[ $install_maven == [Yy]* ]]; then
            install_or_update_maven "install"
        else
            echo "[错误] 请先安装Maven后重新运行脚本"
            exit 1
        fi
    fi
    
    # 验证安装/更新是否成功
    if check_maven_version; then
        echo "[完成] Maven版本验证通过！"
        mvn --version
    else
        echo "[错误] Maven安装/更新失败，请手动处理"
        exit 1
    fi
fi

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "[错误] Docker未安装或未启动！"
    echo "请先安装Docker并确保Docker服务正在运行。"
    exit 1
fi

# 检查docker-compose是否可用
if ! command -v docker-compose &> /dev/null; then
    echo "[错误] docker-compose未安装！"
    echo "请先安装docker-compose。"
    exit 1
fi

echo "[信息] 环境检查通过！"
echo

# 清理之前的构建
echo "[步骤 1/4] 清理之前的构建..."
# 切换到项目根目录（docker脚本的上级目录）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT" || {
    echo "[错误] 无法切换到项目根目录: $PROJECT_ROOT"
    exit 1
}
echo "[信息] 当前工作目录: $(pwd)"
if mvn clean; then
    echo "[完成] 清理完成"
else
    echo "[错误] Maven清理失败！"
    echo "请检查Maven配置和网络连接。"
    exit 1
fi

# 编译和打包
echo "[步骤 2/4] 编译和打包应用..."
if mvn package -DskipTests; then
    echo "[完成] 应用打包完成"
else
    echo "[错误] Maven打包失败！"
    echo "请检查代码编译错误或依赖问题。"
    exit 1
fi

# 构建Docker镜像
echo "[步骤 3/4] 构建Docker镜像..."
if docker build -f docker/Dockerfile -t ecc-server-demo:latest .; then
    echo "[完成] Docker镜像构建完成"
else
    echo "[错误] Docker镜像构建失败！"
    echo "请检查Dockerfile配置和Docker服务状态。"
    exit 1
fi

# 检查环境变量文件
echo "[步骤 4/4] 检查环境配置..."
if [ ! -f "docker/.env" ]; then
    echo "[警告] 未找到环境变量文件 docker/.env"
    echo "[提示] 请复制 docker/.env.app-only.example 为 docker/.env 并修改配置"
    echo
    read -p "是否现在复制示例文件？(y/n): " choice
    if [[ $choice == [Yy]* ]]; then
        cp "docker/.env.app-only.example" "docker/.env"
        echo "[完成] 已复制示例文件，请修改 docker/.env 中的配置后重新运行脚本"
        exit 0
    fi
fi

# 部署应用
echo
echo "[部署] 启动应用容器..."
docker-compose -f docker/docker-compose.app-only.yml --env-file docker/.env up -d

echo
echo "========================================"
echo "           部署完成！"
echo "========================================"
echo
echo "应用访问地址:"
echo "  - 主服务: http://localhost:8089"
echo "  - 管理端口: http://localhost:8090"
echo "  - 健康检查: http://localhost:8089/api/v1/actuator/health"
echo
echo "常用命令:"
echo "  查看容器状态: docker-compose -f docker/docker-compose.app-only.yml ps"
echo "  查看应用日志: docker-compose -f docker/docker-compose.app-only.yml logs -f ecc-app"
echo "  停止应用:     docker-compose -f docker/docker-compose.app-only.yml down"
echo "  重启应用:     docker-compose -f docker/docker-compose.app-only.yml restart ecc-app"
echo "  进入容器:     docker exec -it ecc-app /bin/sh"
echo
echo "注意事项:"
echo "  - 请确保外部MySQL、Redis等服务正常运行"
echo "  - 请确保数据库已导入 mysql/ecc.sql 脚本"
echo "  - 如有问题请检查 docker/.env 配置文件"
echo