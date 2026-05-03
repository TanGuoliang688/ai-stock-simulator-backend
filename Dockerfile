# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 构建项目
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 复制构建产物
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8081

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
