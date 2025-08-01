# 使用官方Python基础镜像（根据需求选择合适版本）
FROM python:3.9-slim

# 设置容器内的时区（可选）
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 设置工作目录
WORKDIR /app/data

# 先单独复制依赖文件以利用Docker缓存机制
COPY requirements.txt .

# 安装Python依赖（添加--no-cache-dir避免缓存占用空间）
RUN pip install --no-cache-dir -r requirements.txt -i https://mirrors.aliyun.com/pypi/simple


# 复制整个项目代码
COPY . .

# 修改后的CMD（使用环境变量）
CMD ["sh", "-c", "echo '正在启动脚本...'; python predict.py --dir ${DATA_DIR:-/app/data};ls;pwd"]
#CMD ["sh", "-c", "echo '正在启动脚本...'"]

# 如果需要暴露端口（根据实际服务配置）
# EXPOSE 8000

