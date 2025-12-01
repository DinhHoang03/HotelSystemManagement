package com.humg.HotelSystemManagement.modules.payment_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRepository;
import com.humg.HotelSystemManagement.modules.booking_service.services.BookingService;
import com.humg.HotelSystemManagement.modules.email_service.services.EmailService;
import com.humg.HotelSystemManagement.modules.payment_service.configs.ZaloPayConfig;
import com.humg.HotelSystemManagement.modules.payment_service.crypto.HMACUtil;
import com.humg.HotelSystemManagement.modules.payment_service.models.entities.PaymentBill;
import com.humg.HotelSystemManagement.modules.payment_service.models.repositories.PaymentBillRepository;
import com.humg.HotelSystemManagement.modules.payment_service.resources.requests.ZaloPayOrderRequest;
import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
import com.humg.HotelSystemManagement.utils.enums.PaymentMethod;
import com.humg.HotelSystemManagement.utils.enums.PaymentStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZaloPayService {
    ZaloPayConfig zaloPayConfig;
    BookingRepository bookingRepository;
    PaymentBillRepository paymentBillRepository;
    BookingService bookingService;
    EmailService emailService;

    private String getCurrentTimeString() {
        return LocalDateTime.now(ZoneId.of("GMT+7"))
                .format(DateTimeFormatter.ofPattern("yyMMdd"));
    }

    // 1. TẠO ĐƠN HÀNG (Giữ nguyên)
    public String createPayment(ZaloPayOrderRequest request) throws IOException {
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        String bookingId = request.getBookingId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        Random random = new Random();
        int randomId = random.nextInt(1000000);
        String appTransId = getCurrentTimeString() + "_" + randomId;

        // Lưu PENDING trước
        PaymentBill pendingBill = PaymentBill.builder()
                .transactionId(appTransId)
                .paymentMethod(PaymentMethod.ZALO_PAY)
                .paidAmount(booking.getGrandTotal())
                .status(PaymentStatus.PENDING)
                .createAt(LocalDateTime.now())
                .user(booking.getUser())
                .booking(booking)
                .build();
        paymentBillRepository.save(pendingBill);

        // Gọi ZaloPay
        Map<String, Object> zaloPayOrder = new HashMap<>();
        zaloPayOrder.put("app_id", zaloPayConfig.getAppId());
        zaloPayOrder.put("app_trans_id", appTransId);
        zaloPayOrder.put("app_time", System.currentTimeMillis());
        zaloPayOrder.put("app_user", booking.getUser().getUsername());
        zaloPayOrder.put("amount", booking.getGrandTotal());
        zaloPayOrder.put("description", "Payment for Booking #" + booking.getBookingId());
        zaloPayOrder.put("bankcode", "");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> itemMap = new LinkedHashMap<>();
        itemMap.put("itemid", booking.getBookingId());
        itemMap.put("itename", "Room Booking");
        itemMap.put("itemprice", booking.getGrandTotal());
        itemMap.put("itemquantity", 1);
        String itemJsonString = objectMapper.writeValueAsString(List.of(itemMap));

        Map<String, Object> embedData = new LinkedHashMap<>();
        embedData.put("redirecturl", zaloPayConfig.getRedirectUrl());
        String embedDataJsonString = objectMapper.writeValueAsString(embedData);

        zaloPayOrder.put("item", itemJsonString);
        zaloPayOrder.put("embed_data", embedDataJsonString);
        zaloPayOrder.put("callback_url", zaloPayConfig.getCallbackUrl());

        String data = zaloPayOrder.get("app_id") + "|" + zaloPayOrder.get("app_trans_id") + "|"
                + zaloPayOrder.get("app_user") + "|" + zaloPayOrder.get("amount") + "|"
                + zaloPayOrder.get("app_time") + "|" + embedDataJsonString + "|" + itemJsonString;

        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zaloPayConfig.getKey1(), data);
        zaloPayOrder.put("mac", mac);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(zaloPayConfig.getCreateOrderUrl());
            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, Object> e : zaloPayOrder.entrySet()) {
                params.add(new BasicNameValuePair(e.getKey(), e.getValue().toString()));
            }
            post.setEntity(new UrlEncodedFormEntity(params));

            try (CloseableHttpResponse response = client.execute(post)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder resultJsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resultJsonString.append(line);
                }
                return resultJsonString.toString();
            }
        } catch (Exception e) {
            log.error("Create ZaloPay Order Failed: {}", e.getMessage());
            return "{\"error\": \"Failed to create order: " + e.getMessage() + "\"}";
        }
    }

    // 2. CHỦ ĐỘNG TRA SOÁT TRẠNG THÁI (API NÀY QUAN TRỌNG NHẤT)
    public Map<String, Object> checkOrderStatus(String appTransId) {
        try {
            String appId = String.valueOf(zaloPayConfig.getAppId());
            String data = appId + "|" + appTransId + "|" + zaloPayConfig.getKey1();
            String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zaloPayConfig.getKey1(), data);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("app_id", appId));
            params.add(new BasicNameValuePair("app_trans_id", appTransId));
            params.add(new BasicNameValuePair("mac", mac));

            URIBuilder uri = new URIBuilder(zaloPayConfig.getOrderStatusUrl());
            uri.addParameters(params);

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(uri.build());
                post.setEntity(new UrlEncodedFormEntity(params));

                try (CloseableHttpResponse response = client.execute(post)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    StringBuilder resultJsonString = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        resultJsonString.append(line);
                    }

                    JSONObject result = new JSONObject(resultJsonString.toString());

                    // return_code = 1 nghĩa là ZaloPay xác nhận đã thanh toán thành công
                    if (result.optInt("return_code") == 1) {
                        updateBookingStatusToPaid(appTransId);
                    }

                    return result.toMap();
                }
            }
        } catch (Exception e) {
            log.error("Check Status Error: {}", e.getMessage());
            return Map.of("return_code", -1, "return_message", e.getMessage());
        }
    }

    // 3. LOGIC CẬP NHẬT DB (Dùng chung)
    private void updateBookingStatusToPaid(String appTransId) {
        PaymentBill paymentBill = paymentBillRepository.findByTransactionId(appTransId).orElse(null);

        if (paymentBill != null && paymentBill.getStatus() == PaymentStatus.PENDING) {
            paymentBill.setStatus(PaymentStatus.COMPLETED);
            paymentBillRepository.save(paymentBill);

            Booking booking = paymentBill.getBooking();
            booking.setPaymentStatus(PaymentStatus.COMPLETED);
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setPaymentDate(LocalDateTime.now());
            booking.setPaymentTransactionId(appTransId);

            if (booking.getBookingRooms() != null) {
                booking.getBookingRooms().forEach(br -> br.setBookingStatus(BookingStatus.CONFIRMED));
            }
            if (booking.getBookingItems() != null) {
                booking.getBookingItems().forEach(bi -> bi.setBookingStatus(BookingStatus.CONFIRMED));
            }
            bookingRepository.save(booking);

            sendBookingConfirmationEmail(booking);
            log.info(">>> UPDATE SUCCESSFUL for Transaction: {}", appTransId);
        }
    }

    // 4. CALLBACK (Vẫn giữ để dự phòng)
    @Transactional
    public JSONObject doCallback(String jsonStr) {
        JSONObject result = new JSONObject();
        try {
            JSONObject cbdata = new JSONObject(jsonStr);
            String dataStr = cbdata.getString("data");
            String reqMac = cbdata.getString("mac");
            String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zaloPayConfig.getKey2(), dataStr);

            if (!reqMac.equals(mac)) {
                result.put("returncode", -1);
                result.put("returnmessage", "mac not equal");
            } else {
                JSONObject data = new JSONObject(dataStr);
                String appTransId = data.getString("app_trans_id");
                updateBookingStatusToPaid(appTransId);
                result.put("returncode", 1);
                result.put("returnmessage", "success");
            }
        } catch (Exception ex) {
            result.put("returncode", 0);
            result.put("returnmessage", ex.getMessage());
        }
        return result;
    }

    private void sendBookingConfirmationEmail(Booking booking) {
        try {
            emailService.sendBookingConfirmationEmail(booking);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }
}