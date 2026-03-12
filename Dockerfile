FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制jar包
COPY target/code-grading-system-0.0.1-SNAPSHOT.jar app.jar

# 创建必要的目录
RUN mkdir -p /app/submissions /app/outputs

# 设置权限
RUN chmod -R 755 /app

EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]