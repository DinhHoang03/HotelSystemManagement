# 🏨 Hotel System Management

> **Hệ thống Quản lý Khách sạn Thông minh**

---

### 📚 Thông tin Đồ án

**Đồ án Khoa học Máy tính**  
🎓 Chuyên ngành: Khoa học Máy tính  
💻 Ngành: Công nghệ Thông tin  
🏛️ Trường: Đại học Mỏ - Địa chất  
📅 Năm học: 2024-2025

---

## 📖 Mô tả

Hệ thống quản lý khách sạn toàn diện được xây dựng trên nền tảng **Spring Boot**, tích hợp các công nghệ hiện đại:

✨ **Tính năng nổi bật:**
- 🤖 Tích hợp AI (Gemini 2.0) hỗ trợ khách hàng thông minh
- 💳 Đa dạng phương thức thanh toán (PayPal, ZaloPay)
- 🔒 Bảo mật cao cấp với JWT & SSL/TLS
- ⚡ Hiệu suất tối ưu với Redis caching
- 📧 Gửi email tự động thông báo & xác nhận
- 🎯 Kiến trúc phân tầng rõ ràng, dễ mở rộng

---

## 🛠️ Công nghệ sử dụng

### 🚀 Backend Framework
| Công nghệ | Phiên bản | Mô tả |
|-----------|-----------|-------|
| **Spring Boot** | 3.4.2 | Framework chính |
| **Spring Security** | Latest | Bảo mật và xác thực |
| **Spring Data JPA** | Latest | Quản lý dữ liệu |
| **Spring WebFlux** | Latest | Reactive programming |
| **OAuth2 Resource Server** | Latest | OAuth2 authentication |

### 💾 Database
- **MySQL 8.0+** - Cơ sở dữ liệu quan hệ chính
- **Redis** - Caching và quản lý session

### 🔐 Security & Authentication
- **JWT (JSON Web Token)** v0.11.5 - Xác thực dựa trên token
    - `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- **Jasypt** v3.0.5 - Mã hóa thông tin nhạy cảm
- **SSL/TLS** - HTTPS với keystore.jks

### 💰 Payment Integration
- **PayPal SDK** v1.14.0 - Thanh toán quốc tế
- **ZaloPay API** - Cổng thanh toán Việt Nam

### 🤖 AI Integration
- **Spring AI** v1.0.0 - Tích hợp OpenAI
- **Gemini 2.0 Flash** - AI model cho chatbot

### 📦 Utilities & Libraries
- **Lombok** v1.18.36 - Giảm boilerplate code
- **MapStruct** v1.5.5 - Object mapping tự động
- **Jackson** - Xử lý JSON
- **Apache HttpComponents** v4.5.14 - HTTP client
- **Apache Commons Codec** v1.15 - HMAC-SHA256
- **Spring Retry** v2.0.7 - Retry logic
- **JavaParser** v3.25.4 - Phân tích Java code
- **Thymeleaf** - Template engine
- **Spring Mail** - Dịch vụ email

### 🔧 Development Tools
- **Java 21** - Ngôn ngữ lập trình
- **Maven 3.6+** - Build tool
- **Spring Boot DevTools** - Hot reload
- **Docker** - Container hóa

---

## ⚙️ Yêu cầu hệ thống

```
☕ Java 21 hoặc cao hơn
📦 Maven 3.6+
🗄️ MySQL 8.0+
🐳 Docker (cho Redis)
🌐 Ngrok (cho webhook callback)
```

---

## 🚀 Cài đặt và cấu hình

### 1️⃣ Cài đặt MySQL

```sql
CREATE DATABASE hotel_database_management;
```

### 2️⃣ Cài đặt và chạy Redis với Docker

```bash
# Pull Redis image
docker pull redis:latest

# Chạy Redis container
docker run --name hotel-redis -p 6379:6379 -d redis:latest

# Kiểm tra Redis đang chạy
docker ps

# Test kết nối Redis
docker exec -it hotel-redis redis-cli ping
# ✅ Output: PONG
```

### 3️⃣ Cài đặt Ngrok

**Bước 1: Tải Ngrok**
- 🌐 Truy cập: https://ngrok.com/download
- 📥 Tải phiên bản Windows (ngrok.exe)

**Bước 2: Cài đặt vào thư mục**
```bash
# Tạo thư mục (nếu chưa có)
mkdir C:\ngrok

# Copy file ngrok.exe vào C:\ngrok\
# ✅ Đường dẫn cuối cùng: C:\ngrok\ngrok.exe
```

**Bước 3: Cấu hình Ngrok authtoken** *(chỉ cần làm 1 lần)*
```bash
# Mở Command Prompt
cd C:\ngrok

# 1. Đăng ký tài khoản: https://dashboard.ngrok.com/signup
# 2. Lấy authtoken: https://dashboard.ngrok.com/get-started/your-authtoken

# 3. Cấu hình authtoken
ngrok config add-authtoken YOUR_AUTH_TOKEN
```

> **✨ Lưu ý:** Dự án đã có script tự động! Bạn chỉ cần đảm bảo `ngrok.exe` ở đúng vị trí `C:\ngrok\ngrok.exe`
>
> Script sẽ tự động:
> - 🚀 Khởi động ngrok trên port 8443
> - 🔗 Lấy public URL từ ngrok API
> - 📝 Cập nhật `callback-url` trong `application.yaml`
> - 🌍 Set biến môi trường `NGROK_URL`

### 4️⃣ Cấu hình Application

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

### 5️⃣ Cấu hình SSL Certificate

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

### 6️⃣ Cấu hình Google AI (Gemini)

🔑 Lấy API key từ: https://makersuite.google.com/app/apikey

Cập nhật trong `application.yaml`:
```yaml
spring:
  ai:
    openai:
      api-key: "YOUR_GEMINI_API_KEY"
```

---

## ▶️ Chạy ứng dụng

### Bước 1: Build project
```bash
mvn clean install
```

### Bước 2: Chạy Redis
```bash
docker start hotel-redis
```

### Bước 3: Chạy application

**🎯 Cách 1: Sử dụng Maven**
```bash
mvn spring-boot:run
```

**🎯 Cách 2: Chạy JAR file**
```bash
java -jar target/HotelSystemManagement-0.0.1-SNAPSHOT.jar
```

**🎯 Cách 3: Chạy script batch** *(khởi động cả ngrok)*
```bash
.\scripts\start-ngrok.bat
```

✅ **Script sẽ tự động:**
- 🚀 Khởi động ngrok trên port 8443
- 🔗 Lấy public URL và cập nhật vào `application.yaml`
- 🌍 Thiết lập biến môi trường `NGROK_URL`

🌐 **Ứng dụng sẽ chạy tại:** `https://localhost:8443`

---

## 🔒 Mã hóa với Jasypt

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

---

## 📁 Cấu trúc dự án

```
HotelSystemManagement/
├── 📁 .idea/                        # IntelliJ IDEA config
├── 📁 .mvn/                         # Maven wrapper
├── 📁 .vscode/                      # VS Code config
├── 📁 scripts/
│   └── 📜 start-ngrok.ps1          # PowerShell script tự động ngrok
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/
│   │   │   └── 📁 com.humg.HotelSystemManagement/
│   │   │       ├── 📁 configs/
│   │   │       │   ├── ⚙️ AppConfig.java
│   │   │       │   ├── ⚙️ ApplicationInitConfig.java
│   │   │       │   ├── ⚙️ CorsConfig.java
│   │   │       │   ├── ⚙️ JacksonConfig.java
│   │   │       │   └── ⚙️ RetryConfig.java
│   │   │       ├── 📁 exceptions/
│   │   │       │   ├── 📁 enums/
│   │   │       │   ├── 📁 exceptions/
│   │   │       │   └── 🚨 GlobalExceptionHandler.java
│   │   │       ├── 📁 modules/
│   │   │       │   ├── 👤 admin_service/
│   │   │       │   ├── 🤖 ai_service/
│   │   │       │   ├── 🔐 auth_service/
│   │   │       │   ├── 📅 booking_service/
│   │   │       │   ├── 👥 customer_service/
│   │   │       │   ├── 📧 email_service/
│   │   │       │   ├── 👔 employee_service/
│   │   │       │   ├── 🎁 hotel_offer_service/
│   │   │       │   ├── 💳 payment_service/
│   │   │       │   ├── 💾 redis_service/
│   │   │       │   └── 🏠 room_service/
│   │   │       ├── 📁 utils/
│   │   │       │   ├── 📁 enums/
│   │   │       │   ├── 📁 interfaces/
│   │   │       │   │   ├── 🔌 APIResponse.java
│   │   │       │   │   └── 🔤 NormalizeString.java
│   │   │       │   └── ⚡ HotelSystemManagementApplication.java
│   │   └── 📁 resources/
│   │       ├── 📁 static/              # CSS, JS, images
│   │       ├── 📁 templates/
│   │       │   ├── 📧 booking-confirmation.html
│   │       │   └── 📧 otp-email.html
│   │       ├── ⚙️ application.yaml
│   │       ├── 🔒 application-secret.yaml
│   │       └── 🔐 keystore.jks
│   └── 📁 test/
│       └── 📁 java/
│           └── 📁 com.humg.HotelSystemManagement/
│               ├── ✅ HotelSystemManagementApplicationTests.java
│               └── ✅ JasyptDecryptorTest.java
├── 📁 target/                       # Build output
├── 📄 .gitattributes
├── 📄 .gitignore
├── 📄 HELP.md
├── 📄 HotelSystemManagement.iml
├── 🐧 mvnw                          # Maven wrapper (Unix)
├── 🪟 mvnw.cmd                      # Maven wrapper (Windows)
├── 📦 pom.xml
├── 📄 qodana.yaml
├── 📖 README.md
└── 🚀 start-app.bat                 # Script khởi động ứng dụng
```

---

## 🏗️ Kiến trúc Modules

Dự án được tổ chức theo **Kiến trúc phân tầng (Layered Architecture)** với 11 modules độc lập:

| Module | Icon | Chức năng |
|--------|------|-----------|
| **admin_service** | 👤 | Quản lý admin và phân quyền hệ thống |
| **ai_service** | 🤖 | Tích hợp AI (Gemini) cho chatbot & hỗ trợ |
| **auth_service** | 🔐 | Xác thực, phân quyền và JWT token |
| **booking_service** | 📅 | Quản lý đặt phòng và lịch trình |
| **customer_service** | 👥 | Quản lý thông tin khách hàng |
| **email_service** | 📧 | Gửi email thông báo tự động |
| **employee_service** | 👔 | Quản lý nhân viên khách sạn |
| **hotel_offer_service** | 🎁 | Quản lý ưu đãi và khuyến mãi |
| **payment_service** | 💳 | Xử lý thanh toán (PayPal, ZaloPay) |
| **redis_service** | 💾 | Cache và quản lý session |
| **room_service** | 🏠 | Quản lý phòng và loại phòng |

---

## 🤖 Scripts tự động

### 📜 start-ngrok.ps1
> *PowerShell script trong thư mục `scripts/`*

**Chức năng:**
- ✅ Kiểm tra `ngrok.exe` tại `C:\ngrok\ngrok.exe`
- 🚀 Khởi động ngrok tunnel port 8443 (background)
- 🔗 Lấy HTTPS URL từ API (http://localhost:4040/api/tunnels)
- 📝 Tự động cập nhật `callback-url` trong `application.yaml`
- 🌍 Set biến môi trường `NGROK_URL`

### 🚀 start-app.bat
> *Script khởi động trong thư mục root*

**Chức năng:**
- 🔗 Tự động gọi `start-ngrok.ps1`
- ⚡ Khởi động Spring Boot application

---

## 🌐 API Endpoints

> **Base URL:** `https://localhost:8443`

### 🔐 Authentication
```
POST   /auth/login      - Đăng nhập
POST   /auth/register   - Đăng ký tài khoản
POST   /auth/refresh    - Làm mới token
```

### 💳 Payment
```
POST   /payment/paypal/create   - Tạo thanh toán PayPal
POST   /payment/zalopay/create  - Tạo thanh toán ZaloPay
POST   /zalopay/callback        - ZaloPay webhook callback
```

---

## 🔧 Troubleshooting

### ❌ Redis không kết nối được

```bash
# Kiểm tra Redis container
docker ps -a

# Xem logs
docker logs hotel-redis

# Restart container
docker restart hotel-redis
```

### ❌ Ngrok bị disconnect

```bash
# Script tự động khởi động lại ngrok khi chạy app
# Hoặc khởi động thủ công:
.\scripts\start-ngrok.bat

# Chạy trực tiếp PowerShell:
powershell -ExecutionPolicy Bypass -File .\scripts\start-ngrok.ps1
```

> 💡 **Lưu ý:** Callback URL sẽ tự động cập nhật trong `application.yaml`

### ❌ SSL Certificate lỗi

- ✅ Đảm bảo `keystore.jks` trong `src/main/resources/`
- ✅ Kiểm tra password trong `application.yaml`
- ✅ Chấp nhận self-signed certificate trên browser

### ❌ MySQL connection failed

- ✅ Kiểm tra MySQL service đang chạy
- ✅ Verify username/password trong `application-secret.yaml`
- ✅ Đảm bảo database đã được tạo

---

## 👨‍💻 Tác giả

**MADE BY HOÀNG BÌNH ĐỊNH**

---

<div align="center">

### 🌟 Cảm ơn bạn đã quan tâm đến dự án! 🌟

*Nếu có bất kỳ vấn đề gì, vui lòng tạo issue hoặc liên hệ trực tiếp.*

**© 2024-2025 - Trường Đại học Mỏ - Địa chất**

</div>