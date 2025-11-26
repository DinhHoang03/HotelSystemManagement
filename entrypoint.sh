#!/bin/bash

# 1. Chờ dịch vụ Ngrok khởi động (thử kết nối đến cổng 4040)
echo "Dang cho Ngrok khoi dong..."
until curl -s http://hotel_ngrok:4040/api/tunnels > /dev/null; do
  sleep 2
done

# 2. Lấy URL Public từ API của Ngrok (dùng jq để parse JSON)
# Lưu ý: Lấy tunnel https đầu tiên tìm thấy
echo "Dang lay URL Ngrok..."
export NGROK_URL=$(curl -s http://hotel_ngrok:4040/api/tunnels | jq -r '.tunnels[] | select(.proto=="https") | .public_url' | head -n 1)

echo ">>> URL NGROK TIM THAY: $NGROK_URL"

# 3. Gán vào biến callback cho ZaloPay
export ZALO_CALLBACK_URL="$NGROK_URL/zalopay/callback"
echo ">>> ZALO CALLBACK SETUP: $ZALO_CALLBACK_URL"

# 4. Chạy ứng dụng Java
echo ">>> DANG KHOI DONG SPRING BOOT..."
exec java -jar app.jar