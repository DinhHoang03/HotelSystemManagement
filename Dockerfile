# Đổi từ openjdk:21-jdk-slim (đã cũ) sang eclipse-temurin:21-jdk (mới nhất)
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Cài đặt curl và jq để script tự động hoạt động
# eclipse-temurin bản chuẩn dựa trên Ubuntu nên dùng apt-get bình thường
RUN apt-get update && apt-get install -y curl jq && rm -rf /var/lib/apt/lists/*

# Copy file jar sau khi build vào container
COPY target/*.jar app.jar

# Copy script khởi động tự động
COPY entrypoint.sh entrypoint.sh
# Cấp quyền thực thi cho script
RUN chmod +x entrypoint.sh

EXPOSE 8080

# Chạy script này thay vì chạy java trực tiếp
ENTRYPOINT ["./entrypoint.sh"]