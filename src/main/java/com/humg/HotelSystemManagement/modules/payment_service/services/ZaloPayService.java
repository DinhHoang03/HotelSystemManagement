package com.humg.HotelSystemManagement.modules.payment_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRepository;
import com.humg.HotelSystemManagement.modules.booking_service.services.BookingService;
import com.humg.HotelSystemManagement.modules.payment_service.configs.ZaloPayConfig;
import com.humg.HotelSystemManagement.modules.payment_service.crypto.HMACUtil;
import com.humg.HotelSystemManagement.modules.payment_service.resources.requests.ZaloPayOrderRequest;
import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZaloPayService {
    ZaloPayConfig zaloPayConfig;
    BookingRepository bookingRepository;
    BookingService bookingService;

    private String getCurrentTimeString(String format){
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }

    public String createPayment(ZaloPayOrderRequest request) throws IOException {
        if(request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        // 1. Lấy Booking trực tiếp (Thay vì BookingBill)
        // Lưu ý: Request nên gửi bookingId. Nếu request vẫn dùng tên getBookingBillId(), hãy map nó thành bookingId
        String bookingId = request.getBookingBillId(); // Giả sử field này chứa ID booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        Random random = new Random();
        int randomId = random.nextInt(1000000);
        String appTransId = getCurrentTimeString("yyMMdd") + "_" + randomId;

        Map<String, Object> zaloPayOrder = new HashMap<>();
        zaloPayOrder.put("app_id", zaloPayConfig.getAppId());
        zaloPayOrder.put("app_trans_id", appTransId);
        zaloPayOrder.put("app_time", System.currentTimeMillis());
        zaloPayOrder.put("app_user", booking.getUser().getUsername());
        zaloPayOrder.put("amount", booking.getGrandTotal());
        zaloPayOrder.put("description", "Payment for Booking #" + booking.getBookingId());
        zaloPayOrder.put("bankcode", "");

        // Tạo Item JSON
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> itemMap = new LinkedHashMap<>();
        itemMap.put("itemid", booking.getBookingId());
        itemMap.put("itename", "Room Booking");
        itemMap.put("itemprice", booking.getGrandTotal());
        itemMap.put("itemquantity", 1);
        String itemJsonString = objectMapper.writeValueAsString(List.of(itemMap));

        // Tạo Embed Data JSON
        Map<String, Object> embedData = new LinkedHashMap<>();
        embedData.put("redirecturl", zaloPayConfig.getRedirectUrl());
        String embedDataJsonString = objectMapper.writeValueAsString(embedData);

        zaloPayOrder.put("item", itemJsonString);
        zaloPayOrder.put("embed_data", embedDataJsonString);
        zaloPayOrder.put("callback_url", zaloPayConfig.getCallbackUrl());

        // Tạo MAC
        String data = zaloPayOrder.get("app_id") + "|" + zaloPayOrder.get("app_trans_id") + "|"
                + zaloPayOrder.get("app_user") + "|" + zaloPayOrder.get("amount") + "|"
                + zaloPayOrder.get("app_time") + "|" + embedDataJsonString + "|" + itemJsonString;

        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zaloPayConfig.getKey1(), data);
        zaloPayOrder.put("mac", mac);

        // Gửi Request sang ZaloPay
        try (CloseableHttpClient client = HttpClients.createDefault()){
            HttpPost post = new HttpPost(zaloPayConfig.getCreateOrderUrl());
            List<NameValuePair> params = new ArrayList<>();
            for(Map.Entry<String, Object> e : zaloPayOrder.entrySet()) {
                params.add(new BasicNameValuePair(e.getKey(), e.getValue().toString()));
            }
            post.setEntity(new UrlEncodedFormEntity(params));

            try (CloseableHttpResponse response = client.execute(post)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder resultJsonString = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                    resultJsonString.append(line);
                }

                // Lưu ý: Ở môi trường thật, việc cập nhật trạng thái thanh toán (bookingService.processSuccessfulPayment)
                // nên được thực hiện ở API Callback (khi ZaloPay gọi ngược lại server mình).
                // Tuy nhiên, nếu bạn muốn cập nhật ngay để test (giả lập thành công):
                // bookingService.processSuccessfulPayment(bookingId, appTransId, booking.getGrandTotal(), PaymentMethod.ZALO_PAY);

                return resultJsonString.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to create order: " + e.getMessage() + "\"}";
        }
    }
}