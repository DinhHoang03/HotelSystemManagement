# Hotel System Management

Hệ thống quản lý khách sạn - Đồ án KHMT 2025

## Mô tả

Hệ thống quản lý khách sạn được xây dựng bằng Spring Boot với các tính năng hiện đại như thanh toán trực tuyến, tích hợp AI, và bảo mật cao.

## Công nghệ sử dụng

### Backend Framework
- **Spring Boot 3.4.2** - Framework chính
- **Spring Security** - Bảo mật và xác thực
- **Spring Data JPA** - Quản lý dữ liệu
- **Spring WebFlux** - Reactive programming
- **Spring OAuth2 Resource Server** - OAuth2 authentication

### Database
- **MySQL** - Database chính
- **Redis** - Caching và session management

### Security & Authentication
- **JWT (JSON Web Token)** - Token-based authentication
    - jjwt-api, jjwt-impl, jjwt-jackson (v0.11.5)
- **Jasypt** - Mã hóa thông tin nhạy cảm trong config
- **SSL/TLS** - HTTPS với keystore.jks

### Payment Integration
- **PayPal SDK** (v1.14.0) - Thanh toán quốc tế
- **ZaloPay API** - Thanh toán nội địa Việt Nam

### AI Integration
- **Spring AI** (v1.0.0) - OpenAI integration
- **Gemini 2.0 Flash** - AI model cho chatbot/hỗ trợ

### Utilities & Libraries
- **Lombok** (v1.18.36) - Giảm boilerplate code
- **MapStruct** (v1.5.5.Final) - Object mapping
- **Jackson** - JSON processing
- **Apache HttpComponents** - HTTP client
- **Apache Commons Codec** - HMAC-SHA256 encryption
- **Spring Retry** - Retry logic
- **JavaParser** (v3.25.4) - Java code parsing
- **Thymeleaf** - Template engine
- **Spring Mail** - Email service

### Development Tools
- **Spring Boot DevTools** - Hot reload
- **Maven** - Build tool
- **Java 21** - Programming language

## Yêu cầu hệ thống

- Java 21 hoặc cao hơn
- Maven 3.6+
- MySQL 8.0+
- Docker (cho Redis)
- Ngrok (cho webhook callback)

## Cài đặt và cấu hình

### 1. Cài đặt MySQL

Tạo database:
```sql
CREATE DATABASE hotel_database_management;
```

### 2. Cài đặt và chạy Redis với Docker

```bash
# Pull Redis image
docker pull redis:latest

# Chạy Redis container
docker run --name hotel-redis -p 6379:6379 -d redis:latest

# Kiểm tra Redis đang chạy
docker ps

# Test kết nối Redis
docker exec -it hotel-redis redis-cli ping
# Output: PONG
```

### 3. Cài đặt Ngrok

**Bước 1:** Tải Ngrok
- Truy cập: https://ngrok.com/download
- Tải phiên bản Windows (ngrok.exe)

**Bước 2:** Cài đặt vào đúng thư mục
```bash
# Tạo thư mục (nếu chưa có)
mkdir C:\ngrok

# Copy file ngrok.exe vào C:\ngrok\
# Đường dẫn cuối cùng: C:\ngrok\ngrok.exe
```

**Bước 3:** Cấu hình Ngrok authtoken (chỉ cần làm 1 lần)
```bash
# Mở Command Prompt
cd C:\ngrok

# Đăng ký tài khoản tại https://dashboard.ngrok.com/signup
# Lấy authtoken từ https://dashboard.ngrok.com/get-started/your-authtoken

# Cấu hình authtoken
ngrok config add-authtoken YOUR_AUTH_TOKEN
```

**✨ Lưu ý:** Dự án đã có script tự động khởi động ngrok và cập nhật callback URL. Bạn chỉ cần đảm bảo `ngrok.exe` nằm đúng vị trí `C:\ngrok\ngrok.exe`, script sẽ tự động:
- Khởi động ngrok trên port 8443
- Lấy public URL từ ngrok API
- Tự động cập nhật `callback-url` trong `application.yaml`
- Set biến môi trường `NGROK_URL`

### 4. Cấu hình application.yaml

Tạo file `src/main/resources/application-secret.yaml`:

```yaml
spring:
  mail:
    username: your-email@gmail.com
    password: your-app-password
  datasource:
    username: root
    password: your-mysql-password

paypal:
  client-id: your-paypal-client-id
  client-secret: your-paypal-client-secret

zalo-pay:
  app-id: your-zalopay-app-id
  key1: your-zalopay-key1
  key2: your-zalopay-key2
  callback-url: https://YOUR-NGROK-URL/zalopay/callback

jwt:
  signerKey: your-secret-key-at-least-32-characters
```

### 5. Cấu hình SSL Certificate

```bash
# Tạo keystore.jks (nếu chưa có)
keytool -genkeypair -alias myssl -keyalg RSA -keysize 2048 \
  -storetype JKS -keystore keystore.jks -validity 3650 \
  -storepass YOUR_PASSWORD -keypass YOUR_PASSWORD

# Copy keystore.jks vào src/main/resources/
```

Cập nhật password trong `application.yaml`:
```yaml
server:
  ssl:
    key-store-password: YOUR_PASSWORD
    key-password: YOUR_PASSWORD
```

### 6. Cấu hình Google AI (Gemini)

Lấy API key từ: https://makersuite.google.com/app/apikey

Cập nhật trong `application.yaml`:
```yaml
spring:
  ai:
    openai:
      api-key: "YOUR_GEMINI_API_KEY"
```

## Chạy ứng dụng

### Bước 1: Build project
```bash
mvn clean install
```

### Bước 2: Chạy Redis
```bash
docker start hotel-redis
```

### Bước 3: Chạy application
Ứng dụng có script tự động khởi động ngrok, chỉ cần chạy:

**Cách 1: Sử dụng Maven**
```bash
mvn spring-boot:run
```

**Cách 2: Chạy JAR file**
```bash
java -jar target/HotelSystemManagement-0.0.1-SNAPSHOT.jar
```

**Cách 3: Chạy script batch (khởi động cả ngrok)**
```bash
# Chạy file start-ngrok.bat trong thư mục scripts/
.\scripts\start-ngrok.bat
```

✅ Script sẽ tự động:
- Khởi động ngrok trên port 8443
- Lấy public URL và cập nhật vào `application.yaml`
- Thiết lập biến môi trường `NGROK_URL`

Ứng dụng sẽ chạy tại: **https://localhost:8443**

## Mã hóa thông tin nhạy cảm với Jasypt

### Mã hóa giá trị:
```bash
mvn jasypt:encrypt-value -Djasypt.encryptor.password=YOUR_MASTER_PASSWORD \
  -Djasypt.plugin.value="value-to-encrypt"
```

### Sử dụng giá trị đã mã hóa:
```yaml
spring:
  datasource:
    password: ENC(encrypted-value-here)
```

### Chạy với master password:
```bash
java -jar target/HotelSystemManagement-0.0.1-SNAPSHOT.jar \
  --jasypt.encryptor.password=YOUR_MASTER_PASSWORD
```

## Cấu trúc dự án

```
HotelSystemManagement/
├── .idea/                           # IntelliJ IDEA config
├── .mvn/                            # Maven wrapper
├── .vscode/                         # VS Code config
├── scripts/
│   └── start-ngrok.ps1             # PowerShell script tự động ngrok
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/humg/HotelSystemManagement/
│   │   │       ├── configs/
│   │   │       │   ├── AppConfig.java
│   │   │       │   ├── ApplicationInitConfig.java
│   │   │       │   ├── CorsConfig.java
│   │   │       │   ├── JacksonConfig.java
│   │   │       │   └── RetryConfig.java
│   │   │       ├── exceptions/
│   │   │       │   ├── enums/
│   │   │       │   ├── exceptions/
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── modules/
│   │   │       │   ├── admin_service/
│   │   │       │   ├── ai_service/
│   │   │       │   ├── auth_service/
│   │   │       │   ├── booking_service/
│   │   │       │   ├── customer_service/
│   │   │       │   ├── email_service/
│   │   │       │   ├── employee_service/
│   │   │       │   ├── hotel_offer_service/
│   │   │       │   ├── payment_service/
│   │   │       │   ├── redis_service/
│   │   │       │   └── room_service/
│   │   │       ├── utils/
│   │   │       │   ├── enums/
│   │   │       │   ├── interfaces/
│   │   │       │   │   ├── APIResponse.java
│   │   │       │   │   └── NormalizeString.java
│   │   │       │   └── HotelSystemManagementApplication.java
│   │   └── resources/
│   │       ├── static/                      # Static files (CSS, JS, images)
│   │       ├── templates/
│   │       │   ├── booking-confirmation.html
│   │       │   └── otp-email.html
│   │       ├── application.yaml
│   │       ├── application-secret.yaml
│   │       └── keystore.jks
│   └── test/
│       └── java/
│           └── com/humg/HotelSystemManagement/
│               ├── HotelSystemManagementApplicationTests.java
│               └── JasyptDecryptorTest.java
├── target/                          # Build output
├── .gitattributes
├── .gitignore
├── HELP.md
├── HotelSystemManagement.iml
├── mvnw                             # Maven wrapper (Unix)
├── mvnw.cmd                         # Maven wrapper (Windows)
├── pom.xml
├── qodana.yaml
├── README.md
└── start-app.bat                    # Script khởi động ứng dụng
```

### Cấu trúc Modules (Services)

Dự án được tổ chức theo kiến trúc microservices với các module chính:

- **admin_service**: Quản lý admin và phân quyền
- **ai_service**: Tích hợp AI (Gemini) cho chatbot/hỗ trợ
- **auth_service**: Xác thực và JWT token
- **booking_service**: Quản lý đặt phòng
- **customer_service**: Quản lý khách hàng
- **email_service**: Gửi email thông báo
- **employee_service**: Quản lý nhân viên
- **hotel_offer_service**: Quản lý ưu đãi khách sạn
- **payment_service**: Xử lý thanh toán (PayPal, ZaloPay)
- **redis_service**: Cache và session management
- **room_service**: Quản lý phòng khách sạn

### Scripts tự động

**start-ngrok.ps1** (trong thư mục `scripts/`):
- Kiểm tra `ngrok.exe` tồn tại tại `C:\ngrok\ngrok.exe`
- Khởi động ngrok tunnel trên port 8443 (chạy ẩn background)
- Lấy public HTTPS URL từ ngrok API (http://localhost:4040/api/tunnels)
- Tự động cập nhật `callback-url` trong `application.yaml`
- Set biến môi trường `NGROK_URL`

**start-app.bat** (trong thư mục root):
- Tự động gọi `start-ngrok.ps1`
- Khởi động Spring Boot application

## API Endpoints

Ứng dụng sử dụng HTTPS, tất cả endpoints bắt đầu với `https://localhost:8443`

### Authentication
- POST `/auth/login` - Đăng nhập
- POST `/auth/register` - Đăng ký
- POST `/auth/refresh` - Refresh token

### Payment
- POST `/payment/paypal/create` - Tạo thanh toán PayPal
- POST `/payment/zalopay/create` - Tạo thanh toán ZaloPay
- POST `/zalopay/callback` - ZaloPay webhook callback

## Troubleshooting

### Redis không kết nối được
```bash
# Kiểm tra Redis container
docker ps -a

# Xem logs
docker logs hotel-redis

# Restart container
docker restart hotel-redis
```

### Ngrok bị disconnect
```bash
# Script sẽ tự động khởi động lại ngrok khi chạy ứng dụng
# Nếu cần khởi động thủ công:
.\scripts\start-ngrok.bat

# Hoặc chạy trực tiếp PowerShell script:
powershell -ExecutionPolicy Bypass -File .\scripts\start-ngrok.ps1
```

**Lưu ý:** Callback URL sẽ được tự động cập nhật trong `application.yaml` bởi script.

### SSL Certificate lỗi
- Đảm bảo keystore.jks nằm trong `src/main/resources/`
- Kiểm tra password trong application.yaml
- Trong browser, chấp nhận self-signed certificate

### MySQL connection failed
- Kiểm tra MySQL service đang chạy
- Verify username/password trong application-secret.yaml
- Đảm bảo database đã được tạo

## Tác giả

MADE BY HOÀNG BÌNH ĐỊNH
