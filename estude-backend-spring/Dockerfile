# Stage 1: Build JAR using Gradle 9
FROM gradle:9.0.0-jdk21 AS builder
WORKDIR /app

# Copy toàn bộ project vào container
COPY . .

# Build file jar (Spring Boot)
RUN gradle clean bootJar --no-daemon

# Stage 2: Run the Spring Boot app
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy file jar đã build từ stage 1
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port 8080 cho Fly.io hoặc local test
EXPOSE 8080

# Kích hoạt profile prod (bạn có thể đổi nếu cần)
ENV SPRING_PROFILES_ACTIVE=prod

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
