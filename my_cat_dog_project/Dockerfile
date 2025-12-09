# 基础镜像：选择包含Python的官方镜像
FROM python:3.10-slim

# 设置工作目录（容器内的目录）
WORKDIR /app

# 复制依赖文件到工作目录
COPY requirements.txt .

# 安装依赖（使用清华镜像源加速下载）
RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 复制项目所有文件到工作目录
COPY . .

# 容器启动时默认执行的命令（可以被docker-compose.yml中的command覆盖）
CMD ["python", "train.py"]