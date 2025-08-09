@echo off
REM ========================================
REM ECC Server Demo 构建和部署脚本
REM ========================================

echo 开始构建 ECC Server Demo...
echo.

REM 设置变量
set JAR_NAME=CeditWarning.jar
set IMAGE_NAME=ecc-server-demo
set CONTAINER_NAME=ecc-server

REM 检查Maven是否安装
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Maven 未安装或未添加到PATH中
    pause
    exit /b 1
)

REM 检查Docker是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Docker 未安装或未启动
    pause
    exit /b 1
)

echo 1. 清理之前的构建...
mvn clean
if %errorlevel% neq 0 (
    echo 错误: Maven clean 失败
    pause
    exit /b 1
)

echo.
echo 2. 编译和打包应用...
mvn package -DskipTests
if %errorlevel% neq 0 (
    echo 错误: Maven 打包失败
    pause
    exit /b 1
)

REM 检查JAR文件是否存在
if not exist "target\%JAR_NAME%" (
    echo 错误: JAR文件不存在: target\%JAR_NAME%
    pause
    exit /b 1
)

echo.
echo 3. 构建Docker镜像...
docker build -f docker/Dockerfile -t %IMAGE_NAME%:latest .
if %errorlevel% neq 0 (
    echo 错误: Docker 镜像构建失败
    pause
    exit /b 1
)

echo.
echo 4. 停止并删除现有容器（如果存在）...
docker stop %CONTAINER_NAME% 2>nul
docker rm %CONTAINER_NAME% 2>nul

echo.
echo 5. 选择部署方式:
echo    1) 开发环境部署（包含数据库和Redis）
echo    2) 生产环境部署（仅应用，需要外部数据库）
echo    3) 仅构建，不部署
set /p choice="请选择 (1-3): "

if "%choice%"=="1" (
    echo.
    echo 启动开发环境（包含MySQL、Redis、RabbitMQ）...
    docker-compose -f docker/docker-compose.yml up -d
    if %errorlevel% neq 0 (
        echo 错误: Docker Compose 启动失败
        pause
        exit /b 1
    )
    echo.
    echo 开发环境部署完成！
    echo 应用地址: http://localhost:8089/api/v1
    echo MySQL: localhost:3306
    echo Redis: localhost:6379
    echo RabbitMQ管理界面: http://localhost:15672 (admin/admin123)
) else if "%choice%"=="2" (
    echo.
    echo 启动生产环境...
    echo 注意: 请确保已正确配置环境变量
    docker-compose -f docker/docker-compose.prod.yml up -d
    if %errorlevel% neq 0 (
        echo 错误: 生产环境启动失败
        pause
        exit /b 1
    )
    echo.
    echo 生产环境部署完成！
    echo 应用地址: http://localhost:8089/api/v1
) else if "%choice%"=="3" (
    echo.
    echo 构建完成，镜像名称: %IMAGE_NAME%:latest
) else (
    echo 无效选择，仅完成构建。
)

echo.
echo ========================================
echo 构建脚本执行完成！
echo ========================================
echo.
echo 常用命令:
echo   查看容器状态: docker ps
echo   查看应用日志: docker logs %CONTAINER_NAME%
echo   停止服务: docker-compose -f docker/docker-compose.yml down
echo   进入容器: docker exec -it %CONTAINER_NAME% sh
echo.
pause