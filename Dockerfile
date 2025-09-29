# 使用多阶段构建优化镜像大小
# 第一阶段：构建阶段
FROM maven:3.9-amazoncorretto-21 AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml文件，利用Docker缓存层
COPY pom.xml .

# 下载依赖（这一步会被Docker缓存，除非pom.xml发生变化）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests -B

# 第二阶段：运行阶段
FROM amazoncorretto:21-alpine

# 设置工作目录
WORKDIR /app

# 创建非root用户运行应用（安全最佳实践）
RUN addgroup -g 1001 -S tripdog && \
    adduser -u 1001 -S tripdog -G tripdog

# 安装必要的运行时依赖
RUN apk add --no-cache \
    tzdata \
    curl \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 从构建阶段复制JAR文件
COPY --from=builder /app/target/tripdog-backend-*.jar app.jar

# 修改文件所有者
RUN chown -R tripdog:tripdog /app

# 切换到非root用户
USER tripdog

# 暴露端口
EXPOSE 7979

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:7979/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
