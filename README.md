"# graduation-project-estude-lms" 
# E-Study Backend - Spring Boot Setup Guide

## 📋 Yêu cầu hệ thống

- Java 17 trở lên
- Docker Desktop hoặc Docker trên Ubuntu
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## 🐘 Cài đặt PostgreSQL bằng Docker

### Cách 1: Sử dụng Docker Desktop (Windows/Mac/Linux)

1. **Tải và cài đặt Docker Desktop:**
   - Windows/Mac: https://www.docker.com/products/docker-desktop/
   - Khởi động Docker Desktop

2. **Chạy PostgreSQL container:**
```bash
# Tạo và chạy PostgreSQL container
docker run --name estude-postgres \
  -e POSTGRES_DB=mydb \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=secret \
  -p 5432:5432 \
  -v estude_postgres_data:/var/lib/postgresql/data \
  -d postgres:15

# Kiểm tra container đang chạy
docker ps
```

### Cách 2: Sử dụng Docker trên Ubuntu

1. **Cài đặt Docker trên Ubuntu:**
```bash
# Cập nhật package
sudo apt update

# Cài đặt Docker
sudo apt install docker.io -y

# Khởi động Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Thêm user vào group docker (không cần sudo)
sudo usermod -aG docker $USER
# Logout và login lại để áp dụng
```

2. **Chạy PostgreSQL container:**
```bash
# Tạo và chạy PostgreSQL container
docker run --name estude-postgres \
  -e POSTGRES_DB=mydb \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=secret \
  -p 5432:5432 \
  -v estude_postgres_data:/var/lib/postgresql/data \
  -d postgres:15
```

### Cách 3: Sử dụng Docker Compose (Khuyến nghị)

1. **Tạo file `docker-compose.yml` trong thư mục dự án:**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: estude-postgres
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data:
```

2. **Chạy Docker Compose:**
```bash
# Chạy database
docker-compose up -d

# Kiểm tra trạng thái
docker-compose ps

# Dừng database
docker-compose down
```

## 🔧 Cấu hình môi trường

1. **Tạo file `.env` trong thư mục root:**
```env
# Database Configuration
POSTGRES_URL=jdbc:postgresql://localhost:5432/mydb
POSTGRES_USER=admin
POSTGRES_PASSWORD=secret

# JWT Configuration
JWT_SECRET_KEY=your-very-secure-secret-key-here-at-least-256-bits-long
JWT_EXPIRATION_MS=3600000
```

2. **Kiểm tra file `application.properties`:**
```properties
spring.application.name=estude-backend-spring

spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=admin
spring.datasource.password=secret

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

jwt.secret=${JWT_SECRET_KEY}
jwt.expiration=${JWT_EXPIRATION_MS}
```

## 🚀 Chạy dự án

### Cách 1: Sử dụng IDE (IntelliJ IDEA/Eclipse)

1. Import project vào IDE
2. Chờ IDE tải dependencies
3. Chạy class `EstudeBackendSpringApplication`

### Cách 2: Sử dụng Gradle Command Line

```bash
# Windows
./gradlew bootRun

# Linux/Mac
./gradlew bootRun
```

### Cách 3: Build và chạy JAR file

```bash
# Build project
./gradlew build

# Chạy JAR file
java -jar build/libs/estude-backend-spring-0.0.1-SNAPSHOT.jar
```

## 📖 Truy cập API Documentation

Sau khi dự án đã chạy thành công (thường ở port 8080):

### Swagger UI - Giao diện web để test API:
- **URL chính:** http://localhost:8080/swagger-ui/index.html
- **URL phụ:** http://localhost:8080/swagger-ui.html

### OpenAPI Documentation:
- **JSON format:** http://localhost:8080/v3/api-docs
- **YAML format:** http://localhost:8080/v3/api-docs.yaml

### API Endpoints chính:

| Endpoint | Method | Mô tả |
|----------|---------|--------|
| `/api/auth/login` | POST | Đăng nhập |
| `/api/auth/forgot-password` | POST | Quên mật khẩu |
| `/api/auth/reset-password` | POST | Đặt lại mật khẩu |
| `/api/auth/logout` | POST | Đăng xuất |
| `/api/ai/predict` | POST | Dự đoán AI |

## 🔍 Kiểm tra kết nối Database

### Sử dụng Docker CLI:
```bash
# Kết nối vào PostgreSQL container
docker exec -it estude-postgres psql -U admin -d mydb

# Một số lệnh SQL hữu ích:
\dt    # Liệt kê các bảng
\q     # Thoát
```

### Sử dụng GUI Tools:
- **pgAdmin:** http://localhost:5050 (nếu cài thêm)
- **DBeaver:** Kết nối với host: localhost, port: 5432
- **DataGrip:** Tạo data source PostgreSQL

## ⚠️ Troubleshooting

### 1. Port 5432 đã được sử dụng:
```bash
# Kiểm tra process đang dùng port
netstat -an | grep 5432

# Dừng PostgreSQL local nếu có
sudo systemctl stop postgresql
```

### 2. Container không khởi động:
```bash
# Xem logs
docker logs estude-postgres

# Xóa container cũ
docker rm -f estude-postgres

# Tạo lại container
docker run --name estude-postgres ...
```

### 3. Lỗi kết nối database:
- Kiểm tra container đang chạy: `docker ps`
- Kiểm tra cấu hình trong `application.properties`
- Kiểm tra firewall/port blocking

### 4. Application không start:
```bash
# Kiểm tra Java version
java -version

# Build lại project
./gradlew clean build

# Kiểm tra logs khi chạy
./gradlew bootRun --info
```

## 🎯 Các bước nhanh để bắt đầu:

1. **Cài Docker Desktop và khởi động**
2. **Clone repository**
3. **Chạy PostgreSQL:**
   ```bash
   docker run --name estude-postgres -e POSTGRES_DB=mydb -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=secret -p 5432:5432 -d postgres:15
   ```
4. **Tạo file `.env` với JWT secret**
5. **Chạy dự án:**
   ```bash
   ./gradlew bootRun
   ```
6. **Truy cập Swagger UI:** http://localhost:8080/swagger-ui/index.html

