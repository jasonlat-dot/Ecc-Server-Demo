@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo    ECC Server Demo - 仅应用部署脚本
echo ========================================
echo.

:: 检查是否在正确的目录
if not exist "pom.xml" (
    echo [错误] 请在项目根目录下运行此脚本！
    echo 当前目录: %CD%
    pause
    exit /b 1
)

:: 检查Maven是否安装
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [错误] Maven未安装或未添加到PATH环境变量中！
    echo 请先安装Maven并配置环境变量。
    pause
    exit /b 1
)

:: 检查Docker是否安装
docker --version >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker未安装或未启动！
    echo 请先安装Docker并确保Docker服务正在运行。
    pause
    exit /b 1
)

:: 检查docker-compose是否可用
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [错误] docker-compose未安装！
    echo 请先安装docker-compose。
    pause
    exit /b 1
)

echo [信息] 环境检查通过！
echo.

:: 清理之前的构建
echo [步骤 1/4] 清理之前的构建...
mvn clean >nul 2>&1
if errorlevel 1 (
    echo [错误] Maven清理失败！
    pause
    exit /b 1
)
echo [完成] 清理完成

:: 编译和打包
echo [步骤 2/4] 编译和打包应用...
mvn package -DskipTests
if errorlevel 1 (
    echo [错误] Maven打包失败！
    pause
    exit /b 1
)
echo [完成] 应用打包完成

:: 构建Docker镜像
echo [步骤 3/4] 构建Docker镜像...
docker build -f docker/Dockerfile -t ecc-server-demo:latest .
if errorlevel 1 (
    echo [错误] Docker镜像构建失败！
    pause
    exit /b 1
)
echo [完成] Docker镜像构建完成

:: 检查环境变量文件
echo [步骤 4/4] 检查环境配置...
if not exist "docker\.env" (
    echo [警告] 未找到环境变量文件 docker\.env
    echo [提示] 请复制 docker\.env.app-only.example 为 docker\.env 并修改配置
    echo.
    echo 是否现在复制示例文件？(y/n)
    set /p choice="请选择: "
    if /i "!choice!"=="y" (
        copy "docker\.env.app-only.example" "docker\.env"
        echo [完成] 已复制示例文件，请修改 docker\.env 中的配置后重新运行脚本
        pause
        exit /b 0
    )
)

:: 部署应用
echo.
echo [部署] 启动应用容器...
docker-compose -f docker/docker-compose.app-only.yml --env-file docker/.env up -d
if errorlevel 1 (
    echo [错误] 应用部署失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo           部署完成！
echo ========================================
echo.
echo 应用访问地址:
echo   - 主服务: http://localhost:8089
echo   - 管理端口: http://localhost:8090
echo   - 健康检查: http://localhost:8089/api/v1/actuator/health
echo.
echo 常用命令:
echo   查看容器状态: docker-compose -f docker/docker-compose.app-only.yml ps
echo   查看应用日志: docker-compose -f docker/docker-compose.app-only.yml logs -f ecc-app
echo   停止应用:     docker-compose -f docker/docker-compose.app-only.yml down
echo   重启应用:     docker-compose -f docker/docker-compose.app-only.yml restart ecc-app
echo   进入容器:     docker exec -it ecc-app /bin/sh
echo.
echo 注意事项:
echo   - 请确保外部MySQL、Redis等服务正常运行
echo   - 请确保数据库已导入 mysql/ecc.sql 脚本
echo   - 如有问题请检查 docker/.env 配置文件
echo.
echo 按任意键退出...
pause >nul